package com.revolut.interview.controllers;

import com.revolut.interview.repos.inmemory.SpringReposConfiguration;
import com.revolut.interview.services.SpringServiceConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ SpringReposConfiguration.class, SpringServiceConfiguration.class})
public class ControllerConfiguration {
}
