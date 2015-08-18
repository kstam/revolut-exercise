package com.revolut.interview.repos;

public class CouldNotLockResourceException extends DataAccessException {
    public CouldNotLockResourceException(String message) {
        super(message);
    }
}
