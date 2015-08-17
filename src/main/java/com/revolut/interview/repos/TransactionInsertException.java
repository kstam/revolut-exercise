package com.revolut.interview.repos;

import com.revolut.interview.model.Transaction;

public class TransactionInsertException extends DataAccessException {

    public TransactionInsertException(Transaction transaction) {
        super("Could not insert transaction [" + transaction + "]");
    }
}
