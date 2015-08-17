package com.revolut.interview.model;

import com.google.common.base.MoreObjects;

import java.math.BigDecimal;
import java.util.Currency;

public class Transaction {

    private Long id;
    private Account src;
    private Account dst;
    private BigDecimal amount;
    private Currency currency;
    private TransactionStatus status;

    public Transaction(Account src, Account dst, BigDecimal amount, Currency currency) {
        this(null, src, dst, amount, currency, TransactionStatus.PENDING);
    }

    public Transaction(Long id, Account src, Account dst, BigDecimal amount, Currency currency,
                       TransactionStatus status) {
        this.id = id;
        this.src = src;
        this.dst = dst;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    public Account getSource() {
        return src;
    }

    public Account getDestination() {
        return dst;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("src", src)
                .add("dst", dst)
                .add("amount", amount)
                .add("currency", currency)
                .add("status", status)
                .toString();
    }

    public enum TransactionStatus {
        FAILED,
        SUCCESSFUL,
        PENDING
    }
}
