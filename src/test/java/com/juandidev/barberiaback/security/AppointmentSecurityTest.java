package com.juandidev.barberiaback.security;

import com.juandidev.barberiaback.dto.AppointmentCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Appointment Security Tests - Pruebas de Seguridad de Citas")
class AppointmentSecurityTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private AppointmentCreateRequest appointmentCreateRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        appointmentCreateRequest = new AppointmentCreateRequest(
                1L, // clientId
                2L, // barberId
                1L, // serviceId
                LocalDateTime.now().plusDays(1),
                "Cita de prueba"
        );
    }

    // ========== PRUEBAS DE AUTENTICACIÓN ==========

    @Test
    @DisplayName("POST /appointments - Debe fallar con HTTP 401 sin autenticación")
    void shouldFailCreateAppointmentWithoutAuth() throws Exception {
        mockMvc.perform(post("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentCreateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /appointments/{id} - Debe fallar con HTTP 401 sin autenticación")
    void shouldFailGetAppointmentWithoutAuth() throws Exception {
        mockMvc.perform(get("/appointments/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /appointments/my-appointments - Debe fallar con HTTP 401 sin autenticación")
    void shouldFailGetMyAppointmentsWithoutAuth() throws Exception {
        mockMvc.perform(get("/appointments/my-appointments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/cancel - Debe fallar con HTTP 401 sin autenticación")
    void shouldFailCancelAppointmentWithoutAuth() throws Exception {
        mockMvc.perform(patch("/appointments/1/cancel"))
                .andExpect(status().isUnauthorized());
    }

    // ========== PRUEBAS DE AUTORIZACIÓN POR ROLES ==========

    @Test
    @DisplayName("POST /appointments - Debe ser accesible con rol CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldAllowCreateAppointmentWithClientRole() throws Exception {
        // El endpoint debe ser accesible (aunque falle por otros motivos como datos de prueba)
        mockMvc.perform(post("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentCreateRequest)))
                .andExpect(status().is(not(anyOf(equalTo(401), equalTo(403))))); // No debe ser error de autorización
    }

    @Test
    @DisplayName("POST /appointments - Debe ser accesible con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowCreateAppointmentWithAdminRole() throws Exception {
        mockMvc.perform(post("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentCreateRequest)))
                .andExpect(status().is(not(anyOf(equalTo(401), equalTo(403))))); // No debe ser error de autorización
    }

    @Test
    @DisplayName("POST /appointments - Debe fallar con HTTP 403 para rol BARBER")
    @WithMockUser(roles = "BARBER")
    void shouldFailCreateAppointmentWithBarberRole() throws Exception {
        mockMvc.perform(post("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentCreateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/confirm - Debe ser accesible con rol BARBER")
    @WithMockUser(roles = "BARBER")
    void shouldAllowConfirmAppointmentWithBarberRole() throws Exception {
        mockMvc.perform(patch("/appointments/1/confirm"))
                .andExpect(status().is(not(anyOf(equalTo(401), equalTo(403))))); // No debe ser error de autorización
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/confirm - Debe ser accesible con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowConfirmAppointmentWithAdminRole() throws Exception {
        mockMvc.perform(patch("/appointments/1/confirm"))
                .andExpect(status().is(not(anyOf(equalTo(401), equalTo(403))))); // No debe ser error de autorización
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/confirm - Debe fallar con HTTP 403 para rol CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldFailConfirmAppointmentWithClientRole() throws Exception {
        mockMvc.perform(patch("/appointments/1/confirm"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/complete - Debe ser accesible con rol BARBER")
    @WithMockUser(roles = "BARBER")
    void shouldAllowCompleteAppointmentWithBarberRole() throws Exception {
        mockMvc.perform(patch("/appointments/1/complete"))
                .andExpect(status().is(not(anyOf(equalTo(401), equalTo(403))))); // No debe ser error de autorización
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/complete - Debe ser accesible con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowCompleteAppointmentWithAdminRole() throws Exception {
        mockMvc.perform(patch("/appointments/1/complete"))
                .andExpect(status().is(not(anyOf(equalTo(401), equalTo(403))))); // No debe ser error de autorización
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/complete - Debe fallar con HTTP 403 para rol CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldFailCompleteAppointmentWithClientRole() throws Exception {
        mockMvc.perform(patch("/appointments/1/complete"))
                .andExpect(status().isForbidden());
    }

    // ========== PRUEBAS DE ACCESO A CONSULTAS ==========

    @Test
    @DisplayName("GET /appointments/{id} - Debe ser accesible con rol CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldAllowGetAppointmentWithClientRole() throws Exception {
        mockMvc.perform(get("/appointments/1"))
                .andExpect(status().is(not(anyOf(equalTo(401), equalTo(403))))); // No debe ser error de autorización
    }

    @Test
    @DisplayName("GET /appointments/{id} - Debe ser accesible con rol BARBER")
    @WithMockUser(roles = "BARBER")
    void shouldAllowGetAppointmentWithBarberRole() throws Exception {
        mockMvc.perform(get("/appointments/1"))
                .andExpect(status().is(not(anyOf(equalTo(401), equalTo(403))))); // No debe ser error de autorización
    }

    @Test
    @DisplayName("GET /appointments/{id} - Debe ser accesible con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowGetAppointmentWithAdminRole() throws Exception {
        mockMvc.perform(get("/appointments/1"))
                .andExpect(status().is(not(anyOf(equalTo(401), equalTo(403))))); // No debe ser error de autorización
    }

    @Test
    @DisplayName("GET /appointments/my-appointments - Debe ser accesible con rol CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldAllowGetMyAppointmentsWithClientRole() throws Exception {
        mockMvc.perform(get("/appointments/my-appointments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /appointments/my-appointments - Debe ser accesible con rol BARBER")
    @WithMockUser(roles = "BARBER")
    void shouldAllowGetMyAppointmentsWithBarberRole() throws Exception {
        mockMvc.perform(get("/appointments/my-appointments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /appointments/my-appointments - Debe ser accesible con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowGetMyAppointmentsWithAdminRole() throws Exception {
        mockMvc.perform(get("/appointments/my-appointments"))
                .andExpect(status().isOk());
    }

    // ========== PRUEBAS DE CANCELACIÓN ==========

    @Test
    @DisplayName("PATCH /appointments/{id}/cancel - Debe ser accesible con rol CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldAllowCancelAppointmentWithClientRole() throws Exception {
        mockMvc.perform(patch("/appointments/1/cancel"))
                .andExpect(status().is(not(anyOf(equalTo(401), equalTo(403))))); // No debe ser error de autorización
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/cancel - Debe ser accesible con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowCancelAppointmentWithAdminRole() throws Exception {
        mockMvc.perform(patch("/appointments/1/cancel"))
                .andExpect(status().is(not(anyOf(equalTo(401), equalTo(403))))); // No debe ser error de autorización
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/cancel - Debe ser accesible con rol BARBER")
    @WithMockUser(roles = "BARBER")
    void shouldAllowCancelAppointmentWithBarberRole() throws Exception {
        // BARBER puede acceder al endpoint, pero la autorización específica se valida en el servicio
        mockMvc.perform(patch("/appointments/1/cancel"))
                .andExpect(status().is(not(anyOf(equalTo(401), equalTo(403))))); // No debe ser error de autorización
    }

    // ========== PRUEBAS DE ENDPOINTS ADMINISTRATIVOS ==========

    @Test
    @DisplayName("GET /appointments - Debe ser accesible con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowGetAllAppointmentsWithAdminRole() throws Exception {
        mockMvc.perform(get("/appointments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /appointments - Debe ser accesible con rol BARBER")
    @WithMockUser(roles = "BARBER")
    void shouldAllowGetAllAppointmentsWithBarberRole() throws Exception {
        mockMvc.perform(get("/appointments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /appointments - Debe fallar con HTTP 403 para rol CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldFailGetAllAppointmentsWithClientRole() throws Exception {
        mockMvc.perform(get("/appointments"))
                .andExpect(status().isForbidden());
    }

    // ========== PRUEBAS DE FILTROS Y PARÁMETROS ==========

    @Test
    @DisplayName("GET /appointments/my-appointments con filtro de estado - Debe ser accesible")
    @WithMockUser(roles = "CLIENT")
    void shouldAllowFilteredMyAppointments() throws Exception {
        mockMvc.perform(get("/appointments/my-appointments")
                .param("status", "PENDING"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /appointments/barber/{barberId}/conflicts - Debe ser accesible con rol BARBER")
    @WithMockUser(roles = "BARBER")
    void shouldAllowCheckConflictsWithBarberRole() throws Exception {
        mockMvc.perform(get("/appointments/barber/1/conflicts")
                .param("startTime", "2024-01-01T10:00:00")
                .param("endTime", "2024-01-01T11:00:00"))
                .andExpect(status().is(not(anyOf(equalTo(401), equalTo(403))))); // No debe ser error de autorización
    }

    @Test
    @DisplayName("GET /appointments/barber/{barberId}/conflicts - Debe fallar con HTTP 403 para rol CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldFailCheckConflictsWithClientRole() throws Exception {
        mockMvc.perform(get("/appointments/barber/1/conflicts")
                .param("startTime", "2024-01-01T10:00:00")
                .param("endTime", "2024-01-01T11:00:00"))
                .andExpect(status().isForbidden());
    }
}
