package com.juandidev.barberiaback.service;

import com.juandidev.barberiaback.dto.ServiceCreateRequest;
import com.juandidev.barberiaback.dto.ServiceDto;
import com.juandidev.barberiaback.dto.ServiceUpdateRequest;
import com.juandidev.barberiaback.model.Service;
import com.juandidev.barberiaback.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceService - Pruebas Unitarias")
class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceService serviceService;

    private Service testService;
    private ServiceCreateRequest createRequest;
    private ServiceUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testService = Service.builder()
                .id(1L)
                .name("Corte de Cabello")
                .duration(30)
                .price(25.0)
                .description("Corte de cabello profesional")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = new ServiceCreateRequest(
                "Corte de Cabello",
                30,
                25.0,
                "Corte de cabello profesional"
        );

        updateRequest = new ServiceUpdateRequest(
                "Corte Premium",
                45,
                35.0,
                "Corte de cabello premium",
                true
        );
    }

    @Test
    @DisplayName("Debe obtener todos los servicios activos ordenados por nombre")
    void shouldGetAllActiveServicesOrderedByName() {
        // Given
        Service service1 = Service.builder().id(1L).name("Barba").active(true).build();
        Service service2 = Service.builder().id(2L).name("Corte").active(true).build();
        List<Service> mockServices = Arrays.asList(service1, service2);

        when(serviceRepository.findByActiveTrueOrderByNameAsc()).thenReturn(mockServices);

        // When
        List<ServiceDto> result = serviceService.getAllActiveServices();

        // Then
        assertThat(result).hasSize(2);
        verify(serviceRepository).findByActiveTrueOrderByNameAsc();
    }

    @Test
    @DisplayName("Debe obtener servicio por ID cuando existe")
    void shouldGetServiceByIdWhenExists() {
        // Given
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));

        // When
        Optional<ServiceDto> result = serviceService.getServiceById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Corte de Cabello");
        verify(serviceRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando el servicio no existe")
    void shouldReturnEmptyOptionalWhenServiceNotExists() {
        // Given
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<ServiceDto> result = serviceService.getServiceById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(serviceRepository).findById(999L);
    }

    @Test
    @DisplayName("Debe crear servicio exitosamente cuando el nombre no existe")
    void shouldCreateServiceSuccessfullyWhenNameNotExists() {
        // Given
        when(serviceRepository.existsByNameAndActiveTrue("Corte de Cabello")).thenReturn(false);
        when(serviceRepository.save(any(Service.class))).thenReturn(testService);

        // When
        ServiceDto result = serviceService.createService(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Corte de Cabello");
        assertThat(result.getActive()).isTrue();
        verify(serviceRepository).existsByNameAndActiveTrue("Corte de Cabello");
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el nombre del servicio ya existe")
    void shouldThrowExceptionWhenServiceNameAlreadyExists() {
        // Given
        when(serviceRepository.existsByNameAndActiveTrue("Corte de Cabello")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> serviceService.createService(createRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe un servicio activo con el nombre: Corte de Cabello");

        verify(serviceRepository).existsByNameAndActiveTrue("Corte de Cabello");
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    @DisplayName("Debe actualizar servicio exitosamente cuando existe")
    void shouldUpdateServiceSuccessfullyWhenExists() {
        // Given
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(Service.class))).thenReturn(testService);

        // When
        Optional<ServiceDto> result = serviceService.updateService(1L, updateRequest);

        // Then
        assertThat(result).isPresent();
        verify(serviceRepository).findById(1L);
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    @DisplayName("Debe retornar Optional vacío al actualizar servicio inexistente")
    void shouldReturnEmptyOptionalWhenUpdatingNonExistentService() {
        // Given
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<ServiceDto> result = serviceService.updateService(999L, updateRequest);

        // Then
        assertThat(result).isEmpty();
        verify(serviceRepository).findById(999L);
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    @DisplayName("Debe realizar eliminación lógica marcando como inactivo")
    void shouldPerformLogicalDeletionByMarkingAsInactive() {
        // Given
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(Service.class))).thenReturn(testService);

        // When
        boolean result = serviceService.deleteService(1L);

        // Then
        assertThat(result).isTrue();
        verify(serviceRepository).findById(1L);
        verify(serviceRepository).save(argThat(service -> !service.getActive()));
    }

    @Test
    @DisplayName("Debe retornar false al eliminar servicio inexistente")
    void shouldReturnFalseWhenDeletingNonExistentService() {
        // Given
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        boolean result = serviceService.deleteService(999L);

        // Then
        assertThat(result).isFalse();
        verify(serviceRepository).findById(999L);
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    @DisplayName("Debe buscar servicios por rango de precios")
    void shouldFindServicesByPriceRange() {
        // Given
        List<Service> mockServices = Arrays.asList(testService);
        when(serviceRepository.findActiveServicesByPriceRange(20.0, 30.0)).thenReturn(mockServices);

        // When
        List<ServiceDto> result = serviceService.getServicesByPriceRange(20.0, 30.0);

        // Then
        assertThat(result).hasSize(1);
        verify(serviceRepository).findActiveServicesByPriceRange(20.0, 30.0);
    }

    @Test
    @DisplayName("Debe buscar servicios por duración máxima")
    void shouldFindServicesByMaxDuration() {
        // Given
        List<Service> mockServices = Arrays.asList(testService);
        when(serviceRepository.findActiveServicesByMaxDuration(45)).thenReturn(mockServices);

        // When
        List<ServiceDto> result = serviceService.getServicesByMaxDuration(45);

        // Then
        assertThat(result).hasSize(1);
        verify(serviceRepository).findActiveServicesByMaxDuration(45);
    }

    @Test
    @DisplayName("Debe verificar existencia por nombre")
    void shouldCheckExistenceByName() {
        // Given
        when(serviceRepository.existsByNameAndActiveTrue("Corte de Cabello")).thenReturn(true);

        // When
        boolean result = serviceService.existsByName("Corte de Cabello");

        // Then
        assertThat(result).isTrue();
        verify(serviceRepository).existsByNameAndActiveTrue("Corte de Cabello");
    }
}
