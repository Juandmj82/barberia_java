package com.juandidev.barberiaback.dto;

import com.juandidev.barberiaback.model.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkScheduleDto {

    private Long id;
    
    private Long barberId;
    
    private String barberUsername;
    
    private String barberFullName;
    
    private DayOfWeek dayOfWeek;
    
    private String dayDisplayName;
    
    private LocalTime startTime;
    
    private LocalTime endTime;
    
    private Boolean active;
    
    private Long workingHours;
    
    private Long workingMinutes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    // Métodos de conveniencia
    public boolean isWorkingDay() {
        return active && startTime != null && endTime != null;
    }

    public boolean isTimeWithinSchedule(LocalTime time) {
        if (!isWorkingDay()) {
            return false;
        }
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }
}
