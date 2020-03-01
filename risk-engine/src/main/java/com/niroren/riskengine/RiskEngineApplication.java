package com.niroren.riskengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class RiskEngineApplication {

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(RiskEngineApplication.class, args);

		// We need to start the service only after application context initialization is complete.
		RiskEngineService service = context.getBean(RiskEngineService.class);
		service.start();
	}
}
