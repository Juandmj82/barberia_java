package com.juandidev.barberiaback.exception;

import java.time.LocalDate;

public class AvailabilityException extends RuntimeException {
    
    public AvailabilityException(String message) {
        super(message);
    }
    
    public AvailabilityException(Long barberId, LocalDate date) {
        super(String.format("No hay disponibilidad para el barbero ID %d en la fecha %s", barberId, date));
    }
    
    public AvailabilityException(String field, String value, String reason) {
        super(String.format("Error en %s '%s': %s", field, value, reason));
    }
    
    public AvailabilityException(Long barberId, LocalDate date, String reason) {
        super(String.format("Error de disponibilidad para barbero ID %d en fecha %s: %s", barberId, date, reason));
    }
}
