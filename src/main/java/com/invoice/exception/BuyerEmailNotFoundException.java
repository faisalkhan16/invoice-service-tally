package com.invoice.exception;

public class BuyerEmailNotFoundException extends RuntimeException{
    public BuyerEmailNotFoundException(String message) {
        super(message);
    }
}
