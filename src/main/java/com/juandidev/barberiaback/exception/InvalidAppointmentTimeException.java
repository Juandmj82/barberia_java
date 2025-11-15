package com.juandidev.barberiaback.exception;

import java.time.LocalDateTime;

public class InvalidAppointmentTimeException extends RuntimeException {
    
    public InvalidAppointmentTimeException(String message) {
        super(message);
    }
    
    public InvalidAppointmentTimeException(LocalDateTime startTime, LocalDateTime endTime) {
        super(String.format("Horario de cita inválido: la hora de inicio (%s) debe ser anterior a la hora de fin (%s)", 
                startTime, endTime));
    }
    
    public InvalidAppointmentTimeException(LocalDateTime appointmentTime) {
        super(String.format("No se pueden programar citas en el pasado. Hora solicitada: %s", appointmentTime));
    }
}
