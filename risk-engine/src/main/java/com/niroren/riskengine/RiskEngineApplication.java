package com.niroren.riskengine;

import com.niroren.common.services.IStreamService;
import com.niroren.riskengine.utils.ContextHolder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Properties;

@SpringBootApplication
public class RiskEngineApplication {

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(RiskEngineApplication.class, args);

		IStreamService riskEngineService = new RiskEngineService();

		riskEngineService.start();

		addShutdownHookAndBlock(riskEngineService);
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
