package com.revolut.interview.services;

import com.revolut.interview.model.Account;
import com.revolut.interview.model.Transaction;
import com.revolut.interview.repos.*;

import java.math.BigDecimal;
import java.util.Currency;

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

    public Transaction createTransaction(long srcAccountId, long dstAccountId, BigDecimal amount, Currency currency)
            throws TransactionServiceException {
        try {
            Account srcAccount = accountRepo.getAccountById(srcAccountId);
            Account dstAccount = accountRepo.getAccountById(dstAccountId);
            Transaction txn = new Transaction(srcAccount, dstAccount, amount, currency);
            return transactionRepo.insert(txn);
        } catch (DataAccessException dae) {
            throw new TransactionServiceException("Could not create transaction", dae);
        }
    }

    public boolean executeTransaction(long transactionId) {
        return false;
    }
}
