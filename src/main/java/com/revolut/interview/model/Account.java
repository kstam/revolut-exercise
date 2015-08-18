package com.revolut.interview.model;

import com.revolut.interview.utils.Assert;

import java.math.BigDecimal;
import java.util.Currency;

public class Account {

    private long id;
    private Amount balance;
    private Currency currency;

    public Account(long id, Amount balance) {
        Assert.checkNotNull(balance, "balance cannot be null");
        Assert.checkIsTrue(id >= 0, "id cannot be negative");
        this.id = id;
        this.balance = balance;
        this.currency = balance.getCurrency();
    }

    public long getId() {
        return id;
    }

    public Amount getBalance() {
        return balance;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Account deposit(Amount amount) {
        return new Account(id, balance.add(amount));
    }

    public Account withdraw(Amount amount) {
        return new Account(id, balance.subtract(amount));
    }
}
