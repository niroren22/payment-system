package com.niroren.riskengine.persistency.dao;

import com.niroren.paymentservice.dto.Payment;
import com.niroren.riskengine.model.Tables;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentsDAO {

    @Autowired
    private DSLContext dslContext;

    @Transactional
    public void insertPayment(Payment payment) {
        dslContext.insertInto(Tables.PAYMENTS)
                .set(Tables.PAYMENTS.PAYMENT_ID, payment.getPaymentId())
                .set(Tables.PAYMENTS.FROM_USER_ID, payment.getUserId())
                .set(Tables.PAYMENTS.TO_USER_ID, payment.getPayeeId())
                .set(Tables.PAYMENTS.AMOUNT, new BigDecimal(payment.getAmount()))
                .set(Tables.PAYMENTS.CURRENCY, payment.getCurrency())
                .set(Tables.PAYMENTS.PAYMENT_METHOD_ID, payment.getPaymentMethodId())
                .set(Tables.PAYMENTS.VALIDATION_RESULT, payment.getValidationResult())
                .execute();
    }


}
