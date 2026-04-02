package com.team.book_exchange.exception;

public class CategoryAlreadyExistsException extends RuntimeException {

   public CategoryAlreadyExistsException(String message) {
       super(message);
   }
}
