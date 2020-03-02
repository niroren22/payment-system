package com.niroren.paymentservice.services;

import com.niroren.paymentservice.dto.PaymentMethod;
import com.niroren.paymentservice.dto.User;
import com.niroren.paymentservice.utils.MockDataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UsersService implements IUsersService {
    private static Logger logger = LoggerFactory.getLogger(UsersService.class);

    @Value("${mock-data.users.file-path}")
    private String usersMockDataFilePath;

    @Value("${mock-data.payment-methods.file-path}")
    private String paymentMethodsMockDataFilePath;

    private Map<String, User> users;
    private Map<String, List<PaymentMethod>> paymentMethods;

    @PostConstruct
    private void init() {
        this.users = MockDataReader.readModelEntities(usersMockDataFilePath, User.class)
                .stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        this.paymentMethods = MockDataReader.readModelEntities(paymentMethodsMockDataFilePath, PaymentMethod.class)
                .stream()
                .collect(Collectors.groupingBy(PaymentMethod::getUserId));
    }


    @Override
    public Optional<User> getUserById(String userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return users.values().stream().filter(u -> email.equals(u.getEmail())).findAny();
    }

    @Override
    public Collection<User> getAllUsers() {
        //TODO niroren - add paging
        return users.values();
    }

    @Override
    public List<PaymentMethod> getUserPaymentMethods(String userId) {
        return paymentMethods.get(userId);
    }
}
