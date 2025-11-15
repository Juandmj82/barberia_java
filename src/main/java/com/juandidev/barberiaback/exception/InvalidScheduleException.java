package com.juandidev.barberiaback.exception;

import com.juandidev.barberiaback.model.DayOfWeek;

import java.time.LocalTime;

public class InvalidScheduleException extends RuntimeException {
    
    public InvalidScheduleException(String message) {
        super(message);
    }
    
    public InvalidScheduleException(LocalTime startTime, LocalTime endTime) {
        super(String.format("Horario inválido: La hora de inicio (%s) debe ser anterior a la hora de fin (%s)", 
                startTime, endTime));
    }
    
    public InvalidScheduleException(Long barberId, DayOfWeek dayOfWeek) {
        super(String.format("Ya existe un horario para el barbero ID %d en el día %s", 
                barberId, dayOfWeek.getDisplayName()));
    }
    
    public InvalidScheduleException(String field, String value, String reason) {
        super(String.format("Error en %s '%s': %s", field, value, reason));
    }
}
