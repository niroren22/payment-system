package com.niroren.riskengine.persistency.dao;

import com.niroren.paymentservice.dto.Payment;
import com.niroren.paymentservice.dto.ValidatedPayment;
import com.niroren.riskengine.model.Tables;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentsDAO {
    private static final Logger logger = LoggerFactory.getLogger(PaymentsDAO.class);

    @Autowired
    private DSLContext dslContext;

    @Transactional
    public void insertPayment(ValidatedPayment validatedPayment) {
        Payment payment = validatedPayment.getPayment();

        try {
            dslContext.insertInto(Tables.PAYMENTS)
                    .set(Tables.PAYMENTS.PAYMENT_ID, payment.getPaymentId())
                    .set(Tables.PAYMENTS.FROM_USER_ID, payment.getUserId())
                    .set(Tables.PAYMENTS.TO_USER_ID, payment.getPayeeId())
                    .set(Tables.PAYMENTS.AMOUNT, new BigDecimal(payment.getAmount()))
                    .set(Tables.PAYMENTS.CURRENCY, payment.getCurrency())
                    .set(Tables.PAYMENTS.PAYMENT_METHOD_ID, payment.getPaymentMethodId())
                    .set(Tables.PAYMENTS.VALIDATION_RESULT, validatedPayment.getValidation())
                    .execute();
        } catch (Exception e) {
            logger.error("Failed to persist payment: " + payment.getPaymentId() + ". Reason: " + e.getMessage(), e);
        }
    }
}
