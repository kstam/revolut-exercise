package com.revolut.interview.services;

import com.revolut.interview.model.Transaction;

public class CreateTransactionResult {

    private final Transaction transaction;
    private final CreationStatus status;
    private final String detailMessage;

    public CreateTransactionResult(Transaction transaction, CreationStatus status) {
        this(transaction, status, "");
    }

    public CreateTransactionResult(Transaction transaction, CreationStatus status, String detailMessage) {
        this.transaction = transaction;
        this.status = status;
        this.detailMessage = detailMessage;

    }

    public Transaction getTransaction() {
        return transaction;
    }

    public CreationStatus getStatus() {
        return status;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public enum CreationStatus {
        SUCCESS(2000, "Transaction created successfully"),

        ACCOUNT_NOT_FOUND(4220, "Account id specified was not found."),

        INTERNAL_ERROR(5000, "Unexpected error");

        private final int statusCode;
        private final String description;

        CreationStatus(int statusCode, String description) {
            this.statusCode = statusCode;
            this.description = description;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getDescription() {
            return description;
        }
    }
}
