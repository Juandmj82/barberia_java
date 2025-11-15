package com.juandidev.barberiaback.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "barbers")
public class Barber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "specialties")
    private String specialties;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "start_time")
    private LocalTime startTime; // Hora de inicio de trabajo

    @Column(name = "end_time")
    private LocalTime endTime; // Hora de fin de trabajo

    @Column(name = "is_available")
    @Builder.Default
    private Boolean available = true;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relación bidireccional con Appointment
    @OneToMany(mappedBy = "barber", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniencia
    public String getFullName() {
        return user != null ? user.getFirstName() + " " + user.getLastName() : "";
    }

    public String getEmail() {
        return user != null ? user.getEmail() : "";
    }

    public String getUsername() {
        return user != null ? user.getUsername() : "";
    }
}
