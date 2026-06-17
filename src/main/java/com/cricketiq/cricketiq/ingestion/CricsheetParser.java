package com.cricketiq.cricketiq.ingestion;

import java.io.File;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@Component
public class CricsheetParser {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public CricsheetMatch parse(File file) {
        try {
            return objectMapper.readValue(file, CricsheetMatch.class);
        } catch (Exception e) {
            throw new IngestionException("Failed to parse Cricsheet file: " + file.getName(), e);
        }
    }
}
