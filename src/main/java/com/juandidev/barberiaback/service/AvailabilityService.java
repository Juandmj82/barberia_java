package com.juandidev.barberiaback.service;

import com.juandidev.barberiaback.dto.AppointmentDto;
import com.juandidev.barberiaback.dto.AvailableSlotDto;
import com.juandidev.barberiaback.dto.WorkScheduleDto;
import com.juandidev.barberiaback.exception.AvailabilityException;
import com.juandidev.barberiaback.exception.EntityNotFoundException;
import com.juandidev.barberiaback.model.DayOfWeek;
import com.juandidev.barberiaback.model.User;
import com.juandidev.barberiaback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final ScheduleService scheduleService;
    private final AppointmentService appointmentService;
    private final UserRepository userRepository;

    /**
     * Lógica central: Calcular slots de tiempo disponibles para un barbero en una fecha específica
     */
    public List<AvailableSlotDto> getAvailableTimeSlots(Long barberId, LocalDate date, Integer durationMinutes) {
        log.info("Calculando slots disponibles para barbero ID: {} en fecha: {} con duración: {} minutos", 
                barberId, date, durationMinutes);

        // Validaciones de entrada
        validateInputParameters(barberId, date, durationMinutes);

        // Paso 1: Obtener horario de trabajo del barbero para el día
        WorkScheduleDto workSchedule = getWorkScheduleForDay(barberId, date);
        if (workSchedule == null) {
            log.info("No hay horario de trabajo para barbero ID: {} en día: {}", barberId, date.getDayOfWeek());
            return new ArrayList<>();
        }

        // Paso 2: Obtener citas confirmadas/pendientes para esa fecha
        List<AppointmentDto> existingAppointments = appointmentService.getAppointmentsByBarberAndDate(barberId, date);

        // Paso 3: Generar slots potenciales basados en el horario de trabajo
        List<AvailableSlotDto> potentialSlots = generatePotentialSlots(
                workSchedule, date, durationMinutes, barberId);

        // Paso 4: Filtrar slots que se superponen con citas existentes
        List<AvailableSlotDto> availableSlots = filterAvailableSlots(potentialSlots, existingAppointments);

        log.info("Generados {} slots potenciales, {} slots disponibles para barbero ID: {} en fecha: {}", 
                potentialSlots.size(), availableSlots.size(), barberId, date);

        return availableSlots;
    }

    /**
     * Obtener todos los barberos disponibles en una fecha y hora específica
     */
    public List<AvailableSlotDto> getAvailableBarbersAtDateTime(LocalDate date, LocalTime time, Integer durationMinutes) {
        log.info("Buscando barberos disponibles en fecha: {} hora: {} duración: {} minutos", date, time, durationMinutes);

        // Validaciones
        validateDateAndTime(date, time, durationMinutes);

        // Obtener todos los barberos activos
        List<User> activeBarbers = userRepository.findByRoleAndEnabledTrue(User.Role.BARBER);

        List<AvailableSlotDto> availableBarbers = new ArrayList<>();

        for (User barber : activeBarbers) {
            try {
                // Verificar si el barbero tiene slots disponibles en esa fecha/hora
                List<AvailableSlotDto> barberSlots = getAvailableTimeSlots(barber.getId(), date, durationMinutes);
                
                // Buscar slot que contenga la hora solicitada
                Optional<AvailableSlotDto> matchingSlot = barberSlots.stream()
                        .filter(slot -> slot.containsTime(time))
                        .findFirst();

                if (matchingSlot.isPresent()) {
                    AvailableSlotDto slot = matchingSlot.get();
                    slot.setBarberId(barber.getId());
                    slot.setBarberName(barber.getFirstName() + " " + barber.getLastName());
                    availableBarbers.add(slot);
                }
            } catch (Exception e) {
                log.debug("Barbero ID: {} no disponible en fecha/hora solicitada: {}", barber.getId(), e.getMessage());
            }
        }

        log.info("Encontrados {} barberos disponibles en fecha: {} hora: {}", availableBarbers.size(), date, time);
        return availableBarbers;
    }

    /**
     * Verificar si un slot específico está disponible
     */
    public boolean isSlotAvailable(Long barberId, LocalDate date, LocalTime startTime, Integer durationMinutes) {
        log.info("Verificando disponibilidad de slot: barbero ID: {} fecha: {} hora: {} duración: {} minutos", 
                barberId, date, startTime, durationMinutes);

        try {
            List<AvailableSlotDto> availableSlots = getAvailableTimeSlots(barberId, date, durationMinutes);
            
            return availableSlots.stream()
                    .anyMatch(slot -> slot.getStartTime().equals(startTime));
        } catch (Exception e) {
            log.warn("Error verificando disponibilidad de slot: {}", e.getMessage());
            return false;
        }
    }

    // Métodos privados de implementación

    private void validateInputParameters(Long barberId, LocalDate date, Integer durationMinutes) {
        if (barberId == null || barberId <= 0) {
            throw new AvailabilityException("barberId", String.valueOf(barberId), "debe ser un ID válido mayor a 0");
        }

        if (date == null) {
            throw new AvailabilityException("date", "null", "la fecha es obligatoria");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new AvailabilityException("date", date.toString(), "la fecha debe ser hoy o en el futuro");
        }

        if (durationMinutes == null || durationMinutes <= 0 || durationMinutes > 480) { // máximo 8 horas
            throw new AvailabilityException("durationMinutes", String.valueOf(durationMinutes), 
                    "debe estar entre 1 y 480 minutos (8 horas)");
        }

        // Validar que el barbero existe
        User barber = userRepository.findById(barberId)
                .orElseThrow(() -> new EntityNotFoundException("Barbero", barberId));

        if (barber.getRole() != User.Role.BARBER && barber.getRole() != User.Role.ADMIN) {
            throw new AvailabilityException("barberId", String.valueOf(barberId), 
                    "el usuario debe tener rol BARBER o ADMIN");
        }

        if (!barber.isEnabled()) {
            throw new AvailabilityException("barberId", String.valueOf(barberId), 
                    "el barbero debe estar activo");
        }
    }

    private void validateDateAndTime(LocalDate date, LocalTime time, Integer durationMinutes) {
        if (date == null) {
            throw new AvailabilityException("date", "null", "la fecha es obligatoria");
        }

        if (time == null) {
            throw new AvailabilityException("time", "null", "la hora es obligatoria");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new AvailabilityException("date", date.toString(), "la fecha debe ser hoy o en el futuro");
        }

        if (durationMinutes == null || durationMinutes <= 0 || durationMinutes > 480) {
            throw new AvailabilityException("durationMinutes", String.valueOf(durationMinutes), 
                    "debe estar entre 1 y 480 minutos");
        }
    }

    private WorkScheduleDto getWorkScheduleForDay(Long barberId, LocalDate date) {
        DayOfWeek dayOfWeek = convertToDayOfWeek(date.getDayOfWeek());
        
        List<WorkScheduleDto> schedules = scheduleService.findActiveByBarberId(barberId);
        
        return schedules.stream()
                .filter(schedule -> schedule.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .orElse(null);
    }

    private DayOfWeek convertToDayOfWeek(java.time.DayOfWeek javaDayOfWeek) {
        switch (javaDayOfWeek) {
            case MONDAY: return DayOfWeek.MONDAY;
            case TUESDAY: return DayOfWeek.TUESDAY;
            case WEDNESDAY: return DayOfWeek.WEDNESDAY;
            case THURSDAY: return DayOfWeek.THURSDAY;
            case FRIDAY: return DayOfWeek.FRIDAY;
            case SATURDAY: return DayOfWeek.SATURDAY;
            case SUNDAY: return DayOfWeek.SUNDAY;
            default: throw new IllegalArgumentException("Día de semana no válido: " + javaDayOfWeek);
        }
    }

    private List<AvailableSlotDto> generatePotentialSlots(WorkScheduleDto workSchedule, LocalDate date, 
                                                         Integer durationMinutes, Long barberId) {
        List<AvailableSlotDto> slots = new ArrayList<>();
        
        LocalTime currentTime = workSchedule.getStartTime();
        LocalTime endTime = workSchedule.getEndTime();
        
        // Generar slots avanzando en intervalos de durationMinutes
        while (currentTime.plusMinutes(durationMinutes).isBefore(endTime) || 
               currentTime.plusMinutes(durationMinutes).equals(endTime)) {
            
            LocalTime slotEndTime = currentTime.plusMinutes(durationMinutes);
            
            AvailableSlotDto slot = AvailableSlotDto.builder()
                    .startTime(currentTime)
                    .endTime(slotEndTime)
                    .date(date)
                    .startDateTime(LocalDateTime.of(date, currentTime))
                    .endDateTime(LocalDateTime.of(date, slotEndTime))
                    .durationMinutes(durationMinutes)
                    .barberId(barberId)
                    .barberName(workSchedule.getBarberFullName())
                    .available(true)
                    .build();
            
            slots.add(slot);
            
            // Avanzar al siguiente slot
            currentTime = currentTime.plusMinutes(durationMinutes);
        }
        
        log.debug("Generados {} slots potenciales para barbero ID: {} en fecha: {}", slots.size(), barberId, date);
        return slots;
    }

    private List<AvailableSlotDto> filterAvailableSlots(List<AvailableSlotDto> potentialSlots, 
                                                       List<AppointmentDto> existingAppointments) {
        return potentialSlots.stream()
                .filter(slot -> !isSlotBlocked(slot, existingAppointments))
                .collect(Collectors.toList());
    }

    private boolean isSlotBlocked(AvailableSlotDto slot, List<AppointmentDto> existingAppointments) {
        return existingAppointments.stream()
                .anyMatch(appointment -> slotsOverlap(slot, appointment));
    }

    private boolean slotsOverlap(AvailableSlotDto slot, AppointmentDto appointment) {
        LocalTime appointmentStart = appointment.getStartTime().toLocalTime();
        LocalTime appointmentEnd = appointment.getEndTime().toLocalTime();
        
        // Verificar superposición: slot se superpone si su inicio es antes del fin de la cita
        // y su fin es después del inicio de la cita
        boolean overlaps = slot.getStartTime().isBefore(appointmentEnd) && 
                          slot.getEndTime().isAfter(appointmentStart);
        
        if (overlaps) {
            log.debug("Slot {}:{} se superpone con cita {}:{}", 
                    slot.getStartTime(), slot.getEndTime(), appointmentStart, appointmentEnd);
        }
        
        return overlaps;
    }
}
