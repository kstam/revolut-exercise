package com.revolut.interview.utils;

import java.math.BigDecimal;

import static org.testng.Assert.assertTrue;

public class TestUtils {
   public static void assertEquals(BigDecimal b1, BigDecimal b2) {
       assertTrue(b1.compareTo(b2) == 0, "Expected [" + b2 + "] but got [" + b1 + "]");
   }
}
