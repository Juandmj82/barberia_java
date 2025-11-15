package com.juandidev.barberiaback.dto;

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
public class BarberDto {
    private Long id;
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String specialties;
    private Integer experienceYears;
    private String phoneNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean available;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
