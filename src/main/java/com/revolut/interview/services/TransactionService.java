package com.revolut.interview.services;

import com.revolut.interview.model.Amount;
import com.revolut.interview.model.Transaction;

public interface TransactionService {

    Transaction createTransaction(long srcAccountId, long dstAccountId, Amount amount)
            throws TransactionServiceException;

    ExecuteTransactionResult executeTransaction(long transactionId) throws TransactionServiceException;

}
