package com.niroren.common;

public class Topics {

    public static String getPaymentsTopic() {
        return System.getProperty("paymentTopic");
    }

    public static String getPaymentsValidationTopic() {
        return System.getProperty("paymentValidationTopic");
    }
}
