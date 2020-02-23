package com.niroren.paymentservice.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "server")
@ConstructorBinding
public class ServerConfig {
    private final String url;

    public ServerConfig(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
