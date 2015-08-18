package com.revolut.interview.repos;

import java.math.BigDecimal;
import java.util.Currency;

public class DummyExchangeRateRepo implements ExchangeRateRepo {
    public BigDecimal getExchangeRate(Currency src, Currency dest) {
        return BigDecimal.ONE;
    }

}
