package com.revolut.interview.controllers;

public class ExceptionResponse {

    private String message;
    private String exceptionType;
    private String exceptionMessage;
    private long timestamp;

    public ExceptionResponse(Throwable t) {
        this.message = "An error occurred";
        this.exceptionType = t.getClass().getSimpleName();
        this.exceptionMessage = t.getMessage();
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
