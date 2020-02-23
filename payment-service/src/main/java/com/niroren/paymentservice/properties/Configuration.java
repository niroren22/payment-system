package com.niroren.paymentservice.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Configuration {

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private MockDataConfig mockDataConfig;

    public KafkaConfig getKafkaConfig() {
        return kafkaConfig;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public MockDataConfig getMockDataConfig() {
        return mockDataConfig;
    }
}
