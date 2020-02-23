package com.niroren.paymentservice.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class MockDataReader {

    public static <T> List<T> readModelEntities(String modelEntitiesFilePath, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory typeFactory = mapper.getTypeFactory();

        try (InputStream inputStream = MockDataReader.class.getResourceAsStream(modelEntitiesFilePath)) {
            List<T> theList = mapper.readValue(inputStream, typeFactory.constructCollectionType(List.class, clazz));
            return Collections.unmodifiableList(theList);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load currencies file, reason: " + e.getMessage(), e);
        }
    }

}
