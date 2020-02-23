package com.niroren.riskengine;

import com.niroren.common.services.IStreamService;
import com.niroren.common.serdes.*;
import com.niroren.paymentservice.dto.Payment;
import com.niroren.paymentservice.dto.ValidationResult;
import com.niroren.riskengine.processors.PaymentProcessor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RiskEngineService implements IStreamService {
    private static final Logger logger = LoggerFactory.getLogger(RiskEngineService.class);

    private KafkaStreams streams;

    @Override
    public void start() {
        streams = processStreams();
        streams.cleanUp();

        final CountDownLatch startLatch = new CountDownLatch(1);
        streams.setStateListener((newState, oldState) -> {
            if (newState == KafkaStreams.State.RUNNING && oldState != KafkaStreams.State.RUNNING) {
                startLatch.countDown();
            }
        });

        streams.start();

        try {
            if (!startLatch.await(60, TimeUnit.SECONDS)) {
                throw new RuntimeException("Streams never finished rebalancing on startup");
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logger.info("Risk Engine service was started successfully.");
    }

    private KafkaStreams processStreams() {
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, Payment> payments = builder
                .stream("payments", Consumed.with(Serdes.String(), new PaymentSerde()))
                .filter((id,payment) -> ValidationResult.PENDING.equals(payment.getValidationResult()));

        payments.print(Printed.toSysOut());
        payments.process(PaymentProcessor::new);

        return new KafkaStreams(builder.build(), getStreamsConfiguration());
    }

    private static Properties getStreamsConfiguration() {
        final Properties streamConfig = new Properties();
        streamConfig.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "risk-engine");
        streamConfig.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        streamConfig.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1);
        return streamConfig;
    }

    @Override
    public void stop() {
        logger.info("Risk Engine service was shut down.");
        if (streams != null) {
            streams.close();
        }
    }

}
