package com.juandidev.barberiaback.dto;

import com.juandidev.barberiaback.model.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información completa de una cita de barbería")
public class AppointmentDto {
    
    @Schema(description = "ID único de la cita", example = "1")
    private Long id;
    
    @Schema(description = "ID del cliente", example = "2")
    private Long clientId;
    
    @Schema(description = "Nombre completo del cliente", example = "Juan Pérez")
    private String clientName;
    
    @Schema(description = "Email del cliente", example = "juan@email.com")
    private String clientEmail;
    
    @Schema(description = "ID del barbero asignado", example = "3")
    private Long barberId;
    
    @Schema(description = "Nombre completo del barbero", example = "Carlos Barbero")
    private String barberName;
    
    @Schema(description = "ID del servicio solicitado", example = "1")
    private Long serviceId;
    
    @Schema(description = "Nombre del servicio", example = "Corte Clásico")
    private String serviceName;
    
    @Schema(description = "Duración del servicio en minutos", example = "30")
    private Integer serviceDuration;
    
    @Schema(description = "Fecha y hora de inicio de la cita", example = "2024-12-15T10:00:00")
    private LocalDateTime startTime;
    
    @Schema(description = "Fecha y hora de finalización de la cita", example = "2024-12-15T10:30:00")
    private LocalDateTime endTime;
    
    @Schema(description = "Estado actual de la cita", example = "CONFIRMED")
    private AppointmentStatus status;
    
    @Schema(description = "Notas adicionales de la cita", example = "Corte corto por favor")
    private String notes;
    
    @Schema(description = "Precio total del servicio", example = "25.00")
    private Double totalPrice;
    
    @Schema(description = "Fecha y hora de creación de la cita", example = "2024-12-14T15:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Fecha y hora de última actualización", example = "2024-12-14T16:00:00")
    private LocalDateTime updatedAt;
}
