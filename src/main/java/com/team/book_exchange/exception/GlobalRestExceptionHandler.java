package com.team.book_exchange.exception;

import com.team.book_exchange.dto.api.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalRestExceptionHandler {

   @ExceptionHandler(ResourceNotFoundException.class)
   public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
       return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
   }

   @ExceptionHandler({
           CategoryAlreadyExistsException.class,
           EmailAlreadyExistsException.class
   })
   public ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest request) {
       return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request, null);
   }

   @ExceptionHandler({
           InvalidBookListingException.class,
           RequestNotAllowedException.class,
           SellerApplicationNotAllowedException.class
   })
   public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
       return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
   }

   @ExceptionHandler(MethodArgumentNotValidException.class)
   public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
       Map<String, String> validationErrors = new LinkedHashMap<>();

       for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
           validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
       }

       return buildErrorResponse(
               HttpStatus.BAD_REQUEST,
               "Validation failed.",
               request,
               validationErrors
       );
   }

   @ExceptionHandler(AccessDeniedException.class)
   public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
       return buildErrorResponse(HttpStatus.FORBIDDEN, "Access is denied.", request, null);
   }

   @ExceptionHandler(Exception.class)
   public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
       return buildErrorResponse(
               HttpStatus.INTERNAL_SERVER_ERROR,
               "An unexpected server error occurred.",
               request,
               null
       );
   }

   private ResponseEntity<ApiErrorResponse> buildErrorResponse(
           HttpStatus status,
           String message,
           HttpServletRequest request,
           Map<String, String> validationErrors
   ) {
       ApiErrorResponse response = ApiErrorResponse.builder()
               .timestamp(LocalDateTime.now())
               .status(status.value())
               .error(status.getReasonPhrase())
               .message(message)
               .path(request.getRequestURI())
               .validationErrors(validationErrors)
               .build();

       return ResponseEntity.status(status).body(response);
   }
}
