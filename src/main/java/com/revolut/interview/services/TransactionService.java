package com.revolut.interview.services;

import com.revolut.interview.model.Transaction;

import java.math.BigDecimal;
import java.util.Currency;

public interface TransactionService {

    Transaction createTransaction(long srcAccountId, long dstAccountId, BigDecimal amount, Currency currency);

    boolean executeTransaction(long transactionId);

}
