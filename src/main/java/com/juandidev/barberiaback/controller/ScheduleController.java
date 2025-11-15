package com.juandidev.barberiaback.controller;

import com.juandidev.barberiaback.dto.WorkScheduleCreateRequest;
import com.juandidev.barberiaback.dto.WorkScheduleDto;
import com.juandidev.barberiaback.dto.WorkScheduleUpdateRequest;
import com.juandidev.barberiaback.model.DayOfWeek;
import com.juandidev.barberiaback.model.User;
import com.juandidev.barberiaback.service.ScheduleService;
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

import java.time.LocalTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * Obtener todos los horarios de un barbero específico
     * Accesible por ADMIN o el BARBER propietario
     */
    @GetMapping("/barber/{barberId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('BARBER') and #barberId == authentication.principal.id)")
    public ResponseEntity<List<WorkScheduleDto>> getSchedulesByBarberId(@PathVariable Long barberId) {
        log.info("Solicitud para obtener horarios del barbero ID: {}", barberId);
        
        List<WorkScheduleDto> schedules = scheduleService.findAllByBarberId(barberId);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Obtener horarios activos de un barbero (público para reservas)
     */
    @GetMapping("/barber/{barberId}/active")
    public ResponseEntity<List<WorkScheduleDto>> getActiveSchedulesByBarberId(@PathVariable Long barberId) {
        log.info("Solicitud pública para obtener horarios activos del barbero ID: {}", barberId);
        
        List<WorkScheduleDto> schedules = scheduleService.findActiveByBarberId(barberId);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Obtener horario específico por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    public ResponseEntity<WorkScheduleDto> getScheduleById(@PathVariable Long id) {
        log.info("Solicitud para obtener horario ID: {}", id);
        
        return scheduleService.findById(id)
                .map(schedule -> ResponseEntity.ok(schedule))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crear nuevo horario de trabajo
     * Solo ADMIN o el BARBER propietario pueden crear horarios
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('BARBER')")
    public ResponseEntity<WorkScheduleDto> createSchedule(@Valid @RequestBody WorkScheduleCreateRequest request) {
        User currentUser = getCurrentUser();
        log.info("Solicitud para crear horario por usuario: {} con rol: {}", 
                currentUser.getUsername(), currentUser.getRole());

        WorkScheduleDto createdSchedule = scheduleService.createSchedule(
                request, currentUser.getId(), currentUser.getRole());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);
    }

    /**
     * Actualizar horario existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BARBER')")
    public ResponseEntity<WorkScheduleDto> updateSchedule(@PathVariable Long id, 
                                                         @Valid @RequestBody WorkScheduleUpdateRequest request) {
        User currentUser = getCurrentUser();
        log.info("Solicitud para actualizar horario ID: {} por usuario: {}", id, currentUser.getUsername());

        return scheduleService.updateSchedule(id, request, currentUser.getId(), currentUser.getRole())
                .map(schedule -> ResponseEntity.ok(schedule))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Eliminar horario (eliminación lógica)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BARBER')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        log.info("Solicitud para eliminar horario ID: {} por usuario: {}", id, currentUser.getUsername());

        boolean deleted = scheduleService.deleteSchedule(id, currentUser.getId(), currentUser.getRole());
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Obtener barberos disponibles en un día y hora específicos (público)
     */
    @GetMapping("/available")
    public ResponseEntity<List<WorkScheduleDto>> getAvailableBarbersAtTime(
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        
        log.info("Solicitud pública para barberos disponibles en {} a las {}", dayOfWeek, time);
        
        List<WorkScheduleDto> availableBarbers = scheduleService.findAvailableBarbersAtTime(dayOfWeek, time);
        return ResponseEntity.ok(availableBarbers);
    }

    /**
     * Verificar disponibilidad de un barbero específico (público)
     */
    @GetMapping("/barber/{barberId}/available")
    public ResponseEntity<Boolean> checkBarberAvailability(
            @PathVariable Long barberId,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        
        log.info("Verificando disponibilidad del barbero ID: {} en {} a las {}", barberId, dayOfWeek, time);
        
        boolean available = scheduleService.isBarberAvailable(barberId, dayOfWeek, time);
        return ResponseEntity.ok(available);
    }

    /**
     * Obtener horarios del usuario actual (para barberos)
     */
    @GetMapping("/my-schedules")
    @PreAuthorize("hasRole('BARBER')")
    public ResponseEntity<List<WorkScheduleDto>> getMySchedules() {
        User currentUser = getCurrentUser();
        log.info("Solicitud de horarios propios por barbero: {}", currentUser.getUsername());

        List<WorkScheduleDto> schedules = scheduleService.findAllByBarberId(currentUser.getId());
        return ResponseEntity.ok(schedules);
    }

    /**
     * Crear horario para el usuario actual (para barberos)
     */
    @PostMapping("/my-schedules")
    @PreAuthorize("hasRole('BARBER')")
    public ResponseEntity<WorkScheduleDto> createMySchedule(@Valid @RequestBody WorkScheduleCreateRequest request) {
        User currentUser = getCurrentUser();
        log.info("Barbero {} creando su propio horario", currentUser.getUsername());

        // Forzar que el barberId sea el del usuario actual
        request.setBarberId(currentUser.getId());

        WorkScheduleDto createdSchedule = scheduleService.createSchedule(
                request, currentUser.getId(), currentUser.getRole());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);
    }

    /**
     * Obtener horarios por día de la semana (público para consultas)
     */
    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<List<WorkScheduleDto>> getSchedulesByDay(@PathVariable DayOfWeek dayOfWeek) {
        log.info("Solicitud pública para horarios del día: {}", dayOfWeek);
        
        // Para este endpoint, podríamos implementar una búsqueda por día si es necesario
        // Por ahora, retornamos una lista vacía o implementamos la lógica según necesidades
        return ResponseEntity.ok(List.of());
    }

    // Método auxiliar para obtener el usuario actual
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
