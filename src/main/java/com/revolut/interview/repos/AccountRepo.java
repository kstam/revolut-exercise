package com.revolut.interview.repos;

import com.revolut.interview.model.Account;

public interface AccountRepo {

    Account insert(Account account) throws DataAccessException;

    Account getById(long accountId) throws DataAccessException;

    Account update(Account account) throws DataAccessException;

    void lockById(long accountId) throws DataAccessException;

    void unlockById(long accountId) throws DataAccessException;

}
