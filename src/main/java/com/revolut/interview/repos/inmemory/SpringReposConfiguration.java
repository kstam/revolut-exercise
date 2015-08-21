package com.revolut.interview.repos.inmemory;

import com.revolut.interview.repos.AccountRepo;
import com.revolut.interview.repos.ExchangeRateRepo;
import com.revolut.interview.repos.TransactionRepo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class SpringReposConfiguration {

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public AccountRepo accountRepo() {
        return new InMemoryAccountRepo();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public TransactionRepo transactionRepo() {
        return new InMemoryTransactionRepo();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public ExchangeRateRepo exchangeRateRepo() {
        return new DummyExchangeRateRepo();
    }
}
