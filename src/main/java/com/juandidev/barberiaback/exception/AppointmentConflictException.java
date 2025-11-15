package com.juandidev.barberiaback.exception;

import java.time.LocalDateTime;

public class AppointmentConflictException extends RuntimeException {
    
    public AppointmentConflictException(String message) {
        super(message);
    }
    
    public AppointmentConflictException(Long barberId, LocalDateTime startTime, LocalDateTime endTime) {
        super(String.format("El barbero con ID %d ya tiene una cita programada que se superpone con el horario solicitado (%s - %s)", 
                barberId, startTime, endTime));
    }
}
