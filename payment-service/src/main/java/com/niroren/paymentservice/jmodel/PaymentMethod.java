package com.niroren.paymentservice.jmodel;

import java.util.Objects;

public class PaymentMethod {
    private String paymentMethodId;
    private PaymentMethodType type;
    private String name;
    private Integer last4;
    private String userId;

    public PaymentMethod(String paymentMethodId, PaymentMethodType type, String name, Integer last4, String userId) {
        this.paymentMethodId = paymentMethodId;
        this.type = type;
        this.name = name;
        this.last4 = last4;
        this.userId = userId;
    }

    // For serialization
    public PaymentMethod() {}

    enum PaymentMethodType {
        BANK_ACCOUNT, CREDIT_CARD
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public PaymentMethodType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Integer getLast4() {
        return last4;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentMethod that = (PaymentMethod) o;
        return paymentMethodId.equals(that.paymentMethodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentMethodId);
    }
}
