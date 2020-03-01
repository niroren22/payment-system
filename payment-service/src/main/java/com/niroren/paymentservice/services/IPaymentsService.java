package com.niroren.paymentservice.services;

import com.niroren.paymentservice.dto.Payment;
import com.niroren.paymentservice.dto.ValidatedPayment;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.streams.KafkaStreams;

import java.util.function.BiConsumer;

public interface IPaymentsService {

    void submitPayment(Payment payment, Callback onSubmit);

    ValidatedPayment retrievePayment(String paymentId);

    void registerValidatedListener(BiConsumer<String, ValidatedPayment> onValidationListener);

}
