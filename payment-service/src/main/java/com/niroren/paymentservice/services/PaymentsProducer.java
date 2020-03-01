package com.niroren.paymentservice.services;

import com.niroren.common.serdes.PaymentSerde;
import com.niroren.paymentservice.dto.Payment;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class PaymentsProducer {

    @Value("${kafka.payments.topic}")
    private String paymentsTopic;

    private final KafkaProducer<String, Payment> producer;

    @Autowired
    public PaymentsProducer(@Value("${kafka.payments.producer.client.id}")String producerClientId,
                            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {

        final Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerConfig.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, String.valueOf(Integer.MAX_VALUE));
        producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        producerConfig.put(ProducerConfig.CLIENT_ID_CONFIG, producerClientId);

        this.producer = new KafkaProducer<>(producerConfig, Serdes.String().serializer(), new PaymentSerde().serializer());
    }

    public void sendPayment(Payment payment, Callback callback) {
        producer.send(new ProducerRecord<>(paymentsTopic, payment.getPaymentId(), payment), callback);
    }

}
