package com.revolut.interview.model;

import com.google.common.base.MoreObjects;

import java.util.Currency;

public class Transaction {

    private Long id;
    private long srcId;
    private long dstId;
    private Amount amount;
    private TransactionStatus status;

    public Transaction(long srcId, long dstId, Amount amount) {
        this(null, srcId, dstId, amount, TransactionStatus.PENDING);
    }

    public Transaction(Long id, long srcId, long dstId, Amount amount,
                       TransactionStatus status) {
        this.id = id;
        this.srcId = srcId;
        this.dstId = dstId;
        this.amount = amount;
        this.status = status;
    }

    public long getSourceId() {
        return srcId;
    }

    public long getDestinationId() {
        return dstId;
    }

    public Amount getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return amount.getCurrency();
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Long getId() {
        return id;
    }

    public Transaction execute() {
        return new Transaction(id, srcId, dstId, amount, TransactionStatus.SUCCESSFUL);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("srcId", srcId)
                .add("dstId", dstId)
                .add("amount", amount)
                .add("status", status)
                .toString();
    }

    public enum TransactionStatus {
        FAILED,
        SUCCESSFUL,
        PENDING
    }
}
