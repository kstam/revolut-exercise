package com.revolut.interview.controllers;

import com.revolut.interview.repos.inmemory.InMemoryReposConfiguration;
import com.revolut.interview.services.SpringServiceConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ InMemoryReposConfiguration.class, SpringServiceConfiguration.class})
public class ControllerConfiguration {
}
