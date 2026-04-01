package com.team.book_exchange.exception;

public class EmailAlreadyExistsException extends RuntimeException {

   public EmailAlreadyExistsException(String message) {
       super(message);
   }
}
