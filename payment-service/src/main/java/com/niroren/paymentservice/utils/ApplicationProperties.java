package com.niroren.paymentservice.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

@PropertySource("classpath:application.properties")
public class ApplicationProperties {
    private static ApplicationProperties _instance = new ApplicationProperties();

    public final String bootstrapServers;



    public static ApplicationProperties getInstance() {
        return _instance;
    }

    @Autowired
    private Environment environment;

    private ApplicationProperties() {
        bootstrapServers = environment.getProperty("spring.kafka.producer.bootstrap-servers");

    }

}
