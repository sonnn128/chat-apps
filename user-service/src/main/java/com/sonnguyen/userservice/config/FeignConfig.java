package com.sonnguyen.userservice.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                5000, // connect timeout
                10000, // read timeout
                true // follow redirects
        );
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
                1000, // period
                3000, // max period
                3 // max attempts
        );
    }
}
