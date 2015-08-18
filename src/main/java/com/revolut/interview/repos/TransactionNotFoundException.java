package com.revolut.interview.repos;

public class TransactionNotFoundException extends DataAccessException {

    public TransactionNotFoundException(Long transactionId) {
        super("Could not find transaction for id [" + transactionId + "]");
    }

}
