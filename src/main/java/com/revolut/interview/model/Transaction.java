package com.revolut.interview.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Currency;

public class Transaction {

    private long id;
    private long sourceId;
    private long destinationId;
    private Amount amount;
    private TransactionStatus status;

    Transaction() {
        // empty constructor for Jackson
    }

    public Transaction(long sourceId, long destinationId, Amount amount) {
        this(0, sourceId, destinationId, amount, TransactionStatus.PENDING);
    }

    public Transaction(long id, long sourceId, long destinationId, Amount amount,
                       TransactionStatus status) {
        this.id = id;
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.amount = amount;
        this.status = status;
    }

    public long getSourceId() {
        return sourceId;
    }

    public long getDestinationId() {
        return destinationId;
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

    public long getId() {
        return id;
    }

    public Transaction executed() {
        return new Transaction(id, sourceId, destinationId, amount, TransactionStatus.EXECUTED);
    }

    public Transaction failed() {
        return new Transaction(id, sourceId, destinationId, amount, TransactionStatus.FAILED);
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
                .add("sourceId", sourceId)
                .add("destinationId", destinationId)
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
