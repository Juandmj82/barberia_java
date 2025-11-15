package com.juandidev.barberiaback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarberUpdateRequest {

    private String specialties;

    private Integer experienceYears;

    private String phoneNumber;

    private LocalTime startTime;

    private LocalTime endTime;

    private Boolean available;

    private Boolean active;
}
