package com.niroren.common.serdes;

import com.niroren.paymentservice.dto.Payment;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.DeserializationException;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PaymentSerde implements Serde<Payment> {

    @Override
    public Serializer<Payment> serializer() {
        return new Serializer<Payment>() {
            @Override
            public byte[] serialize(String topic, Payment payment) {
                try {
                    return payment.toByteBuffer().array();
                } catch (IOException e) {
                    throw new SerializationException("Failed to serialize payment, reason: " + e.getMessage(), e);
                }
            }
        };
    }

    @Override
    public Deserializer<Payment> deserializer() {
        return new Deserializer<Payment>() {
            @Override
            public Payment deserialize(String topic, byte[] bytes) {
                try {
                    return Payment.fromByteBuffer(ByteBuffer.wrap(bytes));
                } catch (IOException e) {
                    throw new DeserializationException("Failed to deserialize payment, reason: ", bytes, false, e);
                }
            }
        };
    }
}
