package com.revolut.interview.model;

import com.revolut.interview.model.Transaction.TransactionStatus;
import com.revolut.interview.utils.TestConstants;
import org.testng.annotations.Test;

public class TransactionTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotBeInitialiedWithNonPositiveSrcId() {
        new Transaction(0, 1L, TestConstants.EUR_5);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotBeInitialiedWithNonPositiveDestId() {
        new Transaction(1L, 0, TestConstants.EUR_5);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotBeInitializedWithNegativeId() {
        new Transaction(-1, 1, 2, TestConstants.EUR_5, TransactionStatus.PENDING);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotBeInitializedWithNullAmount() {
        new Transaction(1L, 2L, null);
    }

}