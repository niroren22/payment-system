package com.niroren.paymentservice.services;

import com.niroren.common.services.IStreamService;
import com.niroren.paymentservice.dto.Payment;
import com.niroren.paymentservice.dto.ValidatedPayment;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.streams.KafkaStreams;

import java.util.function.BiConsumer;

public interface IPaymentsService extends IStreamService {

    void submitPayment(Payment payment, Callback onSubmit);

    ValidatedPayment retrievePayment(String paymentId);

    void registerValidationListener(BiConsumer<String, ValidatedPayment> onValidationListener);

    KafkaStreams getStreams();

}
