package com.juandidev.barberiaback.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarberCreateRequest {

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long userId;

    private String specialties;

    private Integer experienceYears;

    private String phoneNumber;

    private LocalTime startTime;

    private LocalTime endTime;
}
