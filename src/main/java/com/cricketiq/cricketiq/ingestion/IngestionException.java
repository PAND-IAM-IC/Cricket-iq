package com.cricketiq.cricketiq.ingestion;

public class IngestionException extends RuntimeException{
    public IngestionException(String message, Throwable cause) {
        super(message, cause);
    }
}
