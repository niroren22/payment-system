package com.niroren.paymentservice.rest;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.niroren.paymentservice.dto.Payment;
import com.niroren.paymentservice.jmodel.PaymentRequest;
import com.niroren.paymentservice.services.IPaymentsService;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.glassfish.jersey.server.ManagedAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

//@Component
@Path("/payments")
public class PaymentsResource {
    private static Logger logger = LoggerFactory.getLogger(PaymentsResource.class);
    private static final String PAYMENT_TIMEOUT = "10000"; // 10 seconds

    @Autowired
    private PaymentsAsyncMediator paymentService;

    @GET
    @Path("welcome")
    @Produces(MediaType.TEXT_HTML)
    public Response getWelcomeMessage() {
        paymentService.printStreams();
        return Response.ok("Welcome to REST payment system!").build();
    }

    @POST
    @ManagedAsync
    @Consumes(MediaType.APPLICATION_JSON)
    public void createPayment(final PaymentRequest request,
                              @QueryParam("timeout") @DefaultValue(PAYMENT_TIMEOUT) final Long timeout,
                              @Suspended final AsyncResponse asyncResponse) {
        setTimeout(timeout, asyncResponse);
        Payment payment = request.toDto();
        paymentService.submitPaymentAsync(payment, asyncResponse);
    }

    @GET
    @ManagedAsync
    @Path("{id}")
    public void getValidatedPayment(@PathParam("id") final String id,
                                    @QueryParam("timeout") @DefaultValue(PAYMENT_TIMEOUT) final Long timeout,
                                    @Suspended final AsyncResponse asyncResponse) {
        setTimeout(timeout, asyncResponse);
        logger.info("Running GET on payment id: " + id);

        paymentService.retrievePaymentAsync(id, asyncResponse);
    }

    private static void setTimeout(long timeout, AsyncResponse asyncResponse) {
        asyncResponse.setTimeout(timeout, TimeUnit.MILLISECONDS);
        asyncResponse.setTimeoutHandler(resp -> resp.resume(
                new ServiceUnavailableException("Payment request timed out after " + timeout + " ms")));
    }
}
