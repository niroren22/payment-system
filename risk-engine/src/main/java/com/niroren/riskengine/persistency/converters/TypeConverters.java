package com.niroren.riskengine.persistency.converters;

import com.niroren.paymentservice.dto.ValidationResult;
import org.jooq.impl.EnumConverter;

public class TypeConverters {

    public static class ValidationResultConverter extends EnumConverter<String, ValidationResult> {
        public ValidationResultConverter() {
            super(String.class, ValidationResult.class);
        }
    }

}
