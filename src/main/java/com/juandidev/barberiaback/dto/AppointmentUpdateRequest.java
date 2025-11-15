package com.juandidev.barberiaback.dto;

import com.juandidev.barberiaback.model.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para actualizar una cita existente (todos los campos son opcionales)")
public class AppointmentUpdateRequest {

    @Schema(description = "Nuevo ID del barbero asignado (opcional)", example = "4")
    private Long barberId;

    @Schema(description = "Nuevo ID del servicio (opcional)", example = "2")
    private Long serviceId;

    @Schema(description = "Nueva fecha y hora de inicio (opcional)", example = "2024-12-15T11:00:00")
    private LocalDateTime startTime;

    @Schema(description = "Nuevo estado de la cita (opcional)", example = "CONFIRMED")
    private AppointmentStatus status;

    @Schema(description = "Nuevas notas adicionales (opcional)", example = "Cambio de horario solicitado por el cliente")
    private String notes;
}
