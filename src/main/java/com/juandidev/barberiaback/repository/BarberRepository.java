package com.juandidev.barberiaback.repository;

import com.juandidev.barberiaback.model.Barber;
import com.juandidev.barberiaback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BarberRepository extends JpaRepository<Barber, Long> {

    Optional<Barber> findByUser(User user);

    Optional<Barber> findByUserId(Long userId);

    List<Barber> findByActiveTrue();

    List<Barber> findByActiveTrueAndAvailableTrue();

    @Query("SELECT b FROM Barber b WHERE b.active = true AND b.available = true " +
           "AND b.startTime <= :time AND b.endTime >= :time")
    List<Barber> findAvailableBarbersAtTime(@Param("time") LocalTime time);

    @Query("SELECT b FROM Barber b WHERE b.active = true AND " +
           "LOWER(b.specialties) LIKE LOWER(CONCAT('%', :specialty, '%'))")
    List<Barber> findBySpecialtyContainingIgnoreCase(@Param("specialty") String specialty);

    @Query("SELECT b FROM Barber b WHERE b.active = true AND b.experienceYears >= :minYears")
    List<Barber> findByMinimumExperience(@Param("minYears") Integer minYears);

    boolean existsByUser(User user);

    boolean existsByUserId(Long userId);
}
