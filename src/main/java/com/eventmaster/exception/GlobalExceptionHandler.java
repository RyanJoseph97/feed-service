package com.eventmaster.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Bad request: {}", ex.getMessage());
        return errorBody(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Object> handleUpstreamFailure(RestClientException ex) {
        logger.error("Upstream service call failed: {}", ex.getMessage());
        return errorBody(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", "An upstream service is unavailable");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobal(Exception ex) {
        logger.error("Unexpected error", ex);
        return errorBody(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Unexpected error");
    }

    private ResponseEntity<Object> errorBody(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}
