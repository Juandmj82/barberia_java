package com.juandidev.barberiaback.exception;

public class EntityNotFoundException extends RuntimeException {
    
    public EntityNotFoundException(String message) {
        super(message);
    }
    
    public EntityNotFoundException(String entityName, Long id) {
        super(String.format("%s con ID %d no encontrado", entityName, id));
    }
    
    public EntityNotFoundException(String entityName, String field, Object value) {
        super(String.format("%s con %s '%s' no encontrado", entityName, field, value));
    }
}
