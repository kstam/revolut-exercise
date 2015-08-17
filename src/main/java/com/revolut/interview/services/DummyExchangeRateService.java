package com.revolut.interview.services;

import java.math.BigDecimal;
import java.util.Currency;

public class DummyExchangeRateService implements ExchangeRateService{

    public BigDecimal getExchangeRate(Currency src, Currency dest) {
        return BigDecimal.ONE;
    }
}
