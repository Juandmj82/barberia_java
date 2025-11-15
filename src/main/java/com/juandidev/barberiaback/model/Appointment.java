package com.juandidev.barberiaback.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "El cliente es obligatorio")
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    @NotNull(message = "El barbero es obligatorio")
    private Barber barber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    @NotNull(message = "El servicio es obligatorio")
    private Service service;

    @Column(name = "start_time", nullable = false)
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    @NotNull(message = "La hora de fin es obligatoria")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(name = "notes")
    private String notes;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // Calcular precio total basado en el servicio
        if (service != null && totalPrice == null) {
            totalPrice = service.getPrice();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniencia
    public String getClientFullName() {
        return client != null ? client.getFirstName() + " " + client.getLastName() : "";
    }

    public String getBarberFullName() {
        return barber != null ? barber.getFullName() : "";
    }

    public String getServiceName() {
        return service != null ? service.getName() : "";
    }

    public boolean isPending() {
        return AppointmentStatus.PENDING.equals(status);
    }

    public boolean isConfirmed() {
        return AppointmentStatus.CONFIRMED.equals(status);
    }

    public boolean isCancelled() {
        return AppointmentStatus.CANCELLED.equals(status);
    }

    public boolean isCompleted() {
        return AppointmentStatus.COMPLETED.equals(status);
    }
}
