package com.juandidev.barberiaback.controller;

import com.juandidev.barberiaback.dto.AppointmentCreateRequest;
import com.juandidev.barberiaback.dto.AppointmentDto;
import com.juandidev.barberiaback.dto.AppointmentUpdateRequest;
import com.juandidev.barberiaback.model.AppointmentStatus;
import com.juandidev.barberiaback.model.User;
import com.juandidev.barberiaback.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Tag(name = "Gestión de Citas", description = "Endpoints para la gestión completa de citas de barbería con autorización por roles")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(
        summary = "Obtener todas las citas",
        description = "Obtiene la lista completa de citas en el sistema. Solo disponible para administradores y barberos.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de citas obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AppointmentDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token JWT inválido o expirado"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Solo ADMIN y BARBER pueden ver todas las citas"
        )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    public ResponseEntity<List<AppointmentDto>> getAllAppointments() {
        List<AppointmentDto> appointments = appointmentService.getAllAppointments();
        return ResponseEntity.ok(appointments);
    }

    @Operation(
        summary = "Obtener cita por ID",
        description = "Obtiene los detalles de una cita específica. Los clientes solo pueden ver sus propias citas, " +
                     "mientras que barberos y administradores pueden ver citas según su rol y asignación.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cita encontrada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AppointmentDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token JWT inválido o expirado"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - No tiene permisos para ver esta cita"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cita no encontrada"
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'BARBER')")
    public ResponseEntity<AppointmentDto> getAppointmentById(
            @Parameter(description = "ID único de la cita", example = "1") 
            @PathVariable Long id) {
        User currentUser = getCurrentUser();
        log.info("Solicitud de consulta de cita ID: {} por usuario: {} con rol: {}", 
                id, currentUser.getUsername(), currentUser.getRole());
        
        return appointmentService.getAppointmentById(id, currentUser.getId(), currentUser.getRole())
                .map(appointment -> ResponseEntity.ok(appointment))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Crear nueva cita",
        description = "Permite crear una nueva cita de barbería. Los clientes pueden crear citas para sí mismos, " +
                     "mientras que los administradores pueden crear citas para cualquier cliente. " +
                     "Valida disponibilidad del barbero y evita conflictos de horarios.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Cita creada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AppointmentDto.class),
                examples = @ExampleObject(
                    name = "Cita creada",
                    value = """
                        {
                          "id": 1,
                          "clientId": 2,
                          "clientName": "Juan Pérez",
                          "clientEmail": "juan@email.com",
                          "barberId": 3,
                          "barberName": "Carlos Barbero",
                          "serviceId": 1,
                          "serviceName": "Corte Clásico",
                          "serviceDuration": 30,
                          "startTime": "2024-12-15T10:00:00",
                          "endTime": "2024-12-15T10:30:00",
                          "status": "PENDING",
                          "notes": "Corte corto por favor",
                          "totalPrice": 25.00,
                          "createdAt": "2024-12-14T15:30:00",
                          "updatedAt": "2024-12-14T15:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error de validación en los datos de entrada",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error de validación",
                    value = """
                        {
                          "message": "Error de validación",
                          "errors": {
                            "barberId": "El ID del barbero es obligatorio",
                            "startTime": "La hora de inicio es obligatoria"
                          },
                          "status": "error"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token JWT inválido o expirado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "No autorizado",
                    value = """
                        {
                          "message": "Token JWT inválido o expirado",
                          "status": "error"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Solo ADMIN y CLIENT pueden crear citas",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Acceso denegado",
                    value = """
                        {
                          "message": "Acceso denegado - Rol insuficiente",
                          "status": "error"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Barbero, cliente o servicio no encontrado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Recurso no encontrado",
                    value = """
                        {
                          "message": "Barbero con ID 3 no encontrado",
                          "status": "error"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflicto de horarios - El barbero no está disponible en ese horario",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Conflicto de horarios",
                    value = """
                        {
                          "message": "El barbero no está disponible en el horario solicitado",
                          "status": "error",
                          "type": "APPOINTMENT_CONFLICT",
                          "details": {
                            "requestedTime": "2024-12-15T10:00:00",
                            "conflictingAppointment": {
                              "id": 5,
                              "startTime": "2024-12-15T09:30:00",
                              "endTime": "2024-12-15T10:30:00"
                            }
                          }
                        }
                        """
                )
            )
        )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<AppointmentDto> createAppointment(@Valid @RequestBody AppointmentCreateRequest request) {
        User currentUser = getCurrentUser();
        log.info("Solicitud de creación de cita por usuario: {} con rol: {}", 
                currentUser.getUsername(), currentUser.getRole());
        
        AppointmentDto createdAppointment = appointmentService.createAppointment(
                request, currentUser.getId(), currentUser.getRole());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAppointment);
    }

    @Operation(
        summary = "Actualizar cita",
        description = "Actualiza los detalles de una cita existente. Permite modificar barbero, servicio, " +
                     "hora de inicio, estado y notas. Solo disponible para administradores.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cita actualizada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AppointmentDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error de validación en los datos de entrada"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token JWT inválido o expirado"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cita no encontrada"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflicto - Horario no disponible o cambio de estado inválido"
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentDto> updateAppointment(
            @Parameter(description = "ID único de la cita a actualizar", example = "1")
            @PathVariable Long id, 
            @Valid @RequestBody AppointmentUpdateRequest request) {
        return appointmentService.updateAppointment(id, request)
                .map(appointment -> ResponseEntity.ok(appointment))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Cancelar cita",
        description = "Cancela una cita existente. Los clientes pueden cancelar sus propias citas, " +
                     "mientras que administradores y barberos pueden cancelar citas según su rol.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Cita cancelada exitosamente"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token JWT inválido o expirado"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - No tiene permisos para cancelar esta cita"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cita no encontrada"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflicto - No se puede cancelar la cita (ya completada o fuera de tiempo límite)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error de cancelación",
                    value = """
                        {
                          "message": "No se puede cancelar una cita que ya ha sido completada",
                          "status": "error",
                          "type": "INVALID_STATUS_TRANSITION"
                        }
                        """
                )
            )
        )
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelAppointment(
            @Parameter(description = "ID único de la cita a cancelar", example = "1")
            @PathVariable Long id) {
        User currentUser = getCurrentUser();
        boolean cancelled = appointmentService.cancelAppointment(id, currentUser.getId(), currentUser.getRole());
        return cancelled ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "Confirmar cita",
        description = "Confirma una cita pendiente. Solo barberos y administradores pueden confirmar citas. " +
                     "La cita debe estar en estado PENDING para poder ser confirmada.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Cita confirmada exitosamente"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token JWT inválido o expirado"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Solo ADMIN y BARBER pueden confirmar citas"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cita no encontrada"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflicto - La cita no puede ser confirmada (no está en estado PENDING)"
        )
    })
    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    public ResponseEntity<Void> confirmAppointment(
            @Parameter(description = "ID único de la cita a confirmar", example = "1")
            @PathVariable Long id) {
        User currentUser = getCurrentUser();
        boolean confirmed = appointmentService.confirmAppointment(id, currentUser.getId(), currentUser.getRole());
        return confirmed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "Completar cita",
        description = "Marca una cita como completada. Solo barberos y administradores pueden completar citas. " +
                     "La cita debe estar en estado CONFIRMED para poder ser completada.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Cita completada exitosamente"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token JWT inválido o expirado"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Solo ADMIN y BARBER pueden completar citas"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cita no encontrada"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflicto - La cita no puede ser completada (no está en estado CONFIRMED)"
        )
    })
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    public ResponseEntity<Void> completeAppointment(
            @Parameter(description = "ID único de la cita a completar", example = "1")
            @PathVariable Long id) {
        User currentUser = getCurrentUser();
        boolean completed = appointmentService.completeAppointment(id, currentUser.getId(), currentUser.getRole());
        return completed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByClient(@PathVariable Long clientId) {
        List<AppointmentDto> appointments = appointmentService.getAppointmentsByClient(clientId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/barber/{barberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByBarber(@PathVariable Long barberId) {
        List<AppointmentDto> appointments = appointmentService.getAppointmentsByBarber(barberId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByStatus(@PathVariable AppointmentStatus status) {
        List<AppointmentDto> appointments = appointmentService.getAppointmentsByStatus(status);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AppointmentDto> appointments = appointmentService.getAppointmentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/barber/{barberId}/conflicts")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    public ResponseEntity<Boolean> checkConflicts(
            @PathVariable Long barberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        boolean hasConflicts = appointmentService.hasConflictingAppointments(barberId, startTime, endTime);
        return ResponseEntity.ok(hasConflicts);
    }

    @Operation(
        summary = "Obtener mis citas",
        description = "Obtiene las citas del usuario autenticado. Los clientes ven sus propias citas, " +
                     "los barberos ven las citas asignadas a ellos, y los administradores ven todas las citas. " +
                     "Opcionalmente se puede filtrar por estado de la cita.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de citas obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AppointmentDto.class),
                examples = @ExampleObject(
                    name = "Lista de citas",
                    value = """
                        [
                          {
                            "id": 1,
                            "clientId": 2,
                            "clientName": "Juan Pérez",
                            "clientEmail": "juan@email.com",
                            "barberId": 3,
                            "barberName": "Carlos Barbero",
                            "serviceId": 1,
                            "serviceName": "Corte Clásico",
                            "serviceDuration": 30,
                            "startTime": "2024-12-15T10:00:00",
                            "endTime": "2024-12-15T10:30:00",
                            "status": "CONFIRMED",
                            "notes": "Corte corto por favor",
                            "totalPrice": 25.00,
                            "createdAt": "2024-12-14T15:30:00",
                            "updatedAt": "2024-12-14T16:00:00"
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token JWT inválido o expirado"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Token requerido"
        )
    })
    @GetMapping("/my-appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'BARBER')")
    public ResponseEntity<List<AppointmentDto>> getMyAppointments(
            @Parameter(description = "Filtrar por estado de la cita (opcional)", example = "CONFIRMED")
            @RequestParam(required = false) AppointmentStatus status) {
        User currentUser = getCurrentUser();
        log.info("Solicitud de citas propias por usuario: {} con rol: {} y estado: {}", 
                currentUser.getUsername(), currentUser.getRole(), status);
        
        List<AppointmentDto> appointments = appointmentService.getAppointmentsForCurrentUser(
                currentUser.getId(), currentUser.getRole(), status);
        return ResponseEntity.ok(appointments);
    }

    // Método auxiliar para obtener el usuario actual
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
