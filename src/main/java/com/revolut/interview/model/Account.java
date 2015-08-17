package com.revolut.interview.model;

import java.math.BigDecimal;
import java.util.Currency;

public class Account {

    private long id;
    private BigDecimal balance;
    private Currency currency;

    public Account(long id, BigDecimal balance, Currency currency) {
        this.id = id;
        this.balance = balance;
        this.currency = currency;
    }

    public long getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Currency getCurrency() {
        return currency;
    }
}
