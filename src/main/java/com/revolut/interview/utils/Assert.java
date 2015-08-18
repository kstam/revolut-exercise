package com.revolut.interview.utils;

public class Assert {
    public static void checkNotNull(Object o, String msg) {
        if (o == null) {
            error(msg);
        }
    }

    public static void checkIsTrue(boolean value, String message) {
        if (!value) {
            error(message);
        }
    }

    private static void error(String msg) {
        throw new IllegalArgumentException(msg);
    }

}