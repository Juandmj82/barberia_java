package com.juandidev.barberiaback.service;

import com.juandidev.barberiaback.dto.ServiceCreateRequest;
import com.juandidev.barberiaback.dto.ServiceDto;
import com.juandidev.barberiaback.dto.ServiceUpdateRequest;
import com.juandidev.barberiaback.model.Service;
import com.juandidev.barberiaback.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public List<ServiceDto> getAllActiveServices() {
        log.info("Obteniendo todos los servicios activos");
        
        List<Service> activeServices = serviceRepository.findByActiveTrueOrderByNameAsc();
        
        return activeServices.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<ServiceDto> getServiceById(Long id) {
        log.info("Buscando servicio con ID: {}", id);
        
        return serviceRepository.findById(id)
                .map(this::convertToDto);
    }

    public ServiceDto createService(ServiceCreateRequest request) {
        log.info("Creando nuevo servicio: {}", request.getName());
        
        // Verificar que no exista un servicio activo con el mismo nombre
        if (serviceRepository.existsByNameAndActiveTrue(request.getName())) {
            throw new RuntimeException("Ya existe un servicio activo con el nombre: " + request.getName());
        }
        
        Service service = convertToEntity(request);
        Service savedService = serviceRepository.save(service);
        
        log.info("Servicio creado exitosamente con ID: {}", savedService.getId());
        return convertToDto(savedService);
    }

    public Optional<ServiceDto> updateService(Long id, ServiceUpdateRequest request) {
        log.info("Actualizando servicio con ID: {}", id);
        
        return serviceRepository.findById(id)
                .map(existingService -> {
                    updateServiceFields(existingService, request);
                    Service updatedService = serviceRepository.save(existingService);
                    log.info("Servicio actualizado exitosamente: {}", updatedService.getName());
                    return convertToDto(updatedService);
                });
    }

    public boolean deleteService(Long id) {
        log.info("Eliminando servicio con ID: {}", id);
        
        return serviceRepository.findById(id)
                .map(service -> {
                    service.setActive(false);
                    serviceRepository.save(service);
                    log.info("Servicio marcado como inactivo: {}", service.getName());
                    return true;
                })
                .orElse(false);
    }

    public List<ServiceDto> getServicesByPriceRange(Double minPrice, Double maxPrice) {
        log.info("Buscando servicios en rango de precio: {} - {}", minPrice, maxPrice);
        
        List<Service> services = serviceRepository.findActiveServicesByPriceRange(minPrice, maxPrice);
        
        return services.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ServiceDto> getServicesByMaxDuration(Integer maxDuration) {
        log.info("Buscando servicios con duración máxima: {} minutos", maxDuration);
        
        List<Service> services = serviceRepository.findActiveServicesByMaxDuration(maxDuration);
        
        return services.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public boolean existsByName(String name) {
        log.info("Verificando existencia de servicio con nombre: {}", name);
        return serviceRepository.existsByNameAndActiveTrue(name);
    }

    // Métodos privados para conversión
    private ServiceDto convertToDto(Service service) {
        return ServiceDto.builder()
                .id(service.getId())
                .name(service.getName())
                .duration(service.getDuration())
                .price(service.getPrice())
                .description(service.getDescription())
                .active(service.getActive())
                .createdAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt())
                .build();
    }

    private Service convertToEntity(ServiceCreateRequest request) {
        return Service.builder()
                .name(request.getName())
                .duration(request.getDuration())
                .price(request.getPrice())
                .description(request.getDescription())
                .active(true) // Por defecto activo
                .build();
    }

    private void updateServiceFields(Service service, ServiceUpdateRequest request) {
        if (request.getName() != null) {
            service.setName(request.getName());
        }
        if (request.getDuration() != null) {
            service.setDuration(request.getDuration());
        }
        if (request.getPrice() != null) {
            service.setPrice(request.getPrice());
        }
        if (request.getDescription() != null) {
            service.setDescription(request.getDescription());
        }
        if (request.getActive() != null) {
            service.setActive(request.getActive());
        }
    }
}
