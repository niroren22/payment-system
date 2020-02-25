package com.niroren.riskengine.processors;

import com.niroren.paymentservice.dto.ValidatedPayment;
import com.niroren.riskengine.persistency.dao.PaymentsDAO;
import com.niroren.riskengine.utils.ContextHolder;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentProcessor implements Processor<String, ValidatedPayment> {
    private static final Logger logger = LoggerFactory.getLogger(PaymentProcessor.class);

    private ProcessorContext context;
    private PaymentsDAO dao;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        this.dao = ContextHolder.getBean(PaymentsDAO.class);
    }

    @Override
    public void process(String key, ValidatedPayment value) {
        logger.info("Payment: " + key + " was: " + value.getValidation().name().toLowerCase());
        dao.insertPayment(value);
    }

    @Override
    public void close() {

    }
}
