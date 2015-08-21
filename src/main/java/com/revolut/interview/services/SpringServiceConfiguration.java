package com.revolut.interview.services;

import com.revolut.interview.repos.AccountRepo;
import com.revolut.interview.repos.ExchangeRateRepo;
import com.revolut.interview.repos.dummy.DummyRepoConfiguration;
import com.revolut.interview.repos.inmemory.InMemoryReposConfiguration;
import com.revolut.interview.repos.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

@Configuration
@Import({InMemoryReposConfiguration.class, DummyRepoConfiguration.class})
public class SpringServiceConfiguration {

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private ExchangeRateRepo exchangeRateRepo;

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public TransactionService transactionService() {
        return new TheTransactionService(accountRepo, transactionRepo, exchangeRateService());
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public ExchangeRateService exchangeRateService() {
        return new TheExchangeRateService(exchangeRateRepo);
    }
}
