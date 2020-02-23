package com.niroren.paymentservice.rest;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.niroren.paymentservice.dto.Payment;
import com.niroren.paymentservice.services.IPaymentsService;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

@Component
public class PaymentsAsyncMediator {
    private static Logger logger = LoggerFactory.getLogger(PaymentsAsyncMediator.class);
    private final static int MAX_OUTSTANDING_REQUESTS = 100;

    private Cache<String, AsyncResponse> outstandingRequests;

    @Autowired
    private IPaymentsService service;

    public PaymentsAsyncMediator() {
        this.outstandingRequests = CacheBuilder.newBuilder()
                .maximumSize(MAX_OUTSTANDING_REQUESTS)
                .expireAfterAccess(Duration.ofMillis(10))
                .build();
    }

    @PostConstruct
    private void init() {
        service.registerValidationListener(this::tryCompleteOutstandingRequest);

        logger.info("Streams in init mediator: " + ((service.getStreams() != null) ? service.getStreams().toString() : null));
    }

    private void tryCompleteOutstandingRequest(String id, Payment payment) {
        AsyncResponse suspended = outstandingRequests.getIfPresent(id);
        if (suspended != null) {
            suspended.resume(payment);
        }
    }

    public void printStreams() {
        logger.info("Streams in printSteams: " + ((service.getStreams() != null) ? service.getStreams().toString() : null));
    }

    public void submitPaymentAsync(Payment payment, AsyncResponse asyncResponse) {
        service.submitPayment(payment, callback(asyncResponse, payment));
    }

    public void retrievePaymentAsync(String paymentId, AsyncResponse asyncResponse) {
        try {
            final Payment payment = service.retrievePayment(paymentId);
            if (payment == null) {
                logger.info("Suspending GET as payment is not present for id " + paymentId);
                outstandingRequests.put(paymentId, asyncResponse);
            } else {
                logger.info("Payment " + paymentId + " was found in store.");
                asyncResponse.resume(payment);
            }
        } catch (InvalidStateStoreException e) {
            // Store not ready yet, suspending
            outstandingRequests.put(paymentId, asyncResponse);
        }
    }

    private Callback callback(AsyncResponse response, Payment payment) {
        return ((recordMetadata, e) -> {
            if (e != null) {
                response.resume(e);
            } else {
                logger.info("Payment request submitted: " + payment.toString());

                try {
                    URI uri =new URI("/payments/" + payment.getPaymentId());
                    response.resume(Response.accepted(uri).build());
                } catch (URISyntaxException urie) {
                    logger.error("Error building payment url, reason: " + urie.getMessage(), urie);
                    response.resume(urie);
                }
            }
        });
    }
}
