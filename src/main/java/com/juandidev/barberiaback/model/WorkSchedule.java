package com.juandidev.barberiaback.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "work_schedules", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"barber_id", "day_of_week"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    @NotNull(message = "El barbero es obligatorio")
    private User barber;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    @NotNull(message = "El día de la semana es obligatorio")
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Métodos de conveniencia
    public boolean isWorkingDay() {
        return active && startTime != null && endTime != null;
    }

    public boolean isTimeWithinSchedule(LocalTime time) {
        if (!isWorkingDay()) {
            return false;
        }
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    public long getWorkingHours() {
        if (!isWorkingDay()) {
            return 0;
        }
        return java.time.Duration.between(startTime, endTime).toHours();
    }

    public long getWorkingMinutes() {
        if (!isWorkingDay()) {
            return 0;
        }
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }

    // Validación personalizada para asegurar que start_time < end_time
    @PrePersist
    @PreUpdate
    private void validateSchedule() {
        if (startTime != null && endTime != null) {
            if (!startTime.isBefore(endTime)) {
                throw new IllegalArgumentException(
                    "La hora de inicio debe ser anterior a la hora de fin");
            }
            
            // Validar que el horario esté dentro de 24 horas (no cruce medianoche)
            if (startTime.isAfter(LocalTime.of(23, 59)) || endTime.isBefore(LocalTime.of(0, 1))) {
                throw new IllegalArgumentException(
                    "El horario debe estar dentro del mismo día (00:01 - 23:59)");
            }
        }
    }
}
