package com.niroren.paymentservice.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "kafka")
@ConstructorBinding
public class KafkaConfig {
    private final String bootstrapServers;
    private final String paymentsTopic;
    private final String paymentsProducerId;
    private final String streamsApplicationId;

    public KafkaConfig(String bootstrapServers, String paymentsTopic, String paymentsProducerId, String streamsApplicationId) {
        this.bootstrapServers = bootstrapServers;
        this.paymentsTopic = paymentsTopic;
        this.paymentsProducerId = paymentsProducerId;
        this.streamsApplicationId = streamsApplicationId;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getPaymentsTopic() {
        return paymentsTopic;
    }

    public String getPaymentsProducerId() {
        return paymentsProducerId;
    }

    public String getStreamsApplicationId() {
        return streamsApplicationId;
    }
}
