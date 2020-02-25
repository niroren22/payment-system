package com.niroren.paymentservice.services;

import com.niroren.common.serdes.*;
import com.niroren.paymentservice.dto.Payment;
import com.niroren.paymentservice.dto.PaymentMethod;
import com.niroren.paymentservice.dto.User;
import com.niroren.paymentservice.dto.ValidatedPayment;
import com.niroren.paymentservice.properties.Configuration;
import com.niroren.paymentservice.utils.MockDataReader;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@Service
public class PaymentService implements IPaymentsService {
    private static Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final static String VALIDATED_PAYMENTS_STORE_NAME = "validated-payments-store";

    private List<BiConsumer<String, ValidatedPayment>> validationListeners = new ArrayList<>();
    private List<String> currencies;
    private KafkaStreams streams;

    @Autowired
    private Configuration configuration;

    @Autowired
    private UsersService usersService;

    @Autowired
    private PaymentsProducer paymentsProducer;

    @PostConstruct
    private void init() {
        this.currencies = Collections.unmodifiableList(MockDataReader.readModelEntities(configuration.getMockDataConfig().getCurrencyCodesFilePath(), String.class));
        this.streams = new KafkaStreams(
                createPaymentsView().build(),
                getStreamProperties(createTempStateDirectory()));

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

        logger.info("Payment service was started successfully.");

        logger.info("Streams after init: " + ((streams != null) ? streams.toString() : null));


    }

    public KafkaStreams getStreams() {
        return streams;
    }

    @Override
    public void start() {
        this.streams = new KafkaStreams(
                createPaymentsView().build(),
                getStreamProperties(createTempStateDirectory()));

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

        logger.info("Payment service was started successfully.");

        logger.info("Streams after init: " + ((streams != null) ? streams.toString() : null));
    }

    @Override
    public void stop() {
        logger.info("Payment service was shut down.");
        if (streams != null) {
            streams.close();
        }
    }

    @Override
    public void submitPayment(Payment payment, Callback callback) {
        validatePayment(payment);
        paymentsProducer.sendPayment(payment, callback);
    }

    @Override
    public ValidatedPayment retrievePayment(String paymentId) {
        logger.info("Streams in retrieve payment: " + ((streams != null) ? streams.toString() : null));
        return getPaymentStore().get(paymentId);
    }

    @Override
    public void registerValidationListener(BiConsumer<String, ValidatedPayment> onValidationListener) {
        validationListeners.add(onValidationListener);
    }

    private String createTempStateDirectory() {
        try {
            return Files.createTempDirectory("payment-service").toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create state-store temp directory, reason: " + e.getMessage(), e);
        }
    }

    private StreamsBuilder createPaymentsView() {
        final StreamsBuilder builder = new StreamsBuilder();
        //Predicate<String, Payment> isValidated = (id, pmt) -> !ValidationResult.PENDING.equals(pmt.getValidationResult());
        builder.table("validated-payments", Consumed.with(Serdes.String(), new ValidatedPaymentSerde()), Materialized.as(VALIDATED_PAYMENTS_STORE_NAME))
               //.filter(isValidated)
               .toStream()
               .foreach(this::executeValidationListeners);

        return builder;
    }

    private Properties getStreamProperties(String stateDir) {
        final Properties streamConfig = new Properties();
        streamConfig.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "payment-service");
        streamConfig.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        streamConfig.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1);
        streamConfig.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);

        return streamConfig;
    }

    private void executeValidationListeners(String id, ValidatedPayment payment) {
        validationListeners.forEach(vl -> vl.accept(id, payment));
    }

    private ReadOnlyKeyValueStore<String, ValidatedPayment> getPaymentStore() {
        return streams.store(VALIDATED_PAYMENTS_STORE_NAME, QueryableStoreTypes.keyValueStore());
    }

    private void validatePayment(Payment payment) {
        if (payment.getAmount() <= 0) {
            throw new NotAcceptableException("Payed amount must be positive.");
        }

        if (!currencies.contains(payment.getCurrency())) {
            throw new NotAcceptableException("Currency code: " + payment.getCurrency() + " is not recognized.");
        }

        if (!usersService.getUserById(payment.getPayeeId()).isPresent()) {
            throw new NotFoundException("Payee user-Id: " + payment.getPayeeId() + " was not found");
        }

        Optional<User> optPayerUser = usersService.getUserById(payment.getUserId());
        if (!optPayerUser.isPresent()) {
            throw new NotFoundException("Payer user-Id: " + payment.getUserId() + " was not found");
        } else {
            User payer = optPayerUser.get();
            List<PaymentMethod> payerPaymentMethods = usersService.getUserPaymentMethods(payer.getUserId());

            boolean validPaymentMethod = payerPaymentMethods.stream()
                    .map(PaymentMethod::getPaymentMethodId)
                    .anyMatch(pm -> payment.getPaymentMethodId().equals(pm));

            if (!validPaymentMethod) {
                throw new NotFoundException("Payment method id: " + payment.getPaymentMethodId() + " was not found");
            }
        }
    }
}
