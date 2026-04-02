package com.team.book_exchange.exception;

public class InvalidBookListingException extends RuntimeException {

    public InvalidBookListingException(String message) {
        super(message);
    }
}
