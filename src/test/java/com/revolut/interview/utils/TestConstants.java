package com.revolut.interview.utils;

import com.revolut.interview.model.Amount;

import java.math.BigDecimal;
import java.util.Currency;

public interface TestConstants {
    Currency EUR = Currency.getInstance("EUR");
    Currency USD = Currency.getInstance("USD");

    Amount EUR_10 = new Amount(BigDecimal.TEN, TestConstants.EUR);
    Amount EUR_5 = new Amount(new BigDecimal("5"), TestConstants.EUR);
    Amount USD_20 = new Amount(new BigDecimal("20"), TestConstants.USD);
}
