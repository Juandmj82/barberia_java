package com.juandidev.barberiaback.repository;

import com.juandidev.barberiaback.model.DayOfWeek;
import com.juandidev.barberiaback.model.User;
import com.juandidev.barberiaback.model.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {

    /**
     * Buscar todos los horarios de un barbero específico
     */
    List<WorkSchedule> findByBarberIdOrderByDayOfWeek(Long barberId);

    /**
     * Buscar todos los horarios activos de un barbero
     */
    List<WorkSchedule> findByBarberIdAndActiveTrueOrderByDayOfWeek(Long barberId);

    /**
     * Buscar horario específico por barbero y día
     */
    Optional<WorkSchedule> findByBarberIdAndDayOfWeek(Long barberId, DayOfWeek dayOfWeek);

    /**
     * Verificar si existe un horario para barbero y día específicos
     */
    boolean existsByBarberIdAndDayOfWeek(Long barberId, DayOfWeek dayOfWeek);

    /**
     * Buscar horarios por día de la semana
     */
    List<WorkSchedule> findByDayOfWeekAndActiveTrueOrderByStartTime(DayOfWeek dayOfWeek);

    /**
     * Buscar barberos disponibles en un día y hora específicos
     */
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.dayOfWeek = :dayOfWeek " +
           "AND ws.active = true " +
           "AND ws.startTime <= :time " +
           "AND ws.endTime >= :time " +
           "ORDER BY ws.startTime")
    List<WorkSchedule> findAvailableBarbersAtTime(@Param("dayOfWeek") DayOfWeek dayOfWeek, 
                                                  @Param("time") LocalTime time);

    /**
     * Buscar horarios que se superponen con un rango de tiempo específico
     */
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.barber.id = :barberId " +
           "AND ws.dayOfWeek = :dayOfWeek " +
           "AND ws.active = true " +
           "AND ((ws.startTime <= :startTime AND ws.endTime > :startTime) " +
           "OR (ws.startTime < :endTime AND ws.endTime >= :endTime) " +
           "OR (ws.startTime >= :startTime AND ws.endTime <= :endTime))")
    List<WorkSchedule> findOverlappingSchedules(@Param("barberId") Long barberId,
                                               @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                               @Param("startTime") LocalTime startTime,
                                               @Param("endTime") LocalTime endTime);

    /**
     * Buscar todos los horarios de barberos activos
     */
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.barber.enabled = true " +
           "AND ws.active = true " +
           "ORDER BY ws.barber.id, ws.dayOfWeek")
    List<WorkSchedule> findAllActiveBarberSchedules();

    /**
     * Contar horarios activos por barbero
     */
    @Query("SELECT COUNT(ws) FROM WorkSchedule ws WHERE ws.barber.id = :barberId AND ws.active = true")
    Long countActiveSchedulesByBarberId(@Param("barberId") Long barberId);

    /**
     * Buscar horarios por rango de horas
     */
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.active = true " +
           "AND ws.startTime >= :startTime " +
           "AND ws.endTime <= :endTime " +
           "ORDER BY ws.dayOfWeek, ws.startTime")
    List<WorkSchedule> findByTimeRange(@Param("startTime") LocalTime startTime, 
                                      @Param("endTime") LocalTime endTime);

    /**
     * Eliminar todos los horarios de un barbero
     */
    void deleteByBarberId(Long barberId);

    /**
     * Buscar horarios por barbero (entidad User)
     */
    List<WorkSchedule> findByBarberOrderByDayOfWeek(User barber);
}
