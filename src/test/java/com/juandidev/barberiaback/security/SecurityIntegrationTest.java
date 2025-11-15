package com.juandidev.barberiaback.security;

import com.juandidev.barberiaback.dto.BarberCreateRequest;
import com.juandidev.barberiaback.dto.ServiceCreateRequest;
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

import java.time.LocalTime;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Security Integration Tests - Pruebas de Seguridad")
class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private ServiceCreateRequest serviceCreateRequest;
    private BarberCreateRequest barberCreateRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        serviceCreateRequest = new ServiceCreateRequest();
        serviceCreateRequest.setName("Corte de Cabello Test");
        serviceCreateRequest.setDuration(30);
        serviceCreateRequest.setPrice(25.00);
        serviceCreateRequest.setDescription("Servicio de prueba");

        barberCreateRequest = new BarberCreateRequest();
        barberCreateRequest.setUserId(1L);
        barberCreateRequest.setSpecialties("Corte, Barba");
        barberCreateRequest.setExperienceYears(5);
        barberCreateRequest.setPhoneNumber("+1234567890");
        barberCreateRequest.setStartTime(LocalTime.of(9, 0));
        barberCreateRequest.setEndTime(LocalTime.of(18, 0));
    }

    /**
     * Helper method para verificar que el status NO sea de autorización (401 o 403)
     */
    private void expectNotAuthorizationError(org.springframework.test.web.servlet.ResultActions result) throws Exception {
        result.andExpect(result2 -> {
            int status = result2.getResponse().getStatus();
            if (status == 401 || status == 403) {
                throw new AssertionError("No debe ser error de autorización. Status: " + status);
            }
        });
    }

    // ========== PRUEBAS DE ENDPOINTS PÚBLICOS ==========

    @Test
    @DisplayName("GET /services - Debe ser accesible sin autenticación (HTTP 200)")
    void shouldAccessServicesEndpointWithoutAuth() throws Exception {
        mockMvc.perform(get("/services"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /services/{id} - Debe ser accesible sin autenticación")
    void shouldAccessServiceByIdWithoutAuth() throws Exception {
        mockMvc.perform(get("/services/1"))
                .andExpect(status().isNotFound()); // 404 porque no hay datos, pero no 401/403
    }

    @Test
    @DisplayName("GET /barbers - Debe ser accesible sin autenticación (HTTP 200)")
    void shouldAccessBarbersEndpointWithoutAuth() throws Exception {
        mockMvc.perform(get("/barbers"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /barbers/{id} - Debe ser accesible sin autenticación")
    void shouldAccessBarberByIdWithoutAuth() throws Exception {
        mockMvc.perform(get("/barbers/1"))
                .andExpect(status().isNotFound()); // 404 porque no hay datos, pero no 401/403
    }

    @Test
    @DisplayName("GET /services/price-range - Debe ser accesible sin autenticación")
    void shouldAccessServicesPriceRangeWithoutAuth() throws Exception {
        mockMvc.perform(get("/services/price-range")
                .param("minPrice", "10.0")
                .param("maxPrice", "50.0"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /barbers/specialty/{specialty} - Debe ser accesible sin autenticación")
    void shouldAccessBarbersSpecialtyWithoutAuth() throws Exception {
        mockMvc.perform(get("/barbers/specialty/Corte"))
                .andExpect(status().isOk());
    }

    // ========== PRUEBAS DE SEGURIDAD PARA ENDPOINTS ADMIN ==========

    @Test
    @DisplayName("POST /services - Debe fallar con HTTP 401 para usuarios no autenticados")
    void shouldFailCreateServiceWithoutAuth() throws Exception {
        mockMvc.perform(post("/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceCreateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /services - Debe fallar con HTTP 403 para usuarios CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldFailCreateServiceWithClientRole() throws Exception {
        mockMvc.perform(post("/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceCreateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /services - Debe fallar con HTTP 403 para usuarios BARBER")
    @WithMockUser(roles = "BARBER")
    void shouldFailCreateServiceWithBarberRole() throws Exception {
        mockMvc.perform(post("/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceCreateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /barbers - Debe fallar con HTTP 401 para usuarios no autenticados")
    void shouldFailCreateBarberWithoutAuth() throws Exception {
        mockMvc.perform(post("/barbers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(barberCreateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /barbers - Debe fallar con HTTP 403 para usuarios CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldFailCreateBarberWithClientRole() throws Exception {
        mockMvc.perform(post("/barbers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(barberCreateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /barbers - Debe fallar con HTTP 403 para usuarios BARBER")
    @WithMockUser(roles = "BARBER")
    void shouldFailCreateBarberWithBarberRole() throws Exception {
        mockMvc.perform(post("/barbers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(barberCreateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /services/{id} - Debe fallar con HTTP 403 para usuarios CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldFailUpdateServiceWithClientRole() throws Exception {
        mockMvc.perform(put("/services/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceCreateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /services/{id} - Debe fallar con HTTP 403 para usuarios BARBER")
    @WithMockUser(roles = "BARBER")
    void shouldFailDeleteServiceWithBarberRole() throws Exception {
        mockMvc.perform(delete("/services/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /barbers/{id} - Debe fallar con HTTP 403 para usuarios CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldFailDeleteBarberWithClientRole() throws Exception {
        mockMvc.perform(delete("/barbers/1"))
                .andExpect(status().isForbidden());
    }

    // ========== PRUEBAS DE ACCESO ADMIN ==========

    @Test
    @DisplayName("GET /barbers/all - Debe fallar con HTTP 403 para usuarios CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldFailGetAllBarbersWithClientRole() throws Exception {
        mockMvc.perform(get("/barbers/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /barbers/all - Debe fallar con HTTP 403 para usuarios BARBER")
    @WithMockUser(roles = "BARBER")
    void shouldFailGetAllBarbersWithBarberRole() throws Exception {
        mockMvc.perform(get("/barbers/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /barbers/exists/user/{userId} - Debe fallar con HTTP 403 para usuarios CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldFailCheckBarberExistenceWithClientRole() throws Exception {
        mockMvc.perform(get("/barbers/exists/user/1"))
                .andExpect(status().isForbidden());
    }

    // ========== PRUEBAS DE ACCESO BARBER/ADMIN ==========

    @Test
    @DisplayName("PATCH /barbers/{id}/availability - Debe fallar con HTTP 403 para usuarios CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldFailUpdateAvailabilityWithClientRole() throws Exception {
        mockMvc.perform(patch("/barbers/1/availability")
                .param("available", "false"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /barbers/user/{userId} - Debe fallar con HTTP 403 para usuarios CLIENT")
    @WithMockUser(roles = "CLIENT")
    void shouldFailGetBarberByUserIdWithClientRole() throws Exception {
        mockMvc.perform(get("/barbers/user/1"))
                .andExpect(status().isForbidden());
    }

    // ========== PRUEBAS DE ENDPOINTS DE AUTENTICACIÓN ==========

    @Test
    @DisplayName("POST /api/auth/signup - Debe ser accesible sin autenticación")
    void shouldAccessSignupWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest()); // 400 por datos inválidos, no 401/403
    }

    @Test
    @DisplayName("POST /api/auth/signin - Debe ser accesible sin autenticación")
    void shouldAccessSigninWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest()); // 400 por datos inválidos, no 401/403
    }

    @Test
    @DisplayName("GET /api/health - Debe ser accesible sin autenticación")
    void shouldAccessHealthEndpointWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    // ========== PRUEBAS DE CORS Y HEADERS ==========

    @Test
    @DisplayName("OPTIONS requests - Debe manejar preflight CORS correctamente")
    void shouldHandlePreflightCorsRequests() throws Exception {
        mockMvc.perform(options("/services")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }

    // ========== PRUEBAS DE VALIDACIÓN DE ROLES ESPECÍFICOS ==========

    @Test
    @DisplayName("Endpoints ADMIN - Deben ser accesibles solo con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminEndpointsWithAdminRole() throws Exception {
        // Estos deberían pasar la validación de seguridad (aunque fallen por otros motivos)
        mockMvc.perform(post("/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceCreateRequest)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("No debe ser error de autorización. Status: " + status);
                    }
                }); // No debe ser error de autorización

        mockMvc.perform(get("/barbers/all"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/barbers/exists/user/1"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("No debe ser error de autorización. Status: " + status);
                    }
                }); // No debe ser error de autorización
    }

    @Test
    @DisplayName("Endpoints BARBER/ADMIN - Deben ser accesibles con rol BARBER")
    @WithMockUser(roles = "BARBER")
    void shouldAllowBarberEndpointsWithBarberRole() throws Exception {
        mockMvc.perform(patch("/barbers/1/availability")
                .param("available", "true"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("No debe ser error de autorización. Status: " + status);
                    }
                }); // No debe ser error de autorización

        mockMvc.perform(get("/barbers/user/1"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("No debe ser error de autorización. Status: " + status);
                    }
                }); // No debe ser error de autorización
    }
}
