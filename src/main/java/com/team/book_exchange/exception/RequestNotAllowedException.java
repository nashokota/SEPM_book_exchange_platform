package com.team.book_exchange.exception;

public class RequestNotAllowedException extends RuntimeException {

    public RequestNotAllowedException(String message) {
        super(message);
    }
}
