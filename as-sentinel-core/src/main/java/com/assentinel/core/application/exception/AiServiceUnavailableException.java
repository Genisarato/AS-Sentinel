package com.assentinel.core.application.exception;

public class AiServiceUnavailableException extends RuntimeException {
    public AiServiceUnavailableException() {
        super("The AI service is unavailable");
    }
}
