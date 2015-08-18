package com.revolut.interview.repos;

import com.revolut.interview.model.Account;

public interface AccountRepo {

    Account getById(long accountId) throws DataAccessException;

}
