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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BarberService - Pruebas Unitarias")
class BarberServiceTest {

    @Mock
    private BarberRepository barberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BarberService barberService;

    private User testUser;
    private Barber testBarber;
    private BarberCreateRequest createRequest;
    private BarberUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("juan.barber")
                .email("juan@barberia.com")
                .firstName("Juan")
                .lastName("Pérez")
                .role(User.Role.BARBER)
                .enabled(true)
                .build();

        testBarber = Barber.builder()
                .id(1L)
                .user(testUser)
                .specialties("Corte, Barba, Peinado")
                .experienceYears(5)
                .phoneNumber("555-0123")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .available(true)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = new BarberCreateRequest(
                1L,
                "Corte, Barba, Peinado",
                5,
                "555-0123",
                LocalTime.of(9, 0),
                LocalTime.of(18, 0)
        );

        updateRequest = new BarberUpdateRequest(
                "Corte Premium, Barba, Peinado",
                7,
                "555-0456",
                LocalTime.of(8, 0),
                LocalTime.of(19, 0),
                true,
                true
        );
    }

    @Test
    @DisplayName("Debe obtener todos los barberos activos")
    void shouldGetAllActiveBarbers() {
        // Given
        List<Barber> mockBarbers = Arrays.asList(testBarber);
        when(barberRepository.findByActiveTrue()).thenReturn(mockBarbers);

        // When
        List<BarberDto> result = barberService.getAllActiveBarbers();

        // Then
        assertThat(result).hasSize(1);
        verify(barberRepository).findByActiveTrue();
    }

    @Test
    @DisplayName("Debe obtener todos los barberos disponibles")
    void shouldGetAllAvailableBarbers() {
        // Given
        List<Barber> mockBarbers = Arrays.asList(testBarber);
        when(barberRepository.findByActiveTrueAndAvailableTrue()).thenReturn(mockBarbers);

        // When
        List<BarberDto> result = barberService.getAllAvailableBarbers();

        // Then
        assertThat(result).hasSize(1);
        verify(barberRepository).findByActiveTrueAndAvailableTrue();
    }

    @Test
    @DisplayName("Debe obtener barbero por ID cuando existe")
    void shouldGetBarberByIdWhenExists() {
        // Given
        when(barberRepository.findById(1L)).thenReturn(Optional.of(testBarber));

        // When
        Optional<BarberDto> result = barberService.getBarberById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("juan.barber");
        verify(barberRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe crear barbero exitosamente cuando el usuario es válido")
    void shouldCreateBarberSuccessfullyWhenUserIsValid() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(barberRepository.existsByUserId(1L)).thenReturn(false);
        when(barberRepository.save(any(Barber.class))).thenReturn(testBarber);

        // When
        BarberDto result = barberService.createBarber(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("juan.barber");
        verify(userRepository).findById(1L);
        verify(barberRepository).existsByUserId(1L);
        verify(barberRepository).save(any(Barber.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no existe")
    void shouldThrowExceptionWhenUserNotExists() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> barberService.createBarber(createRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Usuario con ID 999 no encontrado");

        verify(userRepository).findById(999L);
        verify(barberRepository, never()).save(any(Barber.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no tiene rol BARBER o ADMIN")
    void shouldThrowExceptionWhenUserHasInvalidRole() {
        // Given
        User clientUser = User.builder()
                .id(1L)
                .role(User.Role.CLIENT)
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(clientUser));

        // When & Then
        assertThatThrownBy(() -> barberService.createBarber(createRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El usuario debe tener rol BARBER o ADMIN para ser barbero");

        verify(userRepository).findById(1L);
        verify(barberRepository, never()).save(any(Barber.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando ya existe un barbero para el usuario")
    void shouldThrowExceptionWhenBarberAlreadyExistsForUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(barberRepository.existsByUserId(1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> barberService.createBarber(createRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Ya existe un barbero asociado al usuario con ID: 1");

        verify(userRepository).findById(1L);
        verify(barberRepository).existsByUserId(1L);
        verify(barberRepository, never()).save(any(Barber.class));
    }

    @Test
    @DisplayName("Debe actualizar barbero exitosamente cuando existe")
    void shouldUpdateBarberSuccessfullyWhenExists() {
        // Given
        when(barberRepository.findById(1L)).thenReturn(Optional.of(testBarber));
        when(barberRepository.save(any(Barber.class))).thenReturn(testBarber);

        // When
        Optional<BarberDto> result = barberService.updateBarber(1L, updateRequest);

        // Then
        assertThat(result).isPresent();
        verify(barberRepository).findById(1L);
        verify(barberRepository).save(any(Barber.class));
    }

    @Test
    @DisplayName("Debe realizar eliminación lógica marcando como inactivo y no disponible")
    void shouldPerformLogicalDeletionByMarkingAsInactiveAndUnavailable() {
        // Given
        when(barberRepository.findById(1L)).thenReturn(Optional.of(testBarber));
        when(barberRepository.save(any(Barber.class))).thenReturn(testBarber);

        // When
        boolean result = barberService.deleteBarber(1L);

        // Then
        assertThat(result).isTrue();
        verify(barberRepository).findById(1L);
        verify(barberRepository).save(argThat(barber -> 
            !barber.getActive() && !barber.getAvailable()));
    }

    @Test
    @DisplayName("Debe retornar false al eliminar barbero inexistente")
    void shouldReturnFalseWhenDeletingNonExistentBarber() {
        // Given
        when(barberRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        boolean result = barberService.deleteBarber(999L);

        // Then
        assertThat(result).isFalse();
        verify(barberRepository).findById(999L);
        verify(barberRepository, never()).save(any(Barber.class));
    }

    @Test
    @DisplayName("Debe actualizar disponibilidad del barbero")
    void shouldUpdateBarberAvailability() {
        // Given
        when(barberRepository.findById(1L)).thenReturn(Optional.of(testBarber));
        when(barberRepository.save(any(Barber.class))).thenReturn(testBarber);

        // When
        boolean result = barberService.updateAvailability(1L, false);

        // Then
        assertThat(result).isTrue();
        verify(barberRepository).findById(1L);
        verify(barberRepository).save(argThat(barber -> !barber.getAvailable()));
    }

    @Test
    @DisplayName("Debe buscar barberos disponibles en horario específico")
    void shouldFindAvailableBarbersAtTime() {
        // Given
        LocalTime testTime = LocalTime.of(10, 0);
        List<Barber> mockBarbers = Arrays.asList(testBarber);
        when(barberRepository.findAvailableBarbersAtTime(testTime)).thenReturn(mockBarbers);

        // When
        List<BarberDto> result = barberService.getAvailableBarbersAtTime(testTime);

        // Then
        assertThat(result).hasSize(1);
        verify(barberRepository).findAvailableBarbersAtTime(testTime);
    }

    @Test
    @DisplayName("Debe buscar barberos por especialidad")
    void shouldFindBarbersBySpecialty() {
        // Given
        String specialty = "Corte";
        List<Barber> mockBarbers = Arrays.asList(testBarber);
        when(barberRepository.findBySpecialtyContainingIgnoreCase(specialty)).thenReturn(mockBarbers);

        // When
        List<BarberDto> result = barberService.getBarbersBySpecialty(specialty);

        // Then
        assertThat(result).hasSize(1);
        verify(barberRepository).findBySpecialtyContainingIgnoreCase(specialty);
    }

    @Test
    @DisplayName("Debe buscar barberos por experiencia mínima")
    void shouldFindBarbersByMinimumExperience() {
        // Given
        Integer minYears = 3;
        List<Barber> mockBarbers = Arrays.asList(testBarber);
        when(barberRepository.findByMinimumExperience(minYears)).thenReturn(mockBarbers);

        // When
        List<BarberDto> result = barberService.getBarbersByMinimumExperience(minYears);

        // Then
        assertThat(result).hasSize(1);
        verify(barberRepository).findByMinimumExperience(minYears);
    }

    @Test
    @DisplayName("Debe verificar existencia por ID de usuario")
    void shouldCheckExistenceByUserId() {
        // Given
        when(barberRepository.existsByUserId(1L)).thenReturn(true);

        // When
        boolean result = barberService.existsByUserId(1L);

        // Then
        assertThat(result).isTrue();
        verify(barberRepository).existsByUserId(1L);
    }

    @Test
    @DisplayName("Debe permitir crear barbero con usuario ADMIN")
    void shouldAllowCreatingBarberWithAdminUser() {
        // Given
        User adminUser = User.builder()
                .id(1L)
                .username("admin")
                .role(User.Role.ADMIN)
                .build();
        
        Barber adminBarber = Barber.builder()
                .id(1L)
                .user(adminUser)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(barberRepository.existsByUserId(1L)).thenReturn(false);
        when(barberRepository.save(any(Barber.class))).thenReturn(adminBarber);

        // When
        BarberDto result = barberService.createBarber(createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(barberRepository).existsByUserId(1L);
        verify(barberRepository).save(any(Barber.class));
    }
}
