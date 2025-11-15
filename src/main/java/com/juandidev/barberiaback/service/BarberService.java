package com.juandidev.barberiaback.service;

import com.juandidev.barberiaback.dto.BarberCreateRequest;
import com.juandidev.barberiaback.dto.BarberDto;
import com.juandidev.barberiaback.dto.BarberUpdateRequest;
import com.juandidev.barberiaback.exception.EntityNotFoundException;
import com.juandidev.barberiaback.exception.UserAlreadyExistsException;
import com.juandidev.barberiaback.model.Barber;
import com.juandidev.barberiaback.model.User;
import com.juandidev.barberiaback.repository.BarberRepository;
import com.juandidev.barberiaback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BarberService {

    private final BarberRepository barberRepository;
    private final UserRepository userRepository;

    public List<BarberDto> getAllActiveBarbers() {
        log.info("Obteniendo todos los barberos activos");
        
        List<Barber> activeBarbers = barberRepository.findByActiveTrue();
        
        return activeBarbers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BarberDto> getAllAvailableBarbers() {
        log.info("Obteniendo barberos disponibles");
        
        List<Barber> availableBarbers = barberRepository.findByActiveTrueAndAvailableTrue();
        
        return availableBarbers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<BarberDto> getBarberById(Long id) {
        log.info("Buscando barbero con ID: {}", id);
        
        return barberRepository.findById(id)
                .map(this::convertToDto);
    }

    public Optional<BarberDto> getBarberByUserId(Long userId) {
        log.info("Buscando barbero por ID de usuario: {}", userId);
        
        return barberRepository.findByUserId(userId)
                .map(this::convertToDto);
    }

    public BarberDto createBarber(BarberCreateRequest request) {
        log.info("Creando nuevo barbero para usuario ID: {}", request.getUserId());
        
        // Verificar que el usuario existe
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario", request.getUserId()));
        
        // Verificar que el usuario tiene rol BARBER o ADMIN
        if (user.getRole() != User.Role.BARBER && user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("El usuario debe tener rol BARBER o ADMIN para ser barbero");
        }
        
        // Verificar que no existe ya un barbero para este usuario
        if (barberRepository.existsByUserId(request.getUserId())) {
            throw new UserAlreadyExistsException("Ya existe un barbero asociado al usuario con ID: " + request.getUserId());
        }
        
        // Crear el barbero
        Barber barber = convertToEntity(request, user);
        Barber savedBarber = barberRepository.save(barber);
        
        log.info("Barbero creado exitosamente con ID: {} para usuario: {}", 
                savedBarber.getId(), user.getUsername());
        
        return convertToDto(savedBarber);
    }

    public Optional<BarberDto> updateBarber(Long id, BarberUpdateRequest request) {
        log.info("Actualizando barbero con ID: {}", id);
        
        return barberRepository.findById(id)
                .map(existingBarber -> {
                    updateBarberFields(existingBarber, request);
                    Barber updatedBarber = barberRepository.save(existingBarber);
                    log.info("Barbero actualizado exitosamente: {}", updatedBarber.getFullName());
                    return convertToDto(updatedBarber);
                });
    }

    public boolean deleteBarber(Long id) {
        log.info("Eliminando barbero con ID: {}", id);
        
        return barberRepository.findById(id)
                .map(barber -> {
                    barber.setActive(false);
                    barber.setAvailable(false);
                    barberRepository.save(barber);
                    log.info("Barbero marcado como inactivo: {}", barber.getFullName());
                    return true;
                })
                .orElse(false);
    }

    public List<BarberDto> getAvailableBarbersAtTime(LocalTime time) {
        log.info("Buscando barberos disponibles a las: {}", time);
        
        List<Barber> availableBarbers = barberRepository.findAvailableBarbersAtTime(time);
        
        return availableBarbers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BarberDto> getBarbersBySpecialty(String specialty) {
        log.info("Buscando barberos con especialidad: {}", specialty);
        
        List<Barber> barbers = barberRepository.findBySpecialtyContainingIgnoreCase(specialty);
        
        return barbers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BarberDto> getBarbersByMinimumExperience(Integer minYears) {
        log.info("Buscando barberos con experiencia mínima: {} años", minYears);
        
        List<Barber> barbers = barberRepository.findByMinimumExperience(minYears);
        
        return barbers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public boolean existsByUserId(Long userId) {
        log.info("Verificando si existe barbero para usuario ID: {}", userId);
        return barberRepository.existsByUserId(userId);
    }

    public boolean updateAvailability(Long id, boolean available) {
        log.info("Actualizando disponibilidad del barbero ID: {} a: {}", id, available);
        
        return barberRepository.findById(id)
                .map(barber -> {
                    barber.setAvailable(available);
                    barberRepository.save(barber);
                    log.info("Disponibilidad actualizada para barbero: {}", barber.getFullName());
                    return true;
                })
                .orElse(false);
    }

    // Métodos privados para conversión
    private BarberDto convertToDto(Barber barber) {
        return BarberDto.builder()
                .id(barber.getId())
                .userId(barber.getUser().getId())
                .username(barber.getUsername())
                .firstName(barber.getUser().getFirstName())
                .lastName(barber.getUser().getLastName())
                .email(barber.getEmail())
                .specialties(barber.getSpecialties())
                .experienceYears(barber.getExperienceYears())
                .phoneNumber(barber.getPhoneNumber())
                .startTime(barber.getStartTime())
                .endTime(barber.getEndTime())
                .available(barber.getAvailable())
                .active(barber.getActive())
                .createdAt(barber.getCreatedAt())
                .updatedAt(barber.getUpdatedAt())
                .build();
    }

    private Barber convertToEntity(BarberCreateRequest request, User user) {
        return Barber.builder()
                .user(user)
                .specialties(request.getSpecialties())
                .experienceYears(request.getExperienceYears())
                .phoneNumber(request.getPhoneNumber())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .available(true) // Por defecto disponible
                .active(true) // Por defecto activo
                .build();
    }

    private void updateBarberFields(Barber barber, BarberUpdateRequest request) {
        if (request.getSpecialties() != null) {
            barber.setSpecialties(request.getSpecialties());
        }
        if (request.getExperienceYears() != null) {
            barber.setExperienceYears(request.getExperienceYears());
        }
        if (request.getPhoneNumber() != null) {
            barber.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getStartTime() != null) {
            barber.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            barber.setEndTime(request.getEndTime());
        }
        if (request.getAvailable() != null) {
            barber.setAvailable(request.getAvailable());
        }
        if (request.getActive() != null) {
            barber.setActive(request.getActive());
        }
    }
}
