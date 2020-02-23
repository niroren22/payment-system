package com.niroren.paymentservice.rest;

import com.google.common.base.Strings;
import com.niroren.paymentservice.dto.PaymentMethod;
import com.niroren.paymentservice.dto.User;
import com.niroren.paymentservice.services.IUsersService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Path("/users")
public class UsersResource {

    @Autowired
    private IUsersService usersService;

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getById(@PathParam("id") String userId) {
        if (Strings.isNullOrEmpty(userId)) {
            throw new NotAcceptableException("must provide a user-id");
        }
        Optional<User> user = usersService.getUserById(userId);
        return user.orElseThrow(() -> new NotFoundException("user with id: " + userId + " was not found"));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<User> getAllUsers() {
        return usersService.getAllUsers();
    }

    @GET
    @Path("/{id}/payment-methods")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PaymentMethod> getUserPaymentMethods(@PathParam("id") String userId) {
        if (Strings.isNullOrEmpty(userId)) {
            throw new NotAcceptableException("must provide a user-id");
        }

        //TODO niroren - remove 'userId' from payment method returned.

        List<PaymentMethod> userPaymentMethods = usersService.getUserPaymentMethods(userId);
        if (userPaymentMethods == null) {
            throw new NotFoundException("user with id: " + userId + " was not found");
        }

        return userPaymentMethods;
    }
}
