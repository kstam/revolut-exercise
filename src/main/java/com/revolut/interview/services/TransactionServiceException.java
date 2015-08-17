package com.revolut.interview.services;

public class TransactionServiceException extends Exception {

    public TransactionServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
