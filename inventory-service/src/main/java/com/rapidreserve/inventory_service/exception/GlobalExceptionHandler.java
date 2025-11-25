package com.rapidreserve.inventory_service.exception;

import com.rapidreserve.inventory_service.exception.EventNotFoundException;
import com.rapidreserve.inventory_service.exception.InsufficientCapacityException;
import com.rapidreserve.inventory_service.exception.VenueNotFoundException;
import com.rapidreserve.inventory_service.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({EventNotFoundException.class, VenueNotFoundException.class})
    public ResponseEntity<ApiResponse<?>> handleNotFoundException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InsufficientCapacityException.class)
    public ResponseEntity<ApiResponse<?>> handleCapacityException(InsufficientCapacityException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }
}