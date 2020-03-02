package com.niroren.paymentservice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niroren.paymentservice.exceptions.AppExceptionsMapper;
import com.niroren.paymentservice.rest.PaymentsResource;
import com.niroren.paymentservice.rest.UsersResource;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificRecordBase;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Component
public class JerseyAppConfig extends ResourceConfig {

    public JerseyAppConfig() {
        super(JacksonFeature.class, AvroMapperProvider.class);

        // register jax-rs resource packages
        register(PaymentsResource.class);

        register(UsersResource.class);

        register(AppExceptionsMapper.class);
    }

    @Provider
    public static class AvroMapperProvider implements ContextResolver<ObjectMapper> {

        @Override
        public ObjectMapper getContext(Class<?> type) {
            return new ObjectMapper().addMixIn(SpecificRecordBase.class, AvroJsonMixin.class);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    abstract static class AvroJsonMixin {

        @JsonIgnore
        abstract Schema getSchema();

        @JsonIgnore
        abstract SpecificData getSpecificData();

    }
}
