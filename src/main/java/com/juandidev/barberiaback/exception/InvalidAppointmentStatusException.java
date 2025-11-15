package com.juandidev.barberiaback.exception;

import com.juandidev.barberiaback.model.AppointmentStatus;

public class InvalidAppointmentStatusException extends RuntimeException {
    
    public InvalidAppointmentStatusException(String message) {
        super(message);
    }
    
    public InvalidAppointmentStatusException(Long appointmentId, AppointmentStatus currentStatus, String operation) {
        super(String.format("No se puede %s la cita con ID %d. Estado actual: %s", 
                operation, appointmentId, currentStatus.getDisplayName()));
    }
    
    public InvalidAppointmentStatusException(Long appointmentId, AppointmentStatus currentStatus, 
                                           AppointmentStatus requiredStatus, String operation) {
        super(String.format("No se puede %s la cita con ID %d. Estado actual: %s, se requiere: %s", 
                operation, appointmentId, currentStatus.getDisplayName(), requiredStatus.getDisplayName()));
    }
}
