package com.revolut.interview.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.revolut.interview.utils.Assert;

import java.util.Currency;

public class Account {

    private long id;
    private Amount balance;
    private Currency currency;

    Account() {
        // empty constructor for Jackson
    }

    public Account(Amount balance) {
        this(0, balance);
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Account account = (Account) o;
        return Objects.equal(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("balance", balance)
                .add("currency", currency)
                .toString();
    }
}
