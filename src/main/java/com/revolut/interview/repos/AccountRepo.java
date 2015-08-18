package com.revolut.interview.repos;

import com.revolut.interview.model.Account;

public interface AccountRepo {

    Account getById(long accountId) throws DataAccessException;

    Account update(Account account);

    boolean lockById(long accountId) throws DataAccessException;

    boolean unlockById(long accountId);
}
