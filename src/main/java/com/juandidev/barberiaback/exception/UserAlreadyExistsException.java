package com.juandidev.barberiaback.exception;

public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
    public UserAlreadyExistsException(String field, String value) {
        super(String.format("Ya existe un usuario con %s: %s", field, value));
    }
}
