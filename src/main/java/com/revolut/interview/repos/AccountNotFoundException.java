package com.revolut.interview.repos;

public class AccountNotFoundException extends DataAccessException {
    public AccountNotFoundException(Long accountId) {
        super("Could not find account for id [" + accountId + "]");
    }
}
