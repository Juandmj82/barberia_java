package com.juandidev.barberiaback.controller;

import com.juandidev.barberiaback.dto.JwtAuthenticationResponse;
import com.juandidev.barberiaback.dto.LoginRequest;
import com.juandidev.barberiaback.dto.UserCreateRequest;
import com.juandidev.barberiaback.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para registro e inicio de sesión de usuarios")
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "Registrar nuevo cliente",
        description = "Permite el registro de un nuevo cliente en el sistema. " +
                     "Valida que el username y email sean únicos, encripta la contraseña " +
                     "y asigna automáticamente el rol CLIENT."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Cliente registrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = JwtAuthenticationResponse.class),
                examples = @ExampleObject(
                    name = "Registro exitoso",
                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "type": "Bearer",
                          "username": "juan_cliente",
                          "email": "juan@email.com",
                          "firstName": "Juan",
                          "lastName": "Pérez",
                          "role": "CLIENT",
                          "expiresIn": 86400000,
                          "message": "Cliente registrado exitosamente"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error de validación en los datos de entrada",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error de validación",
                    value = """
                        {
                          "message": "Error de validación",
                          "errors": {
                            "username": "El nombre de usuario debe tener entre 3 y 50 caracteres",
                            "email": "El formato del email no es válido"
                          },
                          "status": "error"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Usuario o email ya existe",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Usuario duplicado",
                    value = """
                        {
                          "message": "El username 'juan_cliente' ya existe",
                          "status": "error",
                          "type": "USER_ALREADY_EXISTS"
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/signup")
    public ResponseEntity<JwtAuthenticationResponse> signup(@Valid @RequestBody UserCreateRequest request) {
        log.info("Solicitud de registro recibida para username: {}", request.getUsername());
        
        JwtAuthenticationResponse response = authService.signup(request);
        
        log.info("Cliente registrado exitosamente: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica un usuario existente y devuelve un token JWT válido. " +
                     "El token debe incluirse en las cabeceras de autorización para " +
                     "acceder a endpoints protegidos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Inicio de sesión exitoso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = JwtAuthenticationResponse.class),
                examples = @ExampleObject(
                    name = "Login exitoso",
                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "type": "Bearer",
                          "username": "juan_cliente",
                          "email": "juan@email.com",
                          "firstName": "Juan",
                          "lastName": "Pérez",
                          "role": "CLIENT",
                          "expiresIn": 86400000,
                          "message": "Login exitoso"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error de validación en credenciales",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Datos inválidos",
                    value = """
                        {
                          "message": "Error de validación",
                          "errors": {
                            "username": "El nombre de usuario es obligatorio",
                            "password": "La contraseña es obligatoria"
                          },
                          "status": "error"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Credenciales incorrectas",
                    value = """
                        {
                          "message": "Credenciales inválidas",
                          "status": "error"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Usuario no existe",
                    value = """
                        {
                          "message": "Usuario no encontrado",
                          "status": "error"
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationResponse> signin(@Valid @RequestBody LoginRequest request) {
        log.info("Solicitud de login recibida para usuario: {}", request.getUsername());
        
        JwtAuthenticationResponse response = authService.signin(request);
        
        log.info("Login exitoso para usuario: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }
}
