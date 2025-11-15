package com.juandidev.barberiaback.service;

import com.juandidev.barberiaback.dto.AppointmentCreateRequest;
import com.juandidev.barberiaback.dto.AppointmentDto;
import com.juandidev.barberiaback.dto.AppointmentUpdateRequest;
import com.juandidev.barberiaback.exception.*;
import com.juandidev.barberiaback.model.Appointment;
import com.juandidev.barberiaback.model.AppointmentStatus;
import com.juandidev.barberiaback.model.Barber;
import com.juandidev.barberiaback.model.User;
import com.juandidev.barberiaback.repository.AppointmentRepository;
import com.juandidev.barberiaback.repository.BarberRepository;
import com.juandidev.barberiaback.repository.ServiceRepository;
import com.juandidev.barberiaback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;

    public List<AppointmentDto> getAllAppointments() {
        // TODO: Implementar obtención de todas las citas
        log.info("Obteniendo todas las citas");
        return List.of(); // Placeholder
    }

    public List<AppointmentDto> getAppointmentsByClient(Long clientId) {
        log.info("Obteniendo citas del cliente ID: {}", clientId);
        
        List<Appointment> appointments = appointmentRepository.findByClientId(clientId);
        
        return appointments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<AppointmentDto> getAppointmentsByBarber(Long barberId) {
        log.info("Obteniendo citas del barbero ID: {}", barberId);
        
        List<Appointment> appointments = appointmentRepository.findByBarberId(barberId);
        
        return appointments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<AppointmentDto> getAppointmentsByUser(Long userId, User.Role role, AppointmentStatus status) {
        log.info("Obteniendo citas para usuario ID: {} con rol: {} y estado: {}", userId, role, status);
        
        List<Appointment> appointments;
        
        switch (role) {
            case CLIENT:
                // Cliente: Ver solo sus propias citas
                if (status != null) {
                    appointments = appointmentRepository.findByClientIdAndStatus(userId, status);
                } else {
                    appointments = appointmentRepository.findByClientId(userId);
                }
                break;
                
            case BARBER:
                // Barbero: Ver solo las citas asignadas a él
                Optional<Barber> barber = barberRepository.findByUserId(userId);
                if (barber.isEmpty()) {
                    log.warn("No se encontró barbero para usuario ID: {}", userId);
                    return List.of();
                }
                
                if (status != null) {
                    appointments = appointmentRepository.findByBarberIdAndStatus(barber.get().getId(), status);
                } else {
                    appointments = appointmentRepository.findByBarberId(barber.get().getId());
                }
                break;
                
            case ADMIN:
                // Admin: Ver todas las citas (filtradas opcionalmente por estado)
                if (status != null) {
                    appointments = appointmentRepository.findByStatus(status);
                } else {
                    appointments = appointmentRepository.findAll();
                }
                break;
                
            default:
                throw new UnauthorizedAppointmentAccessException("Rol no autorizado para consultar citas: " + role);
        }
        
        return appointments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<AppointmentDto> getAppointmentsByStatus(AppointmentStatus status) {
        // TODO: Implementar obtención de citas por estado
        log.info("Obteniendo citas con estado: {}", status);
        return List.of(); // Placeholder
    }

    public Optional<AppointmentDto> getAppointmentById(Long id, Long currentUserId, User.Role currentUserRole) {
        log.info("Buscando cita con ID: {} por usuario ID: {} con rol: {}", id, currentUserId, currentUserRole);
        
        return appointmentRepository.findById(id)
                .map(appointment -> {
                    // Validar autorización para ver la cita
                    validateViewAuthorization(appointment, currentUserId, currentUserRole);
                    return convertToDto(appointment);
                });
    }

    public List<AppointmentDto> getAppointmentsForCurrentUser(Long currentUserId, User.Role currentUserRole, AppointmentStatus status) {
        log.info("Obteniendo citas para usuario ID: {} con rol: {} y estado: {}", currentUserId, currentUserRole, status);
        
        return getAppointmentsByUser(currentUserId, currentUserRole, status);
    }

    @Transactional
    public AppointmentDto createAppointment(AppointmentCreateRequest request, Long currentUserId, User.Role currentUserRole) {
        log.info("Creando nueva cita para cliente ID: {} con barbero ID: {} en horario: {} por usuario ID: {}", 
                request.getClientId(), request.getBarberId(), request.getStartTime(), currentUserId);
        
        // 1. Validar autorización - Solo ADMIN o el propio cliente pueden crear citas
        validateCreateAuthorization(request.getClientId(), currentUserId, currentUserRole);
        
        // 2. Validar que la hora de inicio no sea en el pasado
        validateAppointmentTime(request.getStartTime());
        
        // 3. Obtener y validar entidades relacionadas
        User client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente", request.getClientId()));
        
        Barber barber = barberRepository.findById(request.getBarberId())
                .orElseThrow(() -> new EntityNotFoundException("Barbero", request.getBarberId()));
        
        com.juandidev.barberiaback.model.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new EntityNotFoundException("Servicio", request.getServiceId()));
        
        // 4. Validar que el barbero esté activo y disponible
        validateBarberAvailability(barber);
        
        // 5. Calcular hora de fin basada en la duración del servicio
        LocalDateTime endTime = calculateEndTime(request.getStartTime(), service.getDuration());
        
        // 6. VALIDACIÓN CRÍTICA: Verificar conflictos de horario (validación final para race conditions)
        boolean hasConflicts = hasConflictingAppointments(barber.getId(), request.getStartTime(), endTime);
        
        if (hasConflicts) {
            log.warn("Race condition detectada: Conflicto de horario para barbero ID: {} en horario: {} - {}", 
                    barber.getId(), request.getStartTime(), endTime);
            throw new AppointmentConflictException(
                    "El horario seleccionado ya no está disponible. Por favor, seleccione otro horario.");
        }
        
        // 7. Validar que la cita esté dentro del horario de trabajo del barbero
        validateBarberWorkingHours(barber, request.getStartTime(), endTime);
        
        // 8. Crear y guardar la cita con estado PENDING por defecto
        Appointment appointment = Appointment.builder()
                .client(client)
                .barber(barber)
                .service(service)
                .startTime(request.getStartTime())
                .endTime(endTime)
                .status(AppointmentStatus.PENDING)
                .notes(request.getNotes())
                .totalPrice(service.getPrice())
                .build();
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        log.info("Cita creada exitosamente con ID: {} para cliente: {} con barbero: {}", 
                savedAppointment.getId(), client.getUsername(), barber.getFullName());
        
        return convertToDto(savedAppointment);
    }

    public Optional<AppointmentDto> updateAppointment(Long id, AppointmentUpdateRequest request) {
        // TODO: Implementar actualización de cita
        log.info("Actualizando cita con ID: {}", id);
        return Optional.empty(); // Placeholder
    }

    @Transactional
    public boolean cancelAppointment(Long id, Long userId, User.Role role) {
        log.info("Cancelando cita con ID: {} por usuario ID: {} con rol: {}", id, userId, role);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cita", id));
        
        // Validar autorización
        validateCancelAuthorization(appointment, userId, role);
        
        // Validar estado de la cita
        if (appointment.getStatus() != AppointmentStatus.PENDING && 
            appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentStatusException(id, appointment.getStatus(), "cancelar");
        }
        
        // Cambiar estado a CANCELLED
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        
        log.info("Cita con ID: {} cancelada exitosamente por usuario ID: {}", id, userId);
        return true;
    }

    @Transactional
    public boolean confirmAppointment(Long id, Long userId, User.Role role) {
        log.info("Confirmando cita con ID: {} por usuario ID: {} con rol: {}", id, userId, role);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cita", id));
        
        // Validar autorización (solo BARBER asociado o ADMIN)
        validateConfirmAuthorization(appointment, userId, role);
        
        // Validar estado de la cita (solo PENDING puede ser confirmada)
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new InvalidAppointmentStatusException(id, appointment.getStatus(), 
                    AppointmentStatus.PENDING, "confirmar");
        }
        
        // Cambiar estado a CONFIRMED
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
        
        log.info("Cita con ID: {} confirmada exitosamente por usuario ID: {}", id, userId);
        return true;
    }

    @Transactional
    public boolean completeAppointment(Long id, Long userId, User.Role role) {
        log.info("Completando cita con ID: {} por usuario ID: {} con rol: {}", id, userId, role);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cita", id));
        
        // Validar autorización (solo BARBER asociado o ADMIN)
        validateCompleteAuthorization(appointment, userId, role);
        
        // Validar estado de la cita (solo CONFIRMED puede ser completada)
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentStatusException(id, appointment.getStatus(), 
                    AppointmentStatus.CONFIRMED, "completar");
        }
        
        // Cambiar estado a COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        
        log.info("Cita con ID: {} completada exitosamente por usuario ID: {}", id, userId);
        return true;
    }

    public List<AppointmentDto> getAppointmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: Implementar búsqueda por rango de fechas
        log.info("Buscando citas entre {} y {}", startDate, endDate);
        return List.of(); // Placeholder
    }

    public List<AppointmentDto> getBarberAppointmentsByDateRange(Long barberId, 
                                                                LocalDateTime startDate, 
                                                                LocalDateTime endDate) {
        // TODO: Implementar búsqueda de citas de barbero por rango de fechas
        log.info("Buscando citas del barbero ID: {} entre {} y {}", barberId, startDate, endDate);
        return List.of(); // Placeholder
    }

    public boolean hasConflictingAppointments(Long barberId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Verificando conflictos para barbero ID: {} entre {} y {}", barberId, startTime, endTime);
        
        List<AppointmentStatus> activeStatuses = Arrays.asList(
                AppointmentStatus.PENDING, 
                AppointmentStatus.CONFIRMED
        );
        
        List<Appointment> conflictingAppointments = appointmentRepository
                .findConflictingAppointments(barberId, startTime, endTime, activeStatuses);
        
        boolean hasConflicts = !conflictingAppointments.isEmpty();
        
        if (hasConflicts) {
            log.warn("Encontrados {} conflictos de horario para barbero ID: {}", 
                    conflictingAppointments.size(), barberId);
        }
        
        return hasConflicts;
    }

    public Long getCompletedAppointmentsCount(Long barberId, LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: Implementar conteo de citas completadas
        log.info("Contando citas completadas del barbero ID: {} entre {} y {}", barberId, startDate, endDate);
        return 0L; // Placeholder
    }

    // Métodos privados para conversión y validación
    private AppointmentDto convertToDto(Appointment appointment) {
        return AppointmentDto.builder()
                .id(appointment.getId())
                .clientId(appointment.getClient().getId())
                .clientName(appointment.getClientFullName())
                .clientEmail(appointment.getClient().getEmail())
                .barberId(appointment.getBarber().getId())
                .barberName(appointment.getBarberFullName())
                .serviceId(appointment.getService().getId())
                .serviceName(appointment.getServiceName())
                .serviceDuration(appointment.getService().getDuration())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .totalPrice(appointment.getTotalPrice())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }

    private void validateAppointmentTime(LocalDateTime startTime) {
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new InvalidAppointmentTimeException(startTime);
        }
    }

    private void validateBarberAvailability(Barber barber) {
        if (!barber.getActive() || !barber.getAvailable()) {
            throw new BarberNotAvailableException(barber.getId());
        }
    }

    private void validateBarberWorkingHours(Barber barber, LocalDateTime startTime, LocalDateTime endTime) {
        LocalTime appointmentStartTime = startTime.toLocalTime();
        LocalTime appointmentEndTime = endTime.toLocalTime();
        
        LocalTime barberStartTime = barber.getStartTime();
        LocalTime barberEndTime = barber.getEndTime();
        
        if (barberStartTime == null || barberEndTime == null) {
            log.warn("Barbero ID: {} no tiene horarios de trabajo configurados", barber.getId());
            return; // Si no hay horarios configurados, permitir la cita
        }
        
        if (appointmentStartTime.isBefore(barberStartTime) || 
            appointmentEndTime.isAfter(barberEndTime)) {
            throw new BarberNotAvailableException(barber.getId(), startTime, barberStartTime, barberEndTime);
        }
    }

    private void validateNoConflictingAppointments(Long barberId, LocalDateTime startTime, LocalDateTime endTime) {
        if (hasConflictingAppointments(barberId, startTime, endTime)) {
            throw new AppointmentConflictException(barberId, startTime, endTime);
        }
    }

    private LocalDateTime calculateEndTime(LocalDateTime startTime, Integer serviceDuration) {
        return startTime.plusMinutes(serviceDuration);
    }

    // Métodos de validación de autorización
    private void validateCancelAuthorization(Appointment appointment, Long userId, User.Role role) {
        if (role == User.Role.ADMIN) {
            return; // ADMIN puede cancelar cualquier cita
        }
        
        if (role == User.Role.CLIENT) {
            // Cliente solo puede cancelar sus propias citas
            if (!appointment.getClient().getId().equals(userId)) {
                throw new UnauthorizedAppointmentAccessException(appointment.getId(), userId, "cancelar");
            }
            return;
        }
        
        // Otros roles no pueden cancelar citas
        throw new UnauthorizedAppointmentAccessException(appointment.getId(), role.name(), "cancelar");
    }

    private void validateConfirmAuthorization(Appointment appointment, Long userId, User.Role role) {
        if (role == User.Role.ADMIN) {
            return; // ADMIN puede confirmar cualquier cita
        }
        
        if (role == User.Role.BARBER) {
            // Barbero solo puede confirmar sus propias citas
            if (!appointment.getBarber().getUser().getId().equals(userId)) {
                throw new UnauthorizedAppointmentAccessException(appointment.getId(), userId, "confirmar");
            }
            return;
        }
        
        // Otros roles no pueden confirmar citas
        throw new UnauthorizedAppointmentAccessException(appointment.getId(), role.name(), "confirmar");
    }

    private void validateCompleteAuthorization(Appointment appointment, Long userId, User.Role role) {
        if (role == User.Role.ADMIN) {
            return; // ADMIN puede completar cualquier cita
        }
        
        if (role == User.Role.BARBER) {
            // Barbero solo puede completar sus propias citas
            if (!appointment.getBarber().getUser().getId().equals(userId)) {
                throw new UnauthorizedAppointmentAccessException(appointment.getId(), userId, "completar");
            }
            return;
        }
        
        // Otros roles no pueden completar citas
        throw new UnauthorizedAppointmentAccessException(appointment.getId(), role.name(), "completar");
    }

    // Métodos de validación de autorización adicionales

    private void validateCreateAuthorization(Long clientId, Long currentUserId, User.Role currentUserRole) {
        if (currentUserRole == User.Role.ADMIN) {
            return; // ADMIN puede crear citas para cualquier cliente
        }

        if (currentUserRole == User.Role.CLIENT && clientId.equals(currentUserId)) {
            return; // CLIENT puede crear sus propias citas
        }

        throw new UnauthorizedAppointmentAccessException(
                "El usuario con ID " + currentUserId + " no tiene permisos para crear citas para el cliente ID " + clientId);
    }

    private void validateViewAuthorization(Appointment appointment, Long currentUserId, User.Role currentUserRole) {
        if (currentUserRole == User.Role.ADMIN) {
            return; // ADMIN puede ver cualquier cita
        }

        if (currentUserRole == User.Role.CLIENT && appointment.getClient().getId().equals(currentUserId)) {
            return; // CLIENT puede ver sus propias citas
        }

        if (currentUserRole == User.Role.BARBER && appointment.getBarber().getUser().getId().equals(currentUserId)) {
            return; // BARBER puede ver citas asignadas a él
        }

        throw new UnauthorizedAppointmentAccessException(appointment.getId(), currentUserId, "ver");
    }

    /**
     * Obtener citas confirmadas de un barbero en una fecha específica
     * Método requerido por AvailabilityService
     */
    public List<AppointmentDto> getAppointmentsByBarberAndDate(Long barberId, LocalDate date) {
        log.info("Obteniendo citas confirmadas para barbero ID: {} en fecha: {}", barberId, date);
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        List<Appointment> appointments = appointmentRepository.findByBarberIdAndDateRange(
                barberId, startOfDay, endOfDay);
        
        // Filtrar solo citas confirmadas y pendientes (que bloquean el horario)
        List<Appointment> confirmedAppointments = appointments.stream()
                .filter(appointment -> appointment.getStatus() == AppointmentStatus.CONFIRMED || 
                                     appointment.getStatus() == AppointmentStatus.PENDING)
                .collect(Collectors.toList());
        
        log.info("Encontradas {} citas que bloquean horario para barbero ID: {} en fecha: {}", 
                confirmedAppointments.size(), barberId, date);
        
        return confirmedAppointments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
