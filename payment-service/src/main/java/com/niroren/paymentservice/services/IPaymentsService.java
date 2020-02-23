package com.niroren.paymentservice.services;

import com.niroren.common.services.IStreamService;
import com.niroren.paymentservice.dto.Payment;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.streams.KafkaStreams;

import java.util.function.BiConsumer;

public interface IPaymentsService extends IStreamService {

    void submitPayment(Payment payment, Callback onSubmit);

    Payment retrievePayment(String paymentId);

    void registerValidationListener(BiConsumer<String, Payment> onValidationListener);

    KafkaStreams getStreams();

}
