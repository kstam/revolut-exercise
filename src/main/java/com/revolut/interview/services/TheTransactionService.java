package com.revolut.interview.services;

import com.revolut.interview.model.Account;
import com.revolut.interview.model.Amount;
import com.revolut.interview.model.Transaction;
import com.revolut.interview.repos.*;

import static com.revolut.interview.utils.Assert.checkNotNull;

public class TheTransactionService implements TransactionService {

    private final AccountRepo accountRepo;
    private final TransactionRepo transactionRepo;

    public TheTransactionService(AccountRepo accountRepo, TransactionRepo transactionRepo) {
        checkNotNull(accountRepo, "accountRepo cannot be null");
        checkNotNull(transactionRepo, "transactionRepo cannot be null");
        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
    }

    public Transaction createTransaction(long srcAccountId, long dstAccountId, Amount amount)
            throws TransactionServiceException {
        try {
            accountRepo.getById(srcAccountId);
            accountRepo.getById(dstAccountId);
            Transaction txn = new Transaction(srcAccountId, dstAccountId, amount);
            return transactionRepo.insert(txn);
        } catch (DataAccessException dae) {
            throw new TransactionServiceException("Could not create transaction", dae);
        }
    }

    public boolean executeTransaction(long transactionId) {
        return false;
    }
}
