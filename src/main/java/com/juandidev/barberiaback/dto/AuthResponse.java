package com.juandidev.barberiaback.dto;

import com.juandidev.barberiaback.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    @Builder.Default
    private String type = "Bearer";
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private User.Role role;
    private String message;

    public AuthResponse(String token, String username, String email, String firstName, String lastName, User.Role role) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }
}
