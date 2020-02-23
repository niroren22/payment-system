package com.niroren.paymentservice.services;

import com.niroren.paymentservice.dto.PaymentMethod;
import com.niroren.paymentservice.dto.User;
import com.niroren.paymentservice.properties.Configuration;
import com.niroren.paymentservice.utils.MockDataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@PropertySource("classpath:application.properties")
public class UsersService implements IUsersService {
    private static Logger logger = LoggerFactory.getLogger(UsersService.class);

    @Autowired
    private Configuration configuration;

    private Map<String, User> users;
    private Map<String, List<PaymentMethod>> paymentMethods;

    //private Map<User, List<PaymentMethod>> usersPaymentMethodsMap;

    @PostConstruct
    private void init() {
        this.users = MockDataReader.readModelEntities(configuration.getMockDataConfig().getUsersFilePath(), User.class).stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));
        this.paymentMethods = MockDataReader.readModelEntities(configuration.getMockDataConfig().getPaymentMethodsFilePath(), PaymentMethod.class).stream()
                .collect(Collectors.groupingBy(PaymentMethod::getUserId));

        /*this.usersPaymentMethodsMap = MockDataReader.readModelEntities(configuration.getMockDataConfig().getPaymentMethodsFilePath(), PaymentMethod.class).stream()
                .map(pm -> new ImmutablePair<>(usersById.get(pm.getUserId()), pm))
                .collect(Collectors.groupingBy(ImmutablePair::getLeft,
                         Collectors.mapping(ImmutablePair::getRight, Collectors.toList())));

        usersStore.forEach((id,user) -> user.setPaymentMethods(groupedPaymentMethods.get(id)));*/
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
