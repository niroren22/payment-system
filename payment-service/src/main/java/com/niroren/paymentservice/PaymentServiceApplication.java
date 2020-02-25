package com.niroren.paymentservice;

import com.niroren.common.services.IStreamService;
import com.niroren.paymentservice.services.PaymentService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application.properties")
@ConfigurationPropertiesScan("com.niroren.paymentservice.properties")
public class PaymentServiceApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(PaymentServiceApplication.class, args);
	}


	private static void addShutdownHookAndBlock(final IStreamService service) throws InterruptedException {
		Thread.currentThread().setUncaughtExceptionHandler((t, e) -> service.stop());
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				service.stop();
			} catch (final Exception ignored) {
			}
		}));
		Thread.currentThread().join();
	}

}
