package com.niroren.common.serdes;

import com.niroren.paymentservice.dto.ValidatedPayment;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.DeserializationException;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ValidatedPaymentSerde implements Serde<ValidatedPayment> {

    @Override
    public Serializer<ValidatedPayment> serializer() {
        return new Serializer<ValidatedPayment>() {
            @Override
            public byte[] serialize(String topic, ValidatedPayment validatedPayment) {
                try {
                    return validatedPayment.toByteBuffer().array();
                } catch (IOException e) {
                    throw new SerializationException("Failed to serialize validated payment, reason: " + e.getMessage(), e);
                }
            }
        };
    }

    @Override
    public Deserializer<ValidatedPayment> deserializer() {
        return new Deserializer<ValidatedPayment>() {
            @Override
            public ValidatedPayment deserialize(String topic, byte[] bytes) {
                try {
                    return ValidatedPayment.fromByteBuffer(ByteBuffer.wrap(bytes));
                } catch (IOException e) {
                    throw new DeserializationException("Failed to deserialize validated payment, reason: ", bytes, false, e);
                }
            }
        };
    }
}
