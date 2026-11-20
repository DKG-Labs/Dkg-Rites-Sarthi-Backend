package com.sarthi.bookset;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.Map;

/** Thrown when a Book No + Set No is already used on a different IC. Controllers turn it into HTTP 409. */
public class BookSetInUseException extends RuntimeException {

    public BookSetInUseException(String message) {
        super(message);
    }

    /** Same body shape for every product so each frontend can read "message". */
    public ResponseEntity<Object> toResponse() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("statusCode", 409);
        status.put("message", getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", getMessage());
        body.put("error", getMessage());
        body.put("responseStatus", status);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
