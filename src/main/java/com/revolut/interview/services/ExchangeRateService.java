package com.revolut.interview.services;

import java.math.BigDecimal;
import java.util.Currency;

public interface ExchangeRateService {

    BigDecimal getExchangeRate(Currency src, Currency dest);

}
