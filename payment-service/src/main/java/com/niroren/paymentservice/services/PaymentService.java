package com.niroren.paymentservice.services;

import com.niroren.common.serdes.ValidatedPaymentSerde;
import com.niroren.common.services.BaseStreamService;
import com.niroren.paymentservice.dto.Payment;
import com.niroren.paymentservice.dto.PaymentMethod;
import com.niroren.paymentservice.dto.User;
import com.niroren.paymentservice.dto.ValidatedPayment;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiConsumer;

@Service
public class PaymentService extends BaseStreamService implements IPaymentsService {
    private static Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private List<BiConsumer<String, ValidatedPayment>> validationListeners = new ArrayList<>();
    private List<String> currencies;

    @Autowired
    private UsersService usersService;

    @Autowired
    private PaymentsProducer paymentsProducer;

    @Value("${kafka.payment-validations.store.name}")
    private String validatedPaymentsStoreName;

    @Value("${mock-data.currency-codes.file-path")
    private String currencyCodesDataFilePath;

    @Value("${kafka.payment-validations.topic}")
    private String paymentsValidationTopic;

    @Autowired
    public PaymentService(@Value("${kafka.payments.streams.application.id}") String appId,
                          @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        super(appId, bootstrapServers);

    }

    @PostConstruct
    private void init() {
        this.currencies = Collections.unmodifiableList(MockDataReader.readModelEntities(currencyCodesDataFilePath, String.class));
        start();
        logger.info("Payment service was started successfully.");
    }

    @Override
    protected KafkaStreams processStreams(Properties streamsConfig) {
        return new KafkaStreams(createPaymentsView().build(), streamsConfig);
    }

    @Override
    protected Properties getStreamsConfig(String appId, String bootstrapServers) {
        final Properties streamConfig = super.getStreamsConfig(appId, bootstrapServers);
        streamConfig.put(StreamsConfig.STATE_DIR_CONFIG, createTempStateDirectory());
        return streamConfig;
    }

    @Override
    public void submitPayment(Payment payment, Callback callback) {
        validatePayment(payment);
        paymentsProducer.sendPayment(payment, callback);
    }

    @Override
    public ValidatedPayment retrievePayment(String paymentId) {
        return getPaymentStore().get(paymentId);
    }

    @Override
    public void registerValidatedListener(BiConsumer<String, ValidatedPayment> onValidationListener) {
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
        builder.table(paymentsValidationTopic, Consumed.with(Serdes.String(), new ValidatedPaymentSerde()), Materialized.as(validatedPaymentsStoreName))
               .toStream()
               .foreach(this::executeValidationListeners);

        return builder;
    }

    private void executeValidationListeners(String id, ValidatedPayment payment) {
        validationListeners.forEach(vl -> vl.accept(id, payment));
    }

    private ReadOnlyKeyValueStore<String, ValidatedPayment> getPaymentStore() {
        return streams.store(validatedPaymentsStoreName, QueryableStoreTypes.keyValueStore());
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
