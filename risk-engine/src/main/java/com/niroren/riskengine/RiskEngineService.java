package com.niroren.riskengine;

import com.niroren.common.serdes.PaymentSerde;
import com.niroren.common.serdes.ValidatedPaymentSerde;
import com.niroren.common.services.IStreamService;
import com.niroren.paymentservice.dto.ValidatedPayment;
import com.niroren.paymentservice.dto.ValidationResult;
import com.niroren.riskengine.processors.PaymentProcessor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
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
        final KStream<String, ValidatedPayment> validated = builder
                .stream("payments", Consumed.with(Serdes.String(), new PaymentSerde()))
                .mapValues(pmt -> ValidatedPayment.newBuilder()
                        .setPayment(pmt)
                        .setValidation(performRiskAssessment())
                        .build());

        validated.print(Printed.toSysOut());
        validated.process(PaymentProcessor::new);

        validated.to("validated-payments", Produced.with(Serdes.String(), new ValidatedPaymentSerde()));

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

    private static ValidationResult performRiskAssessment() {
        double rand = Math.random();
        return rand < 0.7d ? ValidationResult.AUTHORIZED : ValidationResult.REJECTED;
    }

}
