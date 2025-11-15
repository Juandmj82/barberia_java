package com.juandidev.barberiaback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos requeridos para registrar un nuevo cliente")
public class UserCreateRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Schema(description = "Nombre de usuario único (3-50 caracteres)", 
            example = "juan_cliente", 
            minLength = 3, 
            maxLength = 50)
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Schema(description = "Dirección de correo electrónico válida", 
            example = "juan@email.com")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    @Schema(description = "Contraseña segura (6-100 caracteres)", 
            example = "miPassword123", 
            minLength = 6, 
            maxLength = 100)
    private String password;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    @Schema(description = "Nombre del cliente (máximo 50 caracteres)", 
            example = "Juan", 
            maxLength = 50)
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50, message = "El apellido no puede exceder 50 caracteres")
    @Schema(description = "Apellido del cliente (máximo 50 caracteres)", 
            example = "Pérez", 
            maxLength = 50)
    private String lastName;

    @Schema(description = "Número de teléfono opcional (no se persiste en BD)", 
            example = "1234567890")
    private String phoneNumber;
}
