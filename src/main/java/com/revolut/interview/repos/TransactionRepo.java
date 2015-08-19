package com.revolut.interview.repos;

import com.revolut.interview.model.Transaction;

import java.util.List;

public interface TransactionRepo {

    Transaction insert(Transaction transaction) throws DataAccessException;

    Transaction update(Transaction transaction) throws DataAccessException;

    Transaction getById(long transactionId) throws DataAccessException;

    void lockById(long transactionId) throws DataAccessException;

    void unlockById(long transactionId) throws DataAccessException;

    List<Transaction> getAll() throws DataAccessException;
}
