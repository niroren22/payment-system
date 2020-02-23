package com.niroren.paymentservice.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "mock-data")
@ConstructorBinding
public class MockDataConfig {
    private final String currencyCodesFilePath;
    private final String usersFilePath;
    private final String paymentMethodsFilePath;

    public MockDataConfig(String currencyCodesFilePath, String usersFilePath, String paymentMethodsFilePath) {
        this.currencyCodesFilePath = currencyCodesFilePath;
        this.usersFilePath = usersFilePath;
        this.paymentMethodsFilePath = paymentMethodsFilePath;
    }

    public String getCurrencyCodesFilePath() {
        return currencyCodesFilePath;
    }

    public String getUsersFilePath() {
        return usersFilePath;
    }

    public String getPaymentMethodsFilePath() {
        return paymentMethodsFilePath;
    }
}
