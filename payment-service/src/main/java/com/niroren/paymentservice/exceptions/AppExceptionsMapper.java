package com.niroren.paymentservice.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AppExceptionsMapper implements ExceptionMapper<Throwable> {

    private static Logger logger = LoggerFactory.getLogger(AppExceptionsMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        logger.error("Excaption in Payment-Service: " + exception.getMessage(), exception);

        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(exception.getMessage());

        if (exception instanceof WebApplicationException) {
            WebApplicationException webAppException = (WebApplicationException) exception;
            errorMessage.setStatus(webAppException.getResponse().getStatus());
        } else {
            errorMessage.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        return Response.status(errorMessage.getStatus())
                .entity(errorMessage)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
