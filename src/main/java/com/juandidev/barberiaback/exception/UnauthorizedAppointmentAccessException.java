package com.juandidev.barberiaback.exception;

public class UnauthorizedAppointmentAccessException extends RuntimeException {
    
    public UnauthorizedAppointmentAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedAppointmentAccessException(Long appointmentId, Long userId, String operation) {
        super(String.format("El usuario con ID %d no tiene permisos para %s la cita con ID %d", 
                userId, operation, appointmentId));
    }
    
    public UnauthorizedAppointmentAccessException(Long appointmentId, String role, String operation) {
        super(String.format("El rol %s no tiene permisos para %s la cita con ID %d", 
                role, operation, appointmentId));
    }
}
