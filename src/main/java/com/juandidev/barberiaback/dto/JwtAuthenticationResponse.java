package com.juandidev.barberiaback.dto;

import com.juandidev.barberiaback.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de autenticación JWT con información del usuario y token")
public class JwtAuthenticationResponse {

    @Schema(description = "Token JWT para autenticación", 
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Builder.Default
    @Schema(description = "Tipo de token", example = "Bearer", defaultValue = "Bearer")
    private String type = "Bearer";
    
    @Schema(description = "Nombre de usuario único", example = "juan_cliente")
    private String username;
    
    @Schema(description = "Dirección de correo electrónico", example = "juan@email.com")
    private String email;
    
    @Schema(description = "Nombre del usuario", example = "Juan")
    private String firstName;
    
    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String lastName;
    
    @Schema(description = "Rol del usuario en el sistema", example = "CLIENT")
    private User.Role role;
    
    @Schema(description = "Mensaje descriptivo de la operación", 
            example = "Cliente registrado exitosamente")
    private String message;
    
    @Schema(description = "Tiempo de expiración del token en milisegundos", 
            example = "86400000")
    private Long expiresIn;

    public JwtAuthenticationResponse(String token, String username, String email, 
                                   String firstName, String lastName, User.Role role, Long expiresIn) {
        this.token = token;
        this.type = "Bearer";
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.expiresIn = expiresIn;
    }
}
