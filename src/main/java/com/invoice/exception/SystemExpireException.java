package com.invoice.exception;

public class SystemExpireException extends RuntimeException{
    public SystemExpireException(String message) {
        super(message);
    }
}
