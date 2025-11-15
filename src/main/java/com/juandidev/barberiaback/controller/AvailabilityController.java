package com.juandidev.barberiaback.controller;

import com.juandidev.barberiaback.dto.AvailableSlotDto;
import com.juandidev.barberiaback.service.AvailabilityService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    /**
     * Endpoint principal: Obtener slots de tiempo disponibles para un barbero
     * Público para que los clientes puedan consultar disponibilidad
     */
    @GetMapping("/barber/{barberId}")
    public ResponseEntity<List<AvailableSlotDto>> getAvailableTimeSlots(
            @PathVariable Long barberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Integer duration) {
        
        log.info("Solicitud de disponibilidad para barbero ID: {} en fecha: {} con duración: {} minutos", 
                barberId, date, duration);

        List<AvailableSlotDto> availableSlots = availabilityService.getAvailableTimeSlots(
                barberId, date, duration);

        log.info("Retornando {} slots disponibles para barbero ID: {} en fecha: {}", 
                availableSlots.size(), barberId, date);

        return ResponseEntity.ok(availableSlots);
    }

    /**
     * Obtener barberos disponibles en una fecha y hora específica
     * Útil para mostrar alternativas al cliente
     */
    @GetMapping("/barbers")
    public ResponseEntity<List<AvailableSlotDto>> getAvailableBarbers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam Integer duration) {
        
        log.info("Solicitud de barberos disponibles en fecha: {} hora: {} duración: {} minutos", 
                date, time, duration);

        List<AvailableSlotDto> availableBarbers = availabilityService.getAvailableBarbersAtDateTime(
                date, time, duration);

        log.info("Encontrados {} barberos disponibles en fecha: {} hora: {}", 
                availableBarbers.size(), date, time);

        return ResponseEntity.ok(availableBarbers);
    }

    /**
     * Verificar si un slot específico está disponible
     * Útil para validación antes de crear una cita
     */
    @GetMapping("/barber/{barberId}/slot")
    public ResponseEntity<Boolean> checkSlotAvailability(
            @PathVariable Long barberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam Integer duration) {
        
        log.info("Verificando disponibilidad de slot específico: barbero ID: {} fecha: {} hora: {} duración: {} minutos", 
                barberId, date, startTime, duration);

        boolean isAvailable = availabilityService.isSlotAvailable(barberId, date, startTime, duration);

        log.info("Slot disponible: {} para barbero ID: {} en fecha: {} hora: {}", 
                isAvailable, barberId, date, startTime);

        return ResponseEntity.ok(isAvailable);
    }

    /**
     * Obtener disponibilidad resumida por día para un barbero
     * Útil para mostrar calendario con días disponibles/ocupados
     */
    @GetMapping("/barber/{barberId}/summary")
    public ResponseEntity<List<AvailabilityDaySummaryDto>> getAvailabilitySummary(
            @PathVariable Long barberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "30") Integer duration) {
        
        log.info("Solicitud de resumen de disponibilidad para barbero ID: {} desde: {} hasta: {} duración: {} minutos", 
                barberId, startDate, endDate, duration);

        // Validar rango de fechas
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior o igual a la fecha de fin");
        }

        if (startDate.plusDays(31).isBefore(endDate)) {
            throw new IllegalArgumentException("El rango de fechas no puede ser mayor a 31 días");
        }

        List<AvailabilityDaySummaryDto> summary = generateAvailabilitySummary(
                barberId, startDate, endDate, duration);

        log.info("Generado resumen de disponibilidad con {} días para barbero ID: {}", 
                summary.size(), barberId);

        return ResponseEntity.ok(summary);
    }

    /**
     * Obtener próximos slots disponibles para un barbero
     * Útil para sugerir horarios alternativos
     */
    @GetMapping("/barber/{barberId}/next-available")
    public ResponseEntity<List<AvailableSlotDto>> getNextAvailableSlots(
            @PathVariable Long barberId,
            @RequestParam(defaultValue = "30") Integer duration,
            @RequestParam(defaultValue = "5") Integer limit) {
        
        log.info("Solicitud de próximos {} slots disponibles para barbero ID: {} con duración: {} minutos", 
                limit, barberId, duration);

        List<AvailableSlotDto> nextSlots = findNextAvailableSlots(barberId, duration, limit);

        log.info("Encontrados {} próximos slots disponibles para barbero ID: {}", 
                nextSlots.size(), barberId);

        return ResponseEntity.ok(nextSlots);
    }

    // Métodos auxiliares privados

    private List<AvailabilityDaySummaryDto> generateAvailabilitySummary(
            Long barberId, LocalDate startDate, LocalDate endDate, Integer duration) {
        
        List<AvailabilityDaySummaryDto> summary = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            try {
                List<AvailableSlotDto> daySlots = availabilityService.getAvailableTimeSlots(
                        barberId, currentDate, duration);

                AvailabilityDaySummaryDto daySummary = AvailabilityDaySummaryDto.builder()
                        .date(currentDate)
                        .dayOfWeek(currentDate.getDayOfWeek().name())
                        .availableSlots(daySlots.size())
                        .hasAvailability(daySlots.size() > 0)
                        .firstAvailableTime(daySlots.isEmpty() ? null : daySlots.get(0).getStartTime())
                        .lastAvailableTime(daySlots.isEmpty() ? null : 
                                daySlots.get(daySlots.size() - 1).getStartTime())
                        .build();

                summary.add(daySummary);
            } catch (Exception e) {
                log.debug("No hay disponibilidad para fecha: {} - {}", currentDate, e.getMessage());
                
                AvailabilityDaySummaryDto daySummary = AvailabilityDaySummaryDto.builder()
                        .date(currentDate)
                        .dayOfWeek(currentDate.getDayOfWeek().name())
                        .availableSlots(0)
                        .hasAvailability(false)
                        .build();

                summary.add(daySummary);
            }

            currentDate = currentDate.plusDays(1);
        }

        return summary;
    }

    private List<AvailableSlotDto> findNextAvailableSlots(Long barberId, Integer duration, Integer limit) {
        List<AvailableSlotDto> nextSlots = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        int daysChecked = 0;
        int maxDaysToCheck = 30; // Buscar hasta 30 días en el futuro

        while (nextSlots.size() < limit && daysChecked < maxDaysToCheck) {
            try {
                List<AvailableSlotDto> daySlots = availabilityService.getAvailableTimeSlots(
                        barberId, currentDate, duration);

                // Si es hoy, filtrar slots que ya pasaron
                if (currentDate.equals(LocalDate.now())) {
                    LocalTime now = LocalTime.now();
                    daySlots = daySlots.stream()
                            .filter(slot -> slot.getStartTime().isAfter(now))
                            .collect(Collectors.toList());
                }

                nextSlots.addAll(daySlots);
                
                // Limitar al número solicitado
                if (nextSlots.size() > limit) {
                    nextSlots = nextSlots.subList(0, limit);
                }

            } catch (Exception e) {
                log.debug("No hay disponibilidad para fecha: {} - {}", currentDate, e.getMessage());
            }

            currentDate = currentDate.plusDays(1);
            daysChecked++;
        }

        return nextSlots;
    }

    // DTO para resumen de disponibilidad por día
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailabilityDaySummaryDto {
        private LocalDate date;
        private String dayOfWeek;
        private Integer availableSlots;
        private Boolean hasAvailability;
        private LocalTime firstAvailableTime;
        private LocalTime lastAvailableTime;
    }
}
