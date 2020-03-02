package com.niroren.riskengine;

import com.niroren.common.Topics;
import com.niroren.common.serdes.PaymentSerde;
import com.niroren.common.serdes.ValidatedPaymentSerde;
import com.niroren.common.services.BaseStreamService;
import com.niroren.paymentservice.dto.ValidatedPayment;
import com.niroren.paymentservice.dto.ValidationResult;
import com.niroren.riskengine.processors.PaymentProcessor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class RiskEngineService extends BaseStreamService {
    private static final Logger logger = LoggerFactory.getLogger(RiskEngineService.class);

    @Autowired
    public RiskEngineService(@Value("${spring.kafka.application.id}") String appId,
                             @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        super(appId, bootstrapServers);
    }

    @Override
    public void start() {
        super.start();
        logger.info("Risk Engine service was started successfully.");
    }

    @Override
    public KafkaStreams processStreams(Properties streamsConfig) {
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, ValidatedPayment> validated = builder
                .stream(Topics.getPaymentsTopic(), Consumed.with(Serdes.String(), new PaymentSerde()))
                .mapValues(pmt -> ValidatedPayment.newBuilder()
                        .setPayment(pmt)
                        .setValidation(performRiskAssessment())
                        .build());

        validated.print(Printed.toSysOut());
        validated.process(PaymentProcessor::new);

        validated.to(Topics.getPaymentsValidationTopic(), Produced.with(Serdes.String(), new ValidatedPaymentSerde()));

        return new KafkaStreams(builder.build(), streamsConfig);
    }

    private static ValidationResult performRiskAssessment() {
        double rand = Math.random();
        return rand < 0.7d ? ValidationResult.AUTHORIZED : ValidationResult.REJECTED;
    }
}
