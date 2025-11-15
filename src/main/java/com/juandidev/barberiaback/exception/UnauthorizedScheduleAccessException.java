package com.juandidev.barberiaback.exception;

public class UnauthorizedScheduleAccessException extends RuntimeException {
    
    public UnauthorizedScheduleAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedScheduleAccessException(Long scheduleId, Long userId, String operation) {
        super(String.format("El usuario con ID %d no tiene permisos para %s el horario con ID %d", 
                userId, operation, scheduleId));
    }
    
    public UnauthorizedScheduleAccessException(Long barberId, Long userId) {
        super(String.format("El usuario con ID %d no tiene permisos para gestionar los horarios del barbero con ID %d", 
                userId, barberId));
    }
}
