package com.juandidev.barberiaback.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceUpdateRequest {

    private String name;

    @Positive(message = "La duración debe ser mayor a 0")
    private Integer duration;

    @Positive(message = "El precio debe ser mayor a 0")
    private Double price;

    private String description;

    private Boolean active;
}
