package com.revolut.interview.model;

import com.google.common.base.MoreObjects;

import java.math.BigDecimal;
import java.util.Currency;

public class Transaction {

    private Long id;
    private long srcId;
    private long dstId;
    private BigDecimal amount;
    private Currency currency;
    private TransactionStatus status;

    public Transaction(long srcId, long dstId, BigDecimal amount, Currency currency) {
        this(null, srcId, dstId, amount, currency, TransactionStatus.PENDING);
    }

    public Transaction(Long id, long srcId, long dstId, BigDecimal amount, Currency currency,
                       TransactionStatus status) {
        this.id = id;
        this.srcId = srcId;
        this.dstId = dstId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    public long getSourceId() {
        return srcId;
    }

    public long getDestinationId() {
        return dstId;
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
                .add("srcId", srcId)
                .add("dstId", dstId)
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
