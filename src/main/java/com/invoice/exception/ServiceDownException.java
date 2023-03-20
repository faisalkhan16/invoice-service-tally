package com.invoice.exception;

public class ServiceDownException extends RuntimeException{
    public ServiceDownException(String message) {
        super(message);
    }
}
