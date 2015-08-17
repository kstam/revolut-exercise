package com.revolut.interview.repos;

import com.revolut.interview.model.Account;

public interface AccountRepo {

    Account getAccountById(long l) throws DataAccessException;
}
