package com.juandidev.barberiaback.controller;

import com.juandidev.barberiaback.dto.ServiceCreateRequest;
import com.juandidev.barberiaback.dto.ServiceDto;
import com.juandidev.barberiaback.dto.ServiceUpdateRequest;
import com.juandidev.barberiaback.security.JwtUtil;
import com.juandidev.barberiaback.service.ServiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceController.class)
@DisplayName("ServiceController - Pruebas de Integración")
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceService serviceService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private ServiceDto testServiceDto;
    private ServiceCreateRequest createRequest;
    private ServiceUpdateRequest updateRequest;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        testServiceDto = ServiceDto.builder()
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

        // Simular tokens JWT
        adminToken = "admin-jwt-token";
        userToken = "user-jwt-token";
    }

    @Test
    @DisplayName("GET /services - Debe ser accesible sin autenticación (HTTP 200)")
    void shouldGetAllServicesWithoutAuthentication() throws Exception {
        // Given
        List<ServiceDto> services = Arrays.asList(testServiceDto);
        when(serviceService.getAllActiveServices()).thenReturn(services);

        // When & Then
        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Corte de Cabello"));

        verify(serviceService).getAllActiveServices();
    }

    @Test
    @DisplayName("GET /services/{id} - Debe ser accesible sin autenticación")
    void shouldGetServiceByIdWithoutAuthentication() throws Exception {
        // Given
        when(serviceService.getServiceById(1L)).thenReturn(Optional.of(testServiceDto));

        // When & Then
        mockMvc.perform(get("/services/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Corte de Cabello"));

        verify(serviceService).getServiceById(1L);
    }

    @Test
    @DisplayName("GET /services/{id} - Debe retornar 404 cuando el servicio no existe")
    void shouldReturn404WhenServiceNotFound() throws Exception {
        // Given
        when(serviceService.getServiceById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/services/999"))
                .andExpect(status().isNotFound());

        verify(serviceService).getServiceById(999L);
    }

    @Test
    @DisplayName("POST /services - Debe fallar con HTTP 403 para usuarios sin rol ADMIN")
    @WithMockUser(roles = "CLIENT")
    void shouldFailCreateServiceWithoutAdminRole() throws Exception {
        // When & Then
        mockMvc.perform(post("/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(serviceService, never()).createService(any());
    }

    @Test
    @DisplayName("POST /services - Debe fallar con HTTP 401 para usuarios no autenticados")
    void shouldFailCreateServiceWithoutAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(post("/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());

        verify(serviceService, never()).createService(any());
    }

    @Test
    @DisplayName("POST /services - Debe crear servicio exitosamente con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateServiceSuccessfullyWithAdminRole() throws Exception {
        // Given
        when(serviceService.createService(any(ServiceCreateRequest.class))).thenReturn(testServiceDto);

        // When & Then
        mockMvc.perform(post("/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Corte de Cabello"));

        verify(serviceService).createService(any(ServiceCreateRequest.class));
    }

    @Test
    @DisplayName("PUT /services/{id} - Debe fallar con HTTP 403 para usuarios sin rol ADMIN")
    @WithMockUser(roles = "CLIENT")
    void shouldFailUpdateServiceWithoutAdminRole() throws Exception {
        // When & Then
        mockMvc.perform(put("/services/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        verify(serviceService, never()).updateService(anyLong(), any());
    }

    @Test
    @DisplayName("PUT /services/{id} - Debe actualizar servicio exitosamente con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateServiceSuccessfullyWithAdminRole() throws Exception {
        // Given
        when(serviceService.updateService(eq(1L), any(ServiceUpdateRequest.class)))
                .thenReturn(Optional.of(testServiceDto));

        // When & Then
        mockMvc.perform(put("/services/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Corte de Cabello"));

        verify(serviceService).updateService(eq(1L), any(ServiceUpdateRequest.class));
    }

    @Test
    @DisplayName("DELETE /services/{id} - Debe fallar con HTTP 403 para usuarios sin rol ADMIN")
    @WithMockUser(roles = "BARBER")
    void shouldFailDeleteServiceWithoutAdminRole() throws Exception {
        // When & Then
        mockMvc.perform(delete("/services/1"))
                .andExpect(status().isForbidden());

        verify(serviceService, never()).deleteService(anyLong());
    }

    @Test
    @DisplayName("DELETE /services/{id} - Debe eliminar servicio exitosamente con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteServiceSuccessfullyWithAdminRole() throws Exception {
        // Given
        when(serviceService.deleteService(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/services/1"))
                .andExpect(status().isNoContent());

        verify(serviceService).deleteService(1L);
    }

    @Test
    @DisplayName("DELETE /services/{id} - Debe retornar 404 cuando el servicio no existe")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenDeletingNonExistentService() throws Exception {
        // Given
        when(serviceService.deleteService(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/services/999"))
                .andExpect(status().isNotFound());

        verify(serviceService).deleteService(999L);
    }

    @Test
    @DisplayName("GET /services/price-range - Debe ser accesible sin autenticación")
    void shouldGetServicesByPriceRangeWithoutAuthentication() throws Exception {
        // Given
        List<ServiceDto> services = Arrays.asList(testServiceDto);
        when(serviceService.getServicesByPriceRange(20.0, 30.0)).thenReturn(services);

        // When & Then
        mockMvc.perform(get("/services/price-range")
                .param("minPrice", "20.0")
                .param("maxPrice", "30.0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Corte de Cabello"));

        verify(serviceService).getServicesByPriceRange(20.0, 30.0);
    }

    @Test
    @DisplayName("GET /services/max-duration/{duration} - Debe ser accesible sin autenticación")
    void shouldGetServicesByMaxDurationWithoutAuthentication() throws Exception {
        // Given
        List<ServiceDto> services = Arrays.asList(testServiceDto);
        when(serviceService.getServicesByMaxDuration(45)).thenReturn(services);

        // When & Then
        mockMvc.perform(get("/services/max-duration/45"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Corte de Cabello"));

        verify(serviceService).getServicesByMaxDuration(45);
    }

    @Test
    @DisplayName("POST /services - Debe validar campos requeridos")
    @WithMockUser(roles = "ADMIN")
    void shouldValidateRequiredFieldsWhenCreatingService() throws Exception {
        // Given
        ServiceCreateRequest invalidRequest = new ServiceCreateRequest(
                "", // nombre vacío
                -1, // duración negativa
                -10.0, // precio negativo
                null
        );

        // When & Then
        mockMvc.perform(post("/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).createService(any());
    }

    @Test
    @DisplayName("Debe manejar errores de validación correctamente")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleValidationErrorsCorrectly() throws Exception {
        // Given
        ServiceCreateRequest invalidRequest = new ServiceCreateRequest(
                null, // nombre null
                null, // duración null
                null, // precio null
                "Descripción válida"
        );

        // When & Then
        mockMvc.perform(post("/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(serviceService, never()).createService(any());
    }
}
