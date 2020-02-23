package com.niroren.riskengine.processors;

import com.niroren.paymentservice.dto.Payment;
import com.niroren.paymentservice.dto.ValidationResult;
import com.niroren.riskengine.persistency.dao.PaymentsDAO;
import com.niroren.riskengine.utils.ContextHolder;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public class PaymentProcessor implements Processor<String, Payment> {
    private static final Logger logger = LoggerFactory.getLogger(PaymentProcessor.class);

    private ProcessorContext context;
    private PaymentsDAO dao;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        this.dao = ContextHolder.getBean(PaymentsDAO.class);
    }

    @Override
    public void process(String key, Payment value) {
        ValidationResult result = performRiskAssessment();
        logger.info("Payment: " + key + " was: " + result.name().toLowerCase());

        value.setValidationResult(result);
        dao.insertPayment(value);
    }

    @Override
    public void close() {

    }

    private static ValidationResult performRiskAssessment() {
        double rand = Math.random();
        return rand < 0.7d ? ValidationResult.AUTHORIZED : ValidationResult.REJECTED;
    }
}
