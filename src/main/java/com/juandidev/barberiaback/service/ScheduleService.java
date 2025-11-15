package com.juandidev.barberiaback.service;

import com.juandidev.barberiaback.dto.WorkScheduleCreateRequest;
import com.juandidev.barberiaback.dto.WorkScheduleDto;
import com.juandidev.barberiaback.dto.WorkScheduleUpdateRequest;
import com.juandidev.barberiaback.exception.EntityNotFoundException;
import com.juandidev.barberiaback.exception.InvalidScheduleException;
import com.juandidev.barberiaback.exception.UnauthorizedScheduleAccessException;
import com.juandidev.barberiaback.model.DayOfWeek;
import com.juandidev.barberiaback.model.User;
import com.juandidev.barberiaback.model.WorkSchedule;
import com.juandidev.barberiaback.repository.UserRepository;
import com.juandidev.barberiaback.repository.WorkScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final UserRepository userRepository;

    /**
     * Obtener todos los horarios de un barbero específico
     */
    public List<WorkScheduleDto> findAllByBarberId(Long barberId) {
        log.info("Obteniendo horarios para barbero ID: {}", barberId);
        
        List<WorkSchedule> schedules = workScheduleRepository.findByBarberIdOrderByDayOfWeek(barberId);
        
        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtener solo los horarios activos de un barbero
     */
    public List<WorkScheduleDto> findActiveByBarberId(Long barberId) {
        log.info("Obteniendo horarios activos para barbero ID: {}", barberId);
        
        List<WorkSchedule> schedules = workScheduleRepository.findByBarberIdAndActiveTrueOrderByDayOfWeek(barberId);
        
        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtener horario específico por ID
     */
    public Optional<WorkScheduleDto> findById(Long id) {
        log.info("Buscando horario con ID: {}", id);
        
        return workScheduleRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * Crear nuevo horario de trabajo
     */
    @Transactional
    public WorkScheduleDto createSchedule(WorkScheduleCreateRequest request, Long currentUserId, User.Role currentUserRole) {
        log.info("Creando horario para barbero ID: {} en día: {} por usuario ID: {}", 
                request.getBarberId(), request.getDayOfWeek(), currentUserId);

        // Validar autorización
        validateCreateAuthorization(request.getBarberId(), currentUserId, currentUserRole);

        // Validar que el barbero existe y tiene rol BARBER
        User barber = validateBarber(request.getBarberId());

        // Validar horarios
        validateScheduleTimes(request.getStartTime(), request.getEndTime());

        // Verificar que no existe ya un horario para este día
        if (workScheduleRepository.existsByBarberIdAndDayOfWeek(request.getBarberId(), request.getDayOfWeek())) {
            throw new InvalidScheduleException(request.getBarberId(), request.getDayOfWeek());
        }

        // Crear y guardar el horario
        WorkSchedule schedule = convertToEntity(request, barber);
        WorkSchedule savedSchedule = workScheduleRepository.save(schedule);

        log.info("Horario creado exitosamente con ID: {} para barbero: {}", 
                savedSchedule.getId(), barber.getUsername());

        return convertToDto(savedSchedule);
    }

    /**
     * Actualizar horario existente
     */
    @Transactional
    public Optional<WorkScheduleDto> updateSchedule(Long id, WorkScheduleUpdateRequest request, 
                                                   Long currentUserId, User.Role currentUserRole) {
        log.info("Actualizando horario ID: {} por usuario ID: {}", id, currentUserId);

        return workScheduleRepository.findById(id)
                .map(existingSchedule -> {
                    // Validar autorización
                    validateUpdateAuthorization(existingSchedule, currentUserId, currentUserRole);

                    // Actualizar campos
                    updateScheduleFields(existingSchedule, request);

                    // Validar horarios si se modificaron
                    if (request.getStartTime() != null || request.getEndTime() != null) {
                        validateScheduleTimes(existingSchedule.getStartTime(), existingSchedule.getEndTime());
                    }

                    // Verificar unicidad si se cambió el día
                    if (request.getDayOfWeek() != null && !request.getDayOfWeek().equals(existingSchedule.getDayOfWeek())) {
                        if (workScheduleRepository.existsByBarberIdAndDayOfWeek(
                                existingSchedule.getBarber().getId(), request.getDayOfWeek())) {
                            throw new InvalidScheduleException(existingSchedule.getBarber().getId(), request.getDayOfWeek());
                        }
                    }

                    WorkSchedule updatedSchedule = workScheduleRepository.save(existingSchedule);
                    log.info("Horario actualizado exitosamente: {}", updatedSchedule.getId());

                    return convertToDto(updatedSchedule);
                });
    }

    /**
     * Eliminar horario (eliminación lógica)
     */
    @Transactional
    public boolean deleteSchedule(Long id, Long currentUserId, User.Role currentUserRole) {
        log.info("Eliminando horario ID: {} por usuario ID: {}", id, currentUserId);

        return workScheduleRepository.findById(id)
                .map(schedule -> {
                    // Validar autorización
                    validateDeleteAuthorization(schedule, currentUserId, currentUserRole);

                    // Eliminación lógica
                    schedule.setActive(false);
                    workScheduleRepository.save(schedule);

                    log.info("Horario marcado como inactivo: {}", schedule.getId());
                    return true;
                })
                .orElse(false);
    }

    /**
     * Obtener barberos disponibles en un día y hora específicos
     */
    public List<WorkScheduleDto> findAvailableBarbersAtTime(DayOfWeek dayOfWeek, LocalTime time) {
        log.info("Buscando barberos disponibles en {} a las {}", dayOfWeek, time);

        List<WorkSchedule> availableSchedules = workScheduleRepository.findAvailableBarbersAtTime(dayOfWeek, time);

        return availableSchedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Verificar si un barbero está disponible en un día y hora específicos
     */
    public boolean isBarberAvailable(Long barberId, DayOfWeek dayOfWeek, LocalTime time) {
        log.info("Verificando disponibilidad del barbero ID: {} en {} a las {}", barberId, dayOfWeek, time);

        return workScheduleRepository.findByBarberIdAndDayOfWeek(barberId, dayOfWeek)
                .map(schedule -> schedule.isTimeWithinSchedule(time))
                .orElse(false);
    }

    // Métodos de validación privados

    private void validateCreateAuthorization(Long barberId, Long currentUserId, User.Role currentUserRole) {
        if (currentUserRole == User.Role.ADMIN) {
            return; // ADMIN puede crear horarios para cualquier barbero
        }

        if (currentUserRole == User.Role.BARBER && barberId.equals(currentUserId)) {
            return; // BARBER puede crear sus propios horarios
        }

        throw new UnauthorizedScheduleAccessException(barberId, currentUserId);
    }

    private void validateUpdateAuthorization(WorkSchedule schedule, Long currentUserId, User.Role currentUserRole) {
        if (currentUserRole == User.Role.ADMIN) {
            return; // ADMIN puede actualizar cualquier horario
        }

        if (currentUserRole == User.Role.BARBER && schedule.getBarber().getId().equals(currentUserId)) {
            return; // BARBER puede actualizar sus propios horarios
        }

        throw new UnauthorizedScheduleAccessException(schedule.getId(), currentUserId, "actualizar");
    }

    private void validateDeleteAuthorization(WorkSchedule schedule, Long currentUserId, User.Role currentUserRole) {
        if (currentUserRole == User.Role.ADMIN) {
            return; // ADMIN puede eliminar cualquier horario
        }

        if (currentUserRole == User.Role.BARBER && schedule.getBarber().getId().equals(currentUserId)) {
            return; // BARBER puede eliminar sus propios horarios
        }

        throw new UnauthorizedScheduleAccessException(schedule.getId(), currentUserId, "eliminar");
    }

    private User validateBarber(Long barberId) {
        User barber = userRepository.findById(barberId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", barberId));

        if (barber.getRole() != User.Role.BARBER && barber.getRole() != User.Role.ADMIN) {
            throw new InvalidScheduleException("rol de usuario", barber.getRole().name(), 
                    "debe ser BARBER o ADMIN para tener horarios de trabajo");
        }

        return barber;
    }

    private void validateScheduleTimes(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new InvalidScheduleException("Las horas de inicio y fin son obligatorias");
        }

        if (!startTime.isBefore(endTime)) {
            throw new InvalidScheduleException(startTime, endTime);
        }

        // Validar que el horario esté dentro de 24 horas
        if (startTime.isAfter(LocalTime.of(23, 59)) || endTime.isBefore(LocalTime.of(0, 1))) {
            throw new InvalidScheduleException("horario", startTime + "-" + endTime, 
                    "debe estar dentro del mismo día (00:01 - 23:59)");
        }
    }

    // Métodos de conversión

    private WorkScheduleDto convertToDto(WorkSchedule schedule) {
        return WorkScheduleDto.builder()
                .id(schedule.getId())
                .barberId(schedule.getBarber().getId())
                .barberUsername(schedule.getBarber().getUsername())
                .barberFullName(schedule.getBarber().getFirstName() + " " + schedule.getBarber().getLastName())
                .dayOfWeek(schedule.getDayOfWeek())
                .dayDisplayName(schedule.getDayOfWeek().getDisplayName())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .active(schedule.getActive())
                .workingHours(schedule.getWorkingHours())
                .workingMinutes(schedule.getWorkingMinutes())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }

    private WorkSchedule convertToEntity(WorkScheduleCreateRequest request, User barber) {
        return WorkSchedule.builder()
                .barber(barber)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
    }

    private void updateScheduleFields(WorkSchedule schedule, WorkScheduleUpdateRequest request) {
        if (request.getDayOfWeek() != null) {
            schedule.setDayOfWeek(request.getDayOfWeek());
        }
        if (request.getStartTime() != null) {
            schedule.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            schedule.setEndTime(request.getEndTime());
        }
        if (request.getActive() != null) {
            schedule.setActive(request.getActive());
        }
    }
}
