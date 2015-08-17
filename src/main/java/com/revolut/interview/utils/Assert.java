package com.revolut.interview.utils;

public class Assert {
    public static void checkNotNull(Object o, String msg) {
        if (o == null) {
            error(msg);
        }
    }

    private static void error(String msg) {
        throw new IllegalArgumentException(msg);
    }

}