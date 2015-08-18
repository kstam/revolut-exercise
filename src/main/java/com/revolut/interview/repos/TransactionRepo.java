package com.revolut.interview.repos;

import com.revolut.interview.model.Transaction;

public interface TransactionRepo {

    Transaction insert(Transaction transaction) throws DataAccessException;

    Transaction update(Transaction transaction) throws DataAccessException;

    Transaction getById(long transactionId) throws DataAccessException;

    boolean lockById(long transactionId) throws DataAccessException;
}
