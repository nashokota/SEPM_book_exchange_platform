package com.team.book_exchange.exception;

public class SellerApplicationNotAllowedException extends RuntimeException {

    public SellerApplicationNotAllowedException(String message) {
        super(message);
    }
}
