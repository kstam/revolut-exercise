package com.revolut.interview.model;

import com.revolut.interview.utils.TestConstants;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AmountTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotBeInitializedWithNullBalance() {
        new Amount(null, TestConstants.EUR);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotBeInitializedWithNullCurrency() {
        new Amount(BigDecimal.TEN, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotInitializeNegativeAmount() {
        new Amount(new BigDecimal("-10"), TestConstants.EUR);
    }

    @Test
    public void testCanAddAmountsWithSameCurrencies() {
        Amount a1 = new Amount(BigDecimal.TEN, TestConstants.EUR);
        Amount a2 = new Amount(BigDecimal.TEN, TestConstants.EUR);

        Amount result = a1.add(a2);
        assertTrue(result.getValue().equals(new BigDecimal(20)));
        assertEquals(result.getCurrency(), TestConstants.EUR);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotAddAmountsWithDifferentCurrencies() {
        Amount a1 = new Amount(BigDecimal.TEN, TestConstants.EUR);
        Amount a2 = new Amount(BigDecimal.TEN, TestConstants.USD);

        a1.add(a2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotSubtractAmountsWithDifferentCurrencies() {
        Amount a1 = new Amount(BigDecimal.TEN, TestConstants.EUR);
        Amount a2 = new Amount(BigDecimal.TEN, TestConstants.USD);

        a1.subtract(a2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotSubtractBiggerAmountFromSmallerAmount() {
        Amount a1 = TestConstants.EUR_10;
        Amount a2 = TestConstants.EUR_5;
        a2.subtract(a1);
     }

    @Test
    public void testSubtractReturnsCorrectResult() {
        Amount a1 = TestConstants.EUR_10;
        Amount a2 = TestConstants.EUR_5;
        assertTrue(a1.subtract(a2).getValue().equals(new BigDecimal("5")));
        assertTrue(a1.subtract(a1).getValue().equals(BigDecimal.ZERO));
    }
}
