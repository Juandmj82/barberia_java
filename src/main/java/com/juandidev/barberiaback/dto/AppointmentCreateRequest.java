package com.juandidev.barberiaback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos requeridos para crear una nueva cita de barbería")
public class AppointmentCreateRequest {

    @NotNull(message = "El ID del cliente es obligatorio")
    @Schema(description = "ID único del cliente que solicita la cita", 
            example = "2", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long clientId;

    @NotNull(message = "El ID del barbero es obligatorio")
    @Schema(description = "ID único del barbero que realizará el servicio", 
            example = "3", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long barberId;

    @NotNull(message = "El ID del servicio es obligatorio")
    @Schema(description = "ID único del servicio a realizar (corte, barba, etc.)", 
            example = "1", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long serviceId;

    @NotNull(message = "La hora de inicio es obligatoria")
    @Schema(description = "Fecha y hora de inicio de la cita en formato ISO 8601", 
            example = "2024-12-15T10:00:00", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @Schema(description = "Notas adicionales o instrucciones especiales para la cita", 
            example = "Corte corto por favor, sin barba")
    private String notes;
}
