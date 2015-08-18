package com.revolut.interview.repos;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.assertTrue;

public class DummyExchangeRateRepoTest {

    ExchangeRateRepo repo;

    @BeforeMethod
    public void setup() {
        repo = new DummyExchangeRateRepo();
    }

    @DataProvider
    public Object[][] currencyCombinations() {
        Set<Object> currencies = Currency.getAvailableCurrencies()
                .stream().limit(4).collect(Collectors.toSet());
        int N = currencies.size();
        Object[][] result = new Object[N * N][2];

        int i = 0;
        for (Object c1 : currencies) {
            int j = 0;
            for (Object c2 : currencies) {
                result[i * N + j][0] = c1;
                result[i * N + j][1] = c2;
                j++;
            }
            i++;
        }
        return result;
    }

    @Test(dataProvider = "currencyCombinations")
    public void testReturnsOneForAnyCurrencyCombination(Currency c1, Currency c2) {
        assertTrue(repo.getExchangeRate(c1, c2).equals(BigDecimal.ONE));
    }
}