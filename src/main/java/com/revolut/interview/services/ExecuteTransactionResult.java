package com.revolut.interview.services;

import com.revolut.interview.model.Transaction;

public class ExecuteTransactionResult {

    private final Transaction transaction;
    private final ExecutionStatus executionStatus;
    private final String originalErrorMessage;

    public ExecuteTransactionResult(Transaction transaction, ExecutionStatus executionStatus) {
        this(transaction, executionStatus, null);
    }

    public ExecuteTransactionResult(Transaction transaction, ExecutionStatus executionStatus, String originalErrorMessage) {
        this.transaction = transaction;
        this.executionStatus = executionStatus;
        this.originalErrorMessage = originalErrorMessage;
    }


    public Transaction getTransaction() {
        return transaction;
    }

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public String getOriginalErrorMessage() {
        return originalErrorMessage;
    }

    public enum ExecutionStatus {

        SUCCESS(2000, "Transaction executed successfully"),

        UNCHANGED(3040, "Transaction already executed"),

        //Errors
        INSUFFICIENT_FUNDS(4000, "Source account has insufficient funds"),
        TRANSACTION_NOT_FOUND(4040, "Transaction could not be located"),
        ACCOUNT_NOT_FOUND(4041, "Account could not be located"),

        COULD_NOT_ACQUIRE_RESOURCES(5000, "Resources could not be acquired");

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
