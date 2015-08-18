package com.revolut.interview.model;

import com.revolut.interview.utils.TestConstants;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static org.testng.Assert.assertEquals;

public class AccountTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotBeInitializedWithNullBalance() {
        new Account(1L, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotBeInitialiezedWithNegativeId() {
        new Account(-1, TestConstants.EUR_10);
    }

    @Test
    public void testCanDepositACorrectAmount() {
        Account account = new Account(1L, TestConstants.EUR_10);
        account = account.deposit(TestConstants.EUR_5);
        assertEquals(account.getBalance(), new Amount(new BigDecimal(15), TestConstants.EUR));
    }

    @Test
    public void testCanWithdrawFromAcount() {
        Account account = new Account(1L, TestConstants.EUR_10);
        account = account.withdraw(TestConstants.EUR_5);
        assertEquals(account.getBalance(), TestConstants.EUR_5);
    }
}