package com.revolut.interview.repos;

public class AccountNotFoundAccessException extends DataAccessException {
    public AccountNotFoundAccessException(Long accountId) {
        super("Could not find account for id [" + accountId + "]");
    }
}
