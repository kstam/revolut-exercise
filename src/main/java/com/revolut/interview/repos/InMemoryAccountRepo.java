package com.revolut.interview.repos;

import com.google.common.annotations.VisibleForTesting;
import com.revolut.interview.model.Account;
import com.revolut.interview.utils.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

class InMemoryAccountRepo implements AccountRepo {

    public static final long AWAIT_LOCK_MILLISECONDS = 5000L;
    AtomicLong maxAccountId = new AtomicLong(0);
    Map<Long, Account> accountsMap = new ConcurrentHashMap<>();
    Map<Long, Semaphore> accountLocks = new ConcurrentHashMap<>();

    @Override
    public Account insert(Account account) {
        long id = maxAccountId.incrementAndGet();
        Account toInsert = new Account(id, account.getBalance());
        accountsMap.put(toInsert.getId(), toInsert);
        accountLocks.put(toInsert.getId(), new Semaphore(1));
        return toInsert;
    }

    @Override
    public Account getById(long accountId) throws DataAccessException {
        Account account = accountsMap.get(accountId);
        if (account == null) {
            throw new AccountNotFoundException(accountId);
        }
        return account;
    }

    @Override
    public Account update(Account account) throws DataAccessException {
        Assert.checkIsTrue(account.getId() > 0, "Cannot update non persisted account");
        accountsMap.put(account.getId(), account);
        return account;
    }

    @Override
    public void lockById(long accountId) throws DataAccessException {
        lockById(accountId, AWAIT_LOCK_MILLISECONDS);
    }

    @Override
    public void unlockById(long accountId) throws DataAccessException {
        Semaphore semaphore = accountLocks.get(accountId);
        if (semaphore != null && semaphore.availablePermits() == 0) {
            semaphore.release();
        }
    }

    @VisibleForTesting
    void lockById(long accountId, long timeoutMilliseconds) throws DataAccessException {
        Semaphore semaphore = accountLocks.get(accountId);
        if (semaphore == null) {
            throw new AccountNotFoundException(accountId);
        } else {
            try {
                if (!semaphore.tryAcquire(timeoutMilliseconds, TimeUnit.MILLISECONDS)) {
                    throw new CouldNotLockResourceException("Could not lock account with id " + accountId);
                }
            } catch (InterruptedException e) {
                throw new CouldNotLockResourceException("Could not lock account with id " + accountId);
            }
        }
    }

    @VisibleForTesting
    int getPermitsForLock(long accountId) {
        return accountLocks.get(accountId).availablePermits();
    }
}
