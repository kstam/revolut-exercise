package com.revolut.interview.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

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

    public Transaction executed() {
        return new Transaction(id, srcId, dstId, amount, TransactionStatus.EXECUTED);
    }

    public Transaction failed() {
        return new Transaction(id, srcId, dstId, amount, TransactionStatus.FAILED);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Transaction that = (Transaction) o;
        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
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
        EXECUTED,
        PENDING
    }
}
