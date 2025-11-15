package com.juandidev.barberiaback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotDto {

    private LocalTime startTime;
    
    private LocalTime endTime;
    
    private LocalDate date;
    
    private LocalDateTime startDateTime;
    
    private LocalDateTime endDateTime;
    
    private Integer durationMinutes;
    
    private Long barberId;
    
    private String barberName;
    
    private Boolean available;

    // Métodos de conveniencia
    public boolean isAvailable() {
        return available != null && available;
    }

    public String getTimeSlotDisplay() {
        return startTime + " - " + endTime;
    }

    public boolean overlapsWithTime(LocalTime otherStart, LocalTime otherEnd) {
        return startTime.isBefore(otherEnd) && endTime.isAfter(otherStart);
    }

    public boolean containsTime(LocalTime time) {
        return !time.isBefore(startTime) && time.isBefore(endTime);
    }
}
