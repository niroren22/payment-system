package com.niroren.paymentservice.jmodel;

import com.niroren.paymentservice.dto.Payment;
import com.niroren.paymentservice.dto.ValidationResult;

import java.util.UUID;

public class PaymentRequest {

    public double amount;

    public String currency;

    public String userId;

    public String payeeId;

    public String paymentMethodId;

    public Payment toDto() {
        return Payment.newBuilder()
                .setAmount(this.amount)
                .setCurrency(this.currency)
                .setUserId(this.userId)
                .setPayeeId(this.payeeId)
                .setPaymentMethodId(this.paymentMethodId)
                .setPaymentId(UUID.randomUUID().toString())
                .build();
    }
}
