package com.juandidev.barberiaback.exception;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class BarberNotAvailableException extends RuntimeException {
    
    public BarberNotAvailableException(String message) {
        super(message);
    }
    
    public BarberNotAvailableException(Long barberId, LocalDateTime requestedTime, LocalTime barberStart, LocalTime barberEnd) {
        super(String.format("El barbero con ID %d no está disponible en el horario solicitado (%s). " +
                "Horario de trabajo: %s - %s", 
                barberId, requestedTime, barberStart, barberEnd));
    }
    
    public BarberNotAvailableException(Long barberId) {
        super(String.format("El barbero con ID %d no está disponible o no está activo", barberId));
    }
}
