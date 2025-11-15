package com.juandidev.barberiaback.repository;

import com.juandidev.barberiaback.model.Appointment;
import com.juandidev.barberiaback.model.AppointmentStatus;
import com.juandidev.barberiaback.model.Barber;
import com.juandidev.barberiaback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByClient(User client);

    List<Appointment> findByClientId(Long clientId);

    List<Appointment> findByBarber(Barber barber);

    List<Appointment> findByBarberId(Long barberId);

    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByClientAndStatus(User client, AppointmentStatus status);

    List<Appointment> findByClientIdAndStatus(Long clientId, AppointmentStatus status);

    List<Appointment> findByBarberAndStatus(Barber barber, AppointmentStatus status);

    List<Appointment> findByBarberIdAndStatus(Long barberId, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.barber.id = :barberId " +
           "AND a.startTime BETWEEN :startDate AND :endDate " +
           "ORDER BY a.startTime ASC")
    List<Appointment> findByBarberIdAndDateRange(@Param("barberId") Long barberId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.client.id = :clientId " +
           "AND a.startTime BETWEEN :startDate AND :endDate " +
           "ORDER BY a.startTime ASC")
    List<Appointment> findByClientIdAndDateRange(@Param("clientId") Long clientId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.barber.id = :barberId " +
           "AND a.status IN :statuses " +
           "AND ((a.startTime <= :endTime AND a.endTime >= :startTime))")
    List<Appointment> findConflictingAppointments(@Param("barberId") Long barberId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime,
                                                  @Param("statuses") List<AppointmentStatus> statuses);

    @Query("SELECT a FROM Appointment a WHERE a.startTime BETWEEN :startDate AND :endDate " +
           "ORDER BY a.startTime ASC")
    List<Appointment> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.status = :status " +
           "AND a.startTime BETWEEN :startDate AND :endDate " +
           "ORDER BY a.startTime ASC")
    List<Appointment> findByStatusAndDateRange(@Param("status") AppointmentStatus status,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.barber.id = :barberId " +
           "AND a.status = 'COMPLETED' " +
           "AND a.startTime BETWEEN :startDate AND :endDate")
    Long countCompletedAppointmentsByBarberAndDateRange(@Param("barberId") Long barberId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
}
