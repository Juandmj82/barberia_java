package com.juandidev.barberiaback.service;

import com.juandidev.barberiaback.dto.JwtAuthenticationResponse;
import com.juandidev.barberiaback.dto.LoginRequest;
import com.juandidev.barberiaback.dto.UserCreateRequest;
import com.juandidev.barberiaback.exception.UserAlreadyExistsException;
import com.juandidev.barberiaback.model.User;
import com.juandidev.barberiaback.repository.UserRepository;
import com.juandidev.barberiaback.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;


    // Método específico para registro de clientes (signup)
    public JwtAuthenticationResponse signup(UserCreateRequest request) {
        log.info("Iniciando registro de nuevo cliente con username: {}", request.getUsername());
        
        // Verificar que el username no exista
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("username", request.getUsername());
        }
        
        // Verificar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("email", request.getEmail());
        }
        
        // Crear nuevo usuario con rol CLIENT
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(User.Role.CLIENT) // Asignar rol CLIENT automáticamente
                .build();
        
        // Guardar usuario en la base de datos
        User savedUser = userRepository.save(user);
        
        // Generar JWT token
        String token = jwtUtil.generateToken(savedUser);
        Long expirationTime = jwtUtil.getExpirationTime();
        
        log.info("Cliente registrado exitosamente con ID: {} y username: {}", 
                savedUser.getId(), savedUser.getUsername());
        
        return JwtAuthenticationResponse.builder()
                .token(token)
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .role(savedUser.getRole())
                .expiresIn(expirationTime)
                .message("Cliente registrado exitosamente")
                .build();
    }

    // Método específico para inicio de sesión (signin)
    public JwtAuthenticationResponse signin(LoginRequest request) {
        log.info("Iniciando sesión para usuario: {}", request.getUsername());
        
        // Autenticar credenciales usando AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        
        // Obtener usuario autenticado
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Generar JWT token con detalles del usuario y roles
        String token = jwtUtil.generateToken(user);
        Long expirationTime = jwtUtil.getExpirationTime();
        
        log.info("Login exitoso para usuario: {} con rol: {}", user.getUsername(), user.getRole());
        
        return JwtAuthenticationResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .expiresIn(expirationTime)
                .message("Login exitoso")
                .build();
    }
}
