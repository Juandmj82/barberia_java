package com.juandidev.barberiaback.controller;

import com.juandidev.barberiaback.dto.AppointmentCreateRequest;
import com.juandidev.barberiaback.dto.AppointmentDto;
import com.juandidev.barberiaback.model.AppointmentStatus;
import com.juandidev.barberiaback.model.User;
import com.juandidev.barberiaback.security.JwtUtil;
import com.juandidev.barberiaback.service.AppointmentService;
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

@WebMvcTest(AppointmentController.class)
@DisplayName("AppointmentController - Pruebas de Integración Transaccional")
class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private AppointmentDto testAppointmentDto;
    private AppointmentCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testAppointmentDto = AppointmentDto.builder()
                .id(1L)
                .clientId(1L)
                .clientName("Juan Cliente")
                .barberId(2L)
                .barberName("Pedro Barbero")
                .serviceId(1L)
                .serviceName("Corte de Cabello")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(30))
                .status(AppointmentStatus.PENDING)
                .totalPrice(25.0)
                .build();

        createRequest = new AppointmentCreateRequest(
                1L, // clientId
                2L, // barberId
                1L, // serviceId
                LocalDateTime.now().plusDays(1),
                "Corte regular"
        );
    }

    @Test
    @DisplayName("POST /appointments - Debe crear cita exitosamente con rol CLIENT")
    @WithMockUser(username = "client", roles = "CLIENT")
    void shouldCreateAppointmentSuccessfullyWithClientRole() throws Exception {
        // Given
        when(appointmentService.createAppointment(any(AppointmentCreateRequest.class), eq(1L), eq(User.Role.CLIENT)))
                .thenReturn(testAppointmentDto);

        // When & Then
        mockMvc.perform(post("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(appointmentService).createAppointment(any(AppointmentCreateRequest.class), eq(1L), eq(User.Role.CLIENT));
    }

    @Test
    @DisplayName("POST /appointments - Debe crear cita exitosamente con rol ADMIN")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldCreateAppointmentSuccessfullyWithAdminRole() throws Exception {
        // Given
        when(appointmentService.createAppointment(any(AppointmentCreateRequest.class), eq(1L), eq(User.Role.ADMIN)))
                .thenReturn(testAppointmentDto);

        // When & Then
        mockMvc.perform(post("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L));

        verify(appointmentService).createAppointment(any(AppointmentCreateRequest.class), eq(1L), eq(User.Role.ADMIN));
    }

    @Test
    @DisplayName("POST /appointments - Debe fallar con HTTP 403 para rol BARBER")
    @WithMockUser(username = "barber", roles = "BARBER")
    void shouldFailCreateAppointmentWithBarberRole() throws Exception {
        // When & Then
        mockMvc.perform(post("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(appointmentService, never()).createAppointment(any(), any(), any());
    }

    @Test
    @DisplayName("POST /appointments - Debe fallar con HTTP 401 para usuarios no autenticados")
    void shouldFailCreateAppointmentWithoutAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(post("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());

        verify(appointmentService, never()).createAppointment(any(), any(), any());
    }

    @Test
    @DisplayName("POST /appointments - Debe manejar conflicto de disponibilidad (Race Condition)")
    @WithMockUser(username = "client", roles = "CLIENT")
    void shouldHandleAvailabilityConflictDuringCreation() throws Exception {
        // Given - Simular que el slot ya no está disponible
        when(appointmentService.createAppointment(any(AppointmentCreateRequest.class), eq(1L), eq(User.Role.CLIENT)))
                .thenThrow(new com.juandidev.barberiaback.exception.AppointmentConflictException(
                        "El horario seleccionado ya no está disponible. Por favor, seleccione otro horario."));

        // When & Then
        mockMvc.perform(post("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("APPOINTMENT_CONFLICT"))
                .andExpect(jsonPath("$.message").value("El horario seleccionado ya no está disponible. Por favor, seleccione otro horario."));

        verify(appointmentService).createAppointment(any(AppointmentCreateRequest.class), eq(1L), eq(User.Role.CLIENT));
    }

    @Test
    @DisplayName("GET /appointments/{id} - CLIENT solo puede ver sus propias citas")
    @WithMockUser(username = "client", roles = "CLIENT")
    void shouldAllowClientToViewOwnAppointment() throws Exception {
        // Given
        when(appointmentService.getAppointmentById(eq(1L), eq(1L), eq(User.Role.CLIENT)))
                .thenReturn(Optional.of(testAppointmentDto));

        // When & Then
        mockMvc.perform(get("/appointments/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L));

        verify(appointmentService).getAppointmentById(eq(1L), eq(1L), eq(User.Role.CLIENT));
    }

    @Test
    @DisplayName("GET /appointments/{id} - Debe fallar cuando CLIENT intenta ver cita de otro")
    @WithMockUser(username = "client", roles = "CLIENT")
    void shouldFailWhenClientTriesToViewOthersAppointment() throws Exception {
        // Given - Simular excepción de autorización
        when(appointmentService.getAppointmentById(eq(1L), eq(1L), eq(User.Role.CLIENT)))
                .thenThrow(new com.juandidev.barberiaback.exception.UnauthorizedAppointmentAccessException(1L, 1L, "ver"));

        // When & Then
        mockMvc.perform(get("/appointments/1"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UNAUTHORIZED_APPOINTMENT_ACCESS"));

        verify(appointmentService).getAppointmentById(eq(1L), eq(1L), eq(User.Role.CLIENT));
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/cancel - CLIENT solo puede cancelar sus propias citas")
    @WithMockUser(username = "client", roles = "CLIENT")
    void shouldAllowClientToCancelOwnAppointment() throws Exception {
        // Given
        when(appointmentService.cancelAppointment(eq(1L), eq(1L), eq(User.Role.CLIENT)))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(patch("/appointments/1/cancel"))
                .andExpect(status().isNoContent());

        verify(appointmentService).cancelAppointment(eq(1L), eq(1L), eq(User.Role.CLIENT));
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/cancel - Debe fallar cuando CLIENT intenta cancelar cita de otro")
    @WithMockUser(username = "client", roles = "CLIENT")
    void shouldFailWhenClientTriesToCancelOthersAppointment() throws Exception {
        // Given - Simular excepción de autorización
        when(appointmentService.cancelAppointment(eq(1L), eq(1L), eq(User.Role.CLIENT)))
                .thenThrow(new com.juandidev.barberiaback.exception.UnauthorizedAppointmentAccessException(1L, 1L, "cancelar"));

        // When & Then
        mockMvc.perform(patch("/appointments/1/cancel"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UNAUTHORIZED_APPOINTMENT_ACCESS"));

        verify(appointmentService).cancelAppointment(eq(1L), eq(1L), eq(User.Role.CLIENT));
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/confirm - Debe ser accesible solo para ADMIN y BARBER")
    @WithMockUser(username = "barber", roles = "BARBER")
    void shouldAllowBarberToConfirmAppointment() throws Exception {
        // Given
        when(appointmentService.confirmAppointment(eq(1L), eq(2L), eq(User.Role.BARBER)))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(patch("/appointments/1/confirm"))
                .andExpect(status().isNoContent());

        verify(appointmentService).confirmAppointment(eq(1L), eq(2L), eq(User.Role.BARBER));
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/confirm - Debe fallar con HTTP 403 para CLIENT")
    @WithMockUser(username = "client", roles = "CLIENT")
    void shouldFailConfirmAppointmentWithClientRole() throws Exception {
        // When & Then
        mockMvc.perform(patch("/appointments/1/confirm"))
                .andExpect(status().isForbidden());

        verify(appointmentService, never()).confirmAppointment(any(), any(), any());
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/complete - Debe ser accesible solo para ADMIN y BARBER")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldAllowAdminToCompleteAppointment() throws Exception {
        // Given
        when(appointmentService.completeAppointment(eq(1L), eq(1L), eq(User.Role.ADMIN)))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(patch("/appointments/1/complete"))
                .andExpect(status().isNoContent());

        verify(appointmentService).completeAppointment(eq(1L), eq(1L), eq(User.Role.ADMIN));
    }

    @Test
    @DisplayName("GET /appointments/my-appointments - Debe retornar citas del usuario actual")
    @WithMockUser(username = "client", roles = "CLIENT")
    void shouldReturnCurrentUserAppointments() throws Exception {
        // Given
        List<AppointmentDto> appointments = Arrays.asList(testAppointmentDto);
        when(appointmentService.getAppointmentsForCurrentUser(eq(1L), eq(User.Role.CLIENT), isNull()))
                .thenReturn(appointments);

        // When & Then
        mockMvc.perform(get("/appointments/my-appointments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(appointmentService).getAppointmentsForCurrentUser(eq(1L), eq(User.Role.CLIENT), isNull());
    }

    @Test
    @DisplayName("GET /appointments/my-appointments - Debe filtrar por estado cuando se proporciona")
    @WithMockUser(username = "barber", roles = "BARBER")
    void shouldFilterAppointmentsByStatusWhenProvided() throws Exception {
        // Given
        List<AppointmentDto> appointments = Arrays.asList(testAppointmentDto);
        when(appointmentService.getAppointmentsForCurrentUser(eq(2L), eq(User.Role.BARBER), eq(AppointmentStatus.PENDING)))
                .thenReturn(appointments);

        // When & Then
        mockMvc.perform(get("/appointments/my-appointments")
                .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(appointmentService).getAppointmentsForCurrentUser(eq(2L), eq(User.Role.BARBER), eq(AppointmentStatus.PENDING));
    }

    @Test
    @DisplayName("Debe validar campos requeridos en creación de cita")
    @WithMockUser(username = "client", roles = "CLIENT")
    void shouldValidateRequiredFieldsWhenCreatingAppointment() throws Exception {
        // Given - Request con campos faltantes
        AppointmentCreateRequest invalidRequest = new AppointmentCreateRequest(
                null, // clientId faltante
                null, // barberId faltante
                null, // serviceId faltante
                null, // startTime faltante
                "Notas válidas"
        );

        // When & Then
        mockMvc.perform(post("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).createAppointment(any(), any(), any());
    }
}
