package com.niroren.paymentservice.services;

import com.niroren.paymentservice.dto.PaymentMethod;
import com.niroren.paymentservice.dto.User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface IUsersService {

    Optional<User> getUserById(String userId);

    Optional<User> getUserByEmail(String email);

    Collection<User> getAllUsers();

    List<PaymentMethod> getUserPaymentMethods(String userId);

}
