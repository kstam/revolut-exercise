package com.revolut.interview.services;

import com.revolut.interview.model.Amount;

import java.math.BigDecimal;
import java.util.Currency;

public interface ExchangeRateService {

    Amount convert(Amount amount, Currency target);

}
