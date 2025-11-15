package com.juandidev.barberiaback.controller;

import com.juandidev.barberiaback.dto.ServiceCreateRequest;
import com.juandidev.barberiaback.dto.ServiceDto;
import com.juandidev.barberiaback.dto.ServiceUpdateRequest;
import com.juandidev.barberiaback.service.ServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @GetMapping
    public ResponseEntity<List<ServiceDto>> getAllServices() {
        List<ServiceDto> services = serviceService.getAllActiveServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceDto> getServiceById(@PathVariable Long id) {
        return serviceService.getServiceById(id)
                .map(service -> ResponseEntity.ok(service))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceDto> createService(@Valid @RequestBody ServiceCreateRequest request) {
        ServiceDto createdService = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdService);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceDto> updateService(@PathVariable Long id, 
                                                   @Valid @RequestBody ServiceUpdateRequest request) {
        return serviceService.updateService(id, request)
                .map(service -> ResponseEntity.ok(service))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        boolean deleted = serviceService.deleteService(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<ServiceDto>> getServicesByPriceRange(
            @RequestParam Double minPrice, 
            @RequestParam Double maxPrice) {
        List<ServiceDto> services = serviceService.getServicesByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/max-duration/{duration}")
    public ResponseEntity<List<ServiceDto>> getServicesByMaxDuration(@PathVariable Integer duration) {
        List<ServiceDto> services = serviceService.getServicesByMaxDuration(duration);
        return ResponseEntity.ok(services);
    }
}
