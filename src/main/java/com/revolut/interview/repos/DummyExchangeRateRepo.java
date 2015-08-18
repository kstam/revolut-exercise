package com.revolut.interview.repos;

import java.math.BigDecimal;
import java.util.Currency;

class DummyExchangeRateRepo implements ExchangeRateRepo {
    public BigDecimal getExchangeRate(Currency src, Currency dest) {
        return BigDecimal.ONE;
    }

}
