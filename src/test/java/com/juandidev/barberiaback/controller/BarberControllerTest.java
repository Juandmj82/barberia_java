package com.juandidev.barberiaback.controller;

import com.juandidev.barberiaback.dto.BarberCreateRequest;
import com.juandidev.barberiaback.dto.BarberDto;
import com.juandidev.barberiaback.dto.BarberUpdateRequest;
import com.juandidev.barberiaback.security.JwtUtil;
import com.juandidev.barberiaback.service.BarberService;
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
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BarberController.class)
@DisplayName("BarberController - Pruebas de Integración")
class BarberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BarberService barberService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private BarberDto testBarberDto;
    private BarberCreateRequest createRequest;
    private BarberUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testBarberDto = BarberDto.builder()
                .id(1L)
                .userId(1L)
                .username("juan.barber")
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@barberia.com")
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
    @DisplayName("GET /barbers - Debe ser accesible sin autenticación (HTTP 200)")
    void shouldGetAllAvailableBarbersWithoutAuthentication() throws Exception {
        // Given
        List<BarberDto> barbers = Arrays.asList(testBarberDto);
        when(barberService.getAllAvailableBarbers()).thenReturn(barbers);

        // When & Then
        mockMvc.perform(get("/barbers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("juan.barber"));

        verify(barberService).getAllAvailableBarbers();
    }

    @Test
    @DisplayName("GET /barbers/{id} - Debe ser accesible sin autenticación")
    void shouldGetBarberByIdWithoutAuthentication() throws Exception {
        // Given
        when(barberService.getBarberById(1L)).thenReturn(Optional.of(testBarberDto));

        // When & Then
        mockMvc.perform(get("/barbers/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("juan.barber"));

        verify(barberService).getBarberById(1L);
    }

    @Test
    @DisplayName("GET /barbers/{id} - Debe retornar 404 cuando el barbero no existe")
    void shouldReturn404WhenBarberNotFound() throws Exception {
        // Given
        when(barberService.getBarberById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/barbers/999"))
                .andExpect(status().isNotFound());

        verify(barberService).getBarberById(999L);
    }

    @Test
    @DisplayName("POST /barbers - Debe fallar con HTTP 403 para usuarios sin rol ADMIN")
    @WithMockUser(roles = "CLIENT")
    void shouldFailCreateBarberWithoutAdminRole() throws Exception {
        // When & Then
        mockMvc.perform(post("/barbers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(barberService, never()).createBarber(any());
    }

    @Test
    @DisplayName("POST /barbers - Debe fallar con HTTP 401 para usuarios no autenticados")
    void shouldFailCreateBarberWithoutAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(post("/barbers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());

        verify(barberService, never()).createBarber(any());
    }

    @Test
    @DisplayName("POST /barbers - Debe crear barbero exitosamente con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateBarberSuccessfullyWithAdminRole() throws Exception {
        // Given
        when(barberService.createBarber(any(BarberCreateRequest.class))).thenReturn(testBarberDto);

        // When & Then
        mockMvc.perform(post("/barbers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("juan.barber"));

        verify(barberService).createBarber(any(BarberCreateRequest.class));
    }

    @Test
    @DisplayName("PUT /barbers/{id} - Debe fallar con HTTP 403 para usuarios sin rol ADMIN")
    @WithMockUser(roles = "BARBER")
    void shouldFailUpdateBarberWithoutAdminRole() throws Exception {
        // When & Then
        mockMvc.perform(put("/barbers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        verify(barberService, never()).updateBarber(anyLong(), any());
    }

    @Test
    @DisplayName("PUT /barbers/{id} - Debe actualizar barbero exitosamente con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateBarberSuccessfullyWithAdminRole() throws Exception {
        // Given
        when(barberService.updateBarber(eq(1L), any(BarberUpdateRequest.class)))
                .thenReturn(Optional.of(testBarberDto));

        // When & Then
        mockMvc.perform(put("/barbers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("juan.barber"));

        verify(barberService).updateBarber(eq(1L), any(BarberUpdateRequest.class));
    }

    @Test
    @DisplayName("DELETE /barbers/{id} - Debe fallar con HTTP 403 para usuarios sin rol ADMIN")
    @WithMockUser(roles = "CLIENT")
    void shouldFailDeleteBarberWithoutAdminRole() throws Exception {
        // When & Then
        mockMvc.perform(delete("/barbers/1"))
                .andExpect(status().isForbidden());

        verify(barberService, never()).deleteBarber(anyLong());
    }

    @Test
    @DisplayName("DELETE /barbers/{id} - Debe eliminar barbero exitosamente con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteBarberSuccessfullyWithAdminRole() throws Exception {
        // Given
        when(barberService.deleteBarber(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/barbers/1"))
                .andExpect(status().isNoContent());

        verify(barberService).deleteBarber(1L);
    }

    @Test
    @DisplayName("GET /barbers/all - Debe fallar con HTTP 403 para usuarios sin rol ADMIN")
    @WithMockUser(roles = "CLIENT")
    void shouldFailGetAllActiveBarbersWithoutAdminRole() throws Exception {
        // When & Then
        mockMvc.perform(get("/barbers/all"))
                .andExpect(status().isForbidden());

        verify(barberService, never()).getAllActiveBarbers();
    }

    @Test
    @DisplayName("GET /barbers/all - Debe ser accesible con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllActiveBarbersWithAdminRole() throws Exception {
        // Given
        List<BarberDto> barbers = Arrays.asList(testBarberDto);
        when(barberService.getAllActiveBarbers()).thenReturn(barbers);

        // When & Then
        mockMvc.perform(get("/barbers/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("juan.barber"));

        verify(barberService).getAllActiveBarbers();
    }

    @Test
    @DisplayName("PATCH /barbers/{id}/availability - Debe ser accesible con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateAvailabilityWithAdminRole() throws Exception {
        // Given
        when(barberService.updateAvailability(1L, false)).thenReturn(true);

        // When & Then
        mockMvc.perform(patch("/barbers/1/availability")
                .param("available", "false"))
                .andExpect(status().isNoContent());

        verify(barberService).updateAvailability(1L, false);
    }

    @Test
    @DisplayName("PATCH /barbers/{id}/availability - Debe ser accesible con rol BARBER")
    @WithMockUser(roles = "BARBER")
    void shouldUpdateAvailabilityWithBarberRole() throws Exception {
        // Given
        when(barberService.updateAvailability(1L, true)).thenReturn(true);

        // When & Then
        mockMvc.perform(patch("/barbers/1/availability")
                .param("available", "true"))
                .andExpect(status().isNoContent());

        verify(barberService).updateAvailability(1L, true);
    }

    @Test
    @DisplayName("PATCH /barbers/{id}/availability - Debe fallar con HTTP 403 para rol CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldFailUpdateAvailabilityWithClientRole() throws Exception {
        // When & Then
        mockMvc.perform(patch("/barbers/1/availability")
                .param("available", "false"))
                .andExpect(status().isForbidden());

        verify(barberService, never()).updateAvailability(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("GET /barbers/available-at - Debe ser accesible sin autenticación")
    void shouldGetAvailableBarbersAtTimeWithoutAuthentication() throws Exception {
        // Given
        List<BarberDto> barbers = Arrays.asList(testBarberDto);
        when(barberService.getAvailableBarbersAtTime(any(LocalTime.class))).thenReturn(barbers);

        // When & Then
        mockMvc.perform(get("/barbers/available-at")
                .param("time", "10:00"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("juan.barber"));

        verify(barberService).getAvailableBarbersAtTime(any(LocalTime.class));
    }

    @Test
    @DisplayName("GET /barbers/specialty/{specialty} - Debe ser accesible sin autenticación")
    void shouldGetBarbersBySpecialtyWithoutAuthentication() throws Exception {
        // Given
        List<BarberDto> barbers = Arrays.asList(testBarberDto);
        when(barberService.getBarbersBySpecialty("Corte")).thenReturn(barbers);

        // When & Then
        mockMvc.perform(get("/barbers/specialty/Corte"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("juan.barber"));

        verify(barberService).getBarbersBySpecialty("Corte");
    }

    @Test
    @DisplayName("GET /barbers/experience/{minYears} - Debe ser accesible sin autenticación")
    void shouldGetBarbersByExperienceWithoutAuthentication() throws Exception {
        // Given
        List<BarberDto> barbers = Arrays.asList(testBarberDto);
        when(barberService.getBarbersByMinimumExperience(3)).thenReturn(barbers);

        // When & Then
        mockMvc.perform(get("/barbers/experience/3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("juan.barber"));

        verify(barberService).getBarbersByMinimumExperience(3);
    }

    @Test
    @DisplayName("GET /barbers/exists/user/{userId} - Debe fallar con HTTP 403 para usuarios sin rol ADMIN")
    @WithMockUser(roles = "CLIENT")
    void shouldFailCheckExistenceWithoutAdminRole() throws Exception {
        // When & Then
        mockMvc.perform(get("/barbers/exists/user/1"))
                .andExpect(status().isForbidden());

        verify(barberService, never()).existsByUserId(anyLong());
    }

    @Test
    @DisplayName("GET /barbers/exists/user/{userId} - Debe ser accesible con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldCheckExistenceWithAdminRole() throws Exception {
        // Given
        when(barberService.existsByUserId(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/barbers/exists/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true"));

        verify(barberService).existsByUserId(1L);
    }

    @Test
    @DisplayName("POST /barbers - Debe validar campos requeridos")
    @WithMockUser(roles = "ADMIN")
    void shouldValidateRequiredFieldsWhenCreatingBarber() throws Exception {
        // Given
        BarberCreateRequest invalidRequest = new BarberCreateRequest(
                null, // userId null
                "", // specialties vacío
                -1, // experienceYears negativo
                null, // phoneNumber null
                null, // startTime null
                null  // endTime null
        );

        // When & Then
        mockMvc.perform(post("/barbers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(barberService, never()).createBarber(any());
    }
}
