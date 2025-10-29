package com.skillverse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.skillverse.dto.MessageResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        ex.printStackTrace(); // Or use logger
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error occurred: " + ex.getMessage());
    }

    @ExceptionHandler(CreatorProfileNotFoundException.class)
    public ResponseEntity<MessageResponse> handleCreatorProfileNotFound(CreatorProfileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse(ex.getMessage()));
    }
}
