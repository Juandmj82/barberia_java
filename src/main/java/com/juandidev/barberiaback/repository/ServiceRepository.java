package com.juandidev.barberiaback.repository;

import com.juandidev.barberiaback.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByActiveTrue();

    List<Service> findByActiveTrueOrderByNameAsc();

    Optional<Service> findByNameAndActiveTrue(String name);

    @Query("SELECT s FROM Service s WHERE s.active = true AND s.price BETWEEN :minPrice AND :maxPrice")
    List<Service> findActiveServicesByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    @Query("SELECT s FROM Service s WHERE s.active = true AND s.duration <= :maxDuration")
    List<Service> findActiveServicesByMaxDuration(@Param("maxDuration") Integer maxDuration);

    boolean existsByNameAndActiveTrue(String name);
}
