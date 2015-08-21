package com.revolut.interview.services;

import com.revolut.interview.model.Transaction;

public class ExecuteTransactionResult {

    private final Transaction transaction;
    private final ExecutionStatus status;
    private final String detailMessage;

    public ExecuteTransactionResult(Transaction transaction, ExecutionStatus status) {
        this(transaction, status, "");
    }

    public ExecuteTransactionResult(Transaction transaction, ExecutionStatus status, String detailMessage) {
        this.transaction = transaction;
        this.status = status;
        this.detailMessage = detailMessage;
    }


    public Transaction getTransaction() {
        return transaction;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public enum ExecutionStatus {

        SUCCESS(2000, "Transaction executed successfully"),

        UNCHANGED(3040, "Transaction already executed"),

        //Errors
        INSUFFICIENT_FUNDS(4220, "Source account has insufficient funds"),
        TRANSACTION_NOT_FOUND(4221, "Transaction could not be located"),
        ACCOUNT_NOT_FOUND(4222, "Account could not be located"),

        INTERNAL_ERROR(5000, "Unpredictable internal error"),
        COULD_NOT_ACQUIRE_RESOURCES(5001, "Resources could not be acquired");

        private final int statusCode;
        private final String description;

        ExecutionStatus(int statusCode, String description) {

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
