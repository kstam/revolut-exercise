package com.revolut.interview.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan(basePackages = { "com.revolut.interview.controllers" })
public class SpringRootConfig {

}
