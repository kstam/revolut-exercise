package com.revolut.interview.services;

import com.revolut.interview.model.Amount;
import com.revolut.interview.repos.ExchangeRateRepo;
import com.revolut.interview.utils.TestConstants;
import com.revolut.interview.utils.TestUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.nio.file.attribute.UserDefinedFileAttributeView;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TheExchangeRateServiceTest {

    @Mock
    ExchangeRateRepo mockedRepo;

    TheExchangeRateService service;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        service = new TheExchangeRateService(mockedRepo);
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotBeInitializedWithNullRepo() {
        new TheExchangeRateService(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConvertDoesNotAllowNullAmount() {
        service.convert(null, TestConstants.USD);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConvertDoesNotAllowNullCurrency() {
        service.convert(TestConstants.EUR_5, null);
    }

    @Test
    public void testExchangeRateServiceConvertsAmountCorrectly() {
        when(mockedRepo.getExchangeRate(TestConstants.EUR, TestConstants.USD)).thenReturn(new BigDecimal("1.25"));
        when(mockedRepo.getExchangeRate(TestConstants.USD, TestConstants.EUR)).thenReturn(new BigDecimal("0.8"));

        Amount convertedAmount = service.convert(TestConstants.EUR_10, TestConstants.USD);
        assertEquals(convertedAmount.getCurrency(), TestConstants.USD);
        TestUtils.assertEquals(convertedAmount.getValue(), new BigDecimal("12.5"));

        convertedAmount = service.convert(new Amount(BigDecimal.TEN, TestConstants.USD), TestConstants.EUR);
        assertEquals(convertedAmount.getCurrency(), TestConstants.EUR);
        TestUtils.assertEquals(convertedAmount.getValue(), new BigDecimal("8"));

    }
}
