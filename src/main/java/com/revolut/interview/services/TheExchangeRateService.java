package com.revolut.interview.services;

import com.revolut.interview.model.Amount;
import com.revolut.interview.repos.ExchangeRateRepo;
import com.revolut.interview.utils.Assert;

import java.math.BigDecimal;
import java.util.Currency;

class TheExchangeRateService implements ExchangeRateService {

    private final ExchangeRateRepo exchangeRateRepo;

    public TheExchangeRateService(ExchangeRateRepo exchangeRateRepo) {
        Assert.checkNotNull(exchangeRateRepo, "exchangeRateRepo cannot be null");
        this.exchangeRateRepo = exchangeRateRepo;
    }

    @Override
    public Amount convert(Amount amount, Currency target) {
        Assert.checkNotNull(amount, "Cannot convert null amount");
        Assert.checkNotNull(target, "Cannot convert to null currency");

        BigDecimal exchangeRate = exchangeRateRepo.getExchangeRate(amount.getCurrency(), target);
        return new Amount(amount.getValue().multiply(exchangeRate), target);
    }
}
