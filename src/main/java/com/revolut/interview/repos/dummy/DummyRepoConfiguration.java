package com.revolut.interview.repos.dummy;

import com.revolut.interview.repos.ExchangeRateRepo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class DummyRepoConfiguration {
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public ExchangeRateRepo exchangeRateRepo() {
        return new DummyExchangeRateRepo();
    }
}
