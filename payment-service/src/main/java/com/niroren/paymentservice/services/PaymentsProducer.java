package com.niroren.paymentservice.services;

import com.niroren.common.serdes.*;
import com.niroren.paymentservice.dto.Payment;
import com.niroren.paymentservice.properties.Configuration;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class PaymentsProducer {
    private final KafkaProducer<String, Payment> producer;
    private final String topic;

    @Autowired
    public PaymentsProducer(Configuration config) {
        final Properties producerConfig =new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaConfig().getBootstrapServers());
        producerConfig.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, String.valueOf(Integer.MAX_VALUE));
        producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        producerConfig.put(ProducerConfig.CLIENT_ID_CONFIG, config.getKafkaConfig().getPaymentsProducerId());

        //Serde<Payment> paymentSerde = new SpecificAvroSerde<>();
        //paymentSerde.configure(Collections.singletonMap(SCHEMA_REGISTRY_URL_CONFIG, url), false);

        this.producer = new KafkaProducer<>(producerConfig, Serdes.String().serializer(), new PaymentSerde().serializer());
        this.topic = config.getKafkaConfig().getPaymentsTopic();
    }

    public void sendPayment(Payment payment, Callback callback) {
        producer.send(new ProducerRecord<>(topic, payment.getPaymentId(), payment), callback);
    }

}
