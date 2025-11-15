package com.juandidev.barberiaback.controller;

import com.juandidev.barberiaback.dto.BarberCreateRequest;
import com.juandidev.barberiaback.dto.BarberDto;
import com.juandidev.barberiaback.dto.BarberUpdateRequest;
import com.juandidev.barberiaback.service.BarberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/barbers")
@RequiredArgsConstructor
public class BarberController {

    private final BarberService barberService;

    @GetMapping
    public ResponseEntity<List<BarberDto>> getAllAvailableBarbers() {
        log.info("Solicitud para obtener todos los barberos disponibles");
        List<BarberDto> barbers = barberService.getAllAvailableBarbers();
        return ResponseEntity.ok(barbers);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BarberDto>> getAllActiveBarbers() {
        log.info("Solicitud para obtener todos los barberos activos");
        List<BarberDto> barbers = barberService.getAllActiveBarbers();
        return ResponseEntity.ok(barbers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarberDto> getBarberById(@PathVariable Long id) {
        log.info("Solicitud para obtener barbero con ID: {}", id);
        return barberService.getBarberById(id)
                .map(barber -> ResponseEntity.ok(barber))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    public ResponseEntity<BarberDto> getBarberByUserId(@PathVariable Long userId) {
        log.info("Solicitud para obtener barbero por ID de usuario: {}", userId);
        return barberService.getBarberByUserId(userId)
                .map(barber -> ResponseEntity.ok(barber))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BarberDto> createBarber(@Valid @RequestBody BarberCreateRequest request) {
        log.info("Solicitud para crear barbero para usuario ID: {}", request.getUserId());
        BarberDto createdBarber = barberService.createBarber(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBarber);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BarberDto> updateBarber(@PathVariable Long id, 
                                                 @Valid @RequestBody BarberUpdateRequest request) {
        log.info("Solicitud para actualizar barbero con ID: {}", id);
        return barberService.updateBarber(id, request)
                .map(barber -> ResponseEntity.ok(barber))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBarber(@PathVariable Long id) {
        log.info("Solicitud para eliminar barbero con ID: {}", id);
        boolean deleted = barberService.deleteBarber(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    public ResponseEntity<Void> updateAvailability(@PathVariable Long id, 
                                                  @RequestParam boolean available) {
        log.info("Solicitud para actualizar disponibilidad del barbero ID: {} a: {}", id, available);
        boolean updated = barberService.updateAvailability(id, available);
        return updated ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/available-at")
    public ResponseEntity<List<BarberDto>> getAvailableBarbersAtTime(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        log.info("Solicitud para obtener barberos disponibles a las: {}", time);
        List<BarberDto> barbers = barberService.getAvailableBarbersAtTime(time);
        return ResponseEntity.ok(barbers);
    }

    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<BarberDto>> getBarbersBySpecialty(@PathVariable String specialty) {
        log.info("Solicitud para obtener barberos con especialidad: {}", specialty);
        List<BarberDto> barbers = barberService.getBarbersBySpecialty(specialty);
        return ResponseEntity.ok(barbers);
    }

    @GetMapping("/experience/{minYears}")
    public ResponseEntity<List<BarberDto>> getBarbersByMinimumExperience(@PathVariable Integer minYears) {
        log.info("Solicitud para obtener barberos con experiencia mínima: {} años", minYears);
        List<BarberDto> barbers = barberService.getBarbersByMinimumExperience(minYears);
        return ResponseEntity.ok(barbers);
    }

    @GetMapping("/exists/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> existsByUserId(@PathVariable Long userId) {
        log.info("Verificando si existe barbero para usuario ID: {}", userId);
        boolean exists = barberService.existsByUserId(userId);
        return ResponseEntity.ok(exists);
    }
}
