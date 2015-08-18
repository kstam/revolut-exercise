package com.revolut.interview.repos;

import com.revolut.interview.model.Account;
import com.revolut.interview.model.Amount;
import com.revolut.interview.utils.TestConstants;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class InMemoryAccountRepoTest {

    InMemoryAccountRepo repo;

    @BeforeMethod
    public void setup() {
        repo = new InMemoryAccountRepo();
    }

    @Test
    public void testCanInsertATransaction() throws DataAccessException {
        Account account = repo.insert(new Account(TestConstants.EUR_5));
        assertNotNull(account.getId());

        Account gotAccount = repo.getById(account.getId());
        assertEquals(gotAccount, account);
        assertEquals(gotAccount.getId(), 1L);
        assertEquals(gotAccount.getBalance(), TestConstants.EUR_5);
        assertEquals(gotAccount.getCurrency(), TestConstants.EUR);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotUpdateANonPersistedTransaction() throws DataAccessException {
        Account account = new Account(TestConstants.EUR_5);
        repo.update(account);
    }

    @Test
    public void testCanUpdateATransaction() throws DataAccessException {
        Account account = repo.insert(new Account(TestConstants.EUR_5));
        Account updatedAccount = repo.update(account.withdraw(TestConstants.EUR_5));

        Account gotAccount = repo.getById(account.getId());
        assertEquals(updatedAccount.getBalance(), gotAccount.getBalance());
        assertEquals(gotAccount.getBalance(), new Amount(BigDecimal.ZERO, TestConstants.EUR));
    }

    @Test(expectedExceptions = CouldNotLockResourceException.class)
    public void testCanLockATransactionAndIfSomeoneElseTriesToAcquireTheLockTheyFail() throws DataAccessException {
        Account account = repo.insert(new Account(TestConstants.EUR_5));
        repo.lockById(account.getId(), 50);
        repo.lockById(account.getId(), 50);
    }

    @Test
    public void testDoesNotFailIfLockIsReleasedWithinGivenTimeout() throws InterruptedException, DataAccessException {
        Account account = repo.insert(new Account(TestConstants.EUR_5));
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            try {
                latch.countDown();
                repo.lockById(account.getId());
                Thread.sleep(30);
                repo.unlockById(account.getId());
            } catch (DataAccessException | InterruptedException ignored) {
            }
        }).start();

        latch.await();
        repo.lockById(account.getId(), 400); // this has to wait
    }

    @Test
    public void testMultipleUnlocksDoNotIncreaseThePermitsToMoreThanOne() throws DataAccessException {
        Account account = repo.insert(new Account(TestConstants.EUR_5));
        repo.unlockById(account.getId());
        assertEquals(repo.getPermitsForLock(account.getId()), 1);
    }

    @Test
    public void testConcurrentUnlocks() {
        Account account = repo.insert(new Account(TestConstants.EUR_5));
        int N = 200;
        ExecutorService es = Executors.newFixedThreadPool(N);
        CountDownLatch latch = new CountDownLatch(N);
        List<Future<Integer>> results = new ArrayList<>();

        for (int i = 0; i < N; i++) {
            results.add(es.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    repo.lockById(account.getId());
                    Thread.sleep(5L);
                    int permitsAfterLock = repo.getPermitsForLock(account.getId());
                    repo.unlockById(account.getId());
                    return permitsAfterLock;
                } catch (DataAccessException | InterruptedException ignored) {
                    return -1;
                }
            }));
        }

        results.forEach(future -> {
            Integer result = null;
            try {
                result = future.get();
            } catch (InterruptedException | ExecutionException e) {
                result = -1;
            }
            assertEquals(result, new Integer(0));
        });

        assertEquals(repo.getPermitsForLock(account.getId()), 1);
    }
}