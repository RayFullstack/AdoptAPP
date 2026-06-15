package com.adoptapp.shelterservice.service;

import com.adoptapp.shelterservice.client.NotificationServiceClient;
import com.adoptapp.shelterservice.client.PetServiceClient;
import com.adoptapp.shelterservice.client.StaffServiceClient;
import com.adoptapp.shelterservice.client.SupplyServiceClient;
import com.adoptapp.shelterservice.client.UserServiceClient;
import com.adoptapp.sharedkernel.dto.UserAuthResponse;
import com.adoptapp.shelterservice.dto.*;
import com.adoptapp.shelterservice.model.Shelter;
import com.adoptapp.shelterservice.model.ShelterStatus;
import com.adoptapp.shelterservice.repository.ShelterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class ShelterService {

    private final ShelterRepository repository;
    private final ShelterHistoryService historyService;
    private final UserServiceClient userServiceClient;
    private final StaffServiceClient staffServiceClient;
    private final PetServiceClient petServiceClient;
    private final SupplyServiceClient supplyServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public ShelterService(ShelterRepository repository,
                          ShelterHistoryService historyService,
                          UserServiceClient userServiceClient,
                          StaffServiceClient staffServiceClient,
                          PetServiceClient petServiceClient,
                          SupplyServiceClient supplyServiceClient,
                          NotificationServiceClient notificationServiceClient) {
        this.repository = repository;
        this.historyService = historyService;
        this.userServiceClient = userServiceClient;
        this.staffServiceClient = staffServiceClient;
        this.petServiceClient = petServiceClient;
        this.supplyServiceClient = supplyServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    public List<ShelterResult> getShelters() {
        return repository.findByStatusNot(ShelterStatus.DELETED).stream()
                .map(this::toResult)
                .toList();
    }

    public List<ShelterResult> getShelters(String status) {
        try {
            ShelterStatus shelterStatus = ShelterStatus.valueOf(status.toUpperCase());
            return repository.findByStatus(shelterStatus).stream()
                    .map(this::toResult)
                    .toList();
        } catch (IllegalArgumentException e) {
            log.warn("Estado inválido para refugio: '{}'", status);
            throw new IllegalArgumentException("Status invalido: " + status);
        }
    }

    public Optional<ShelterResult> getById(Long id) {
        return repository.findById(id)
                .filter(shelter -> shelter.getStatus() != ShelterStatus.DELETED)
                .map(this::toResult);
    }

    public Optional<ShelterResult> getByIdActive(Long id) {
        return repository.findById(id)
                .filter(shelter -> shelter.getStatus() != ShelterStatus.DELETED)
                .map(this::toResult);
    }

    public Long getUserIdByEmail(String email) {
        ResponseEntity<UserAuthResponse> response = userServiceClient.getUserAuthByEmail(email);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalArgumentException("Usuario autenticado no encontrado: " + email);
        }
        return response.getBody().id();
    }

    public Long getShelterIdForStaffUser(Long userId) {
        ResponseEntity<StaffResponse> response = staffServiceClient.getStaffByUserId(userId);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalArgumentException("El usuario no tiene staff activo asociado");
        }
        return response.getBody().shelterId();
    }

    public List<ShelterHistoryResponse> getHistory(Long shelterId) {
        return historyService.getHistory(shelterId);
    }

    @Transactional
    public ShelterResult create(ShelterCommand command, Long userId) {
        log.info("Creando refugio: name={}, email={}", command.name(), command.email());
        try {
            ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(userId);
            if (!userResponse.getStatusCode().is2xxSuccessful()) {
                log.warn("Usuario no encontrado: ID={}", userId);
                throw new IllegalArgumentException("El usuario con ID " + userId + " no existe");
            }

            Shelter shelter = new Shelter();
            shelter.setName(command.name());
            shelter.setEmail(command.email());
            shelter.setPhone(command.phone());
            shelter.setDescription(command.description());
            shelter.setStatus(command.status() != null ? command.status() : ShelterStatus.ACTIVE);
            shelter.setActive(true);

            Shelter saved = repository.save(shelter);

            historyService.recordHistory(saved.getId(), "CREATED",
                    "Refugio creado: " + command.name(),
                    userId,
                    null, command.name(),
                    null, command.email(),
                    null, command.phone(),
                    null, command.description(),
                    null, saved.getStatus().name(),
                    null, saved.isActive());

            String email = userResponse.getBody().email();
            sendNotification(userId, email,
                    "El refugio " + command.name() + " ha sido registrado", "SHELTER_CREATED");

            log.info("Refugio creado exitosamente: ID={}", saved.getId());
            return toResult(saved);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al crear refugio: servicio remoto no disponible - {}", e.getMessage());
            throw new RuntimeException("Error al crear refugio: no se pudo completar la validación");
        }
    }

    @Transactional
    public Optional<ShelterResult> updateById(Long id, ShelterCommand command, Long userId) {
        log.info("Actualizando refugio: ID={}", id);
        try {
            Optional<Shelter> found = repository.findById(id);
            if (found.isEmpty()) {
                log.warn("Refugio no encontrado: ID={}", id);
                return Optional.empty();
            }

            ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(userId);
            if (!userResponse.getStatusCode().is2xxSuccessful()) {
                log.warn("Usuario no encontrado: ID={}", userId);
                throw new IllegalArgumentException("El usuario con ID " + userId + " no existe");
            }

            Shelter toUpdate = found.get();
            if (toUpdate.getStatus() == ShelterStatus.DELETED) {
                throw new IllegalArgumentException("No se puede actualizar un refugio eliminado");
            }

            String prevName = toUpdate.getName();
            String prevEmail = toUpdate.getEmail();
            String prevPhone = toUpdate.getPhone();
            String prevDescription = toUpdate.getDescription();
            ShelterStatus prevStatus = toUpdate.getStatus();
            boolean prevActive = toUpdate.isActive();

            toUpdate.setName(command.name());
            toUpdate.setEmail(command.email());
            toUpdate.setPhone(command.phone());
            toUpdate.setDescription(command.description());
            if (command.status() == ShelterStatus.DELETED) {
                throw new IllegalArgumentException("No se puede marcar un refugio como eliminado desde update; use delete");
            }
            if (command.status() != null) {
                toUpdate.setStatus(command.status());
            }
            toUpdate.setUpdatedAt(LocalDateTime.now());

            Shelter updated = repository.save(toUpdate);

            String cambios = "";
            if (!Objects.equals(prevName, updated.getName()))
                cambios += "nombre: " + prevName + "→" + updated.getName() + ", ";
            if (!Objects.equals(prevEmail, updated.getEmail()))
                cambios += "email: " + prevEmail + "→" + updated.getEmail() + ", ";
            if (!Objects.equals(prevPhone, updated.getPhone()))
                cambios += "telefono: " + prevPhone + "→" + updated.getPhone() + ", ";
            if (!Objects.equals(prevStatus, updated.getStatus()))
                cambios += "estado: " + prevStatus + "→" + updated.getStatus() + ", ";

            historyService.recordHistory(id, "UPDATED",
                    "Refugio modificado. Cambios: " + cambios,
                    userId,
                    prevName, updated.getName(),
                    prevEmail, updated.getEmail(),
                    prevPhone, updated.getPhone(),
                    prevDescription, updated.getDescription(),
                    prevStatus != null ? prevStatus.name() : null,
                    updated.getStatus() != null ? updated.getStatus().name() : null,
                    prevActive, updated.isActive());

            String email = userResponse.getBody().email();
            sendNotification(userId, email,
                    "Refugio " + updated.getName() + " actualizado: " + cambios, "SHELTER_UPDATED");

            log.info("Refugio actualizado exitosamente: ID={}", id);
            return Optional.of(toResult(updated));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar refugio: servicio remoto no disponible - {}", e.getMessage());
            throw new RuntimeException("Error al actualizar refugio: no se pudo completar la validación");
        }
    }

    @Transactional
    public boolean deleteById(Long id, Long userId) {
        log.info("Eliminando refugio: ID={}", id);

        Optional<Shelter> found = repository.findById(id);
        if (found.isEmpty()) {
            log.warn("Refugio a eliminar no encontrado: ID={}", id);
            return false;
        }

        Shelter shelter = found.get();
        if (shelter.getStatus() == ShelterStatus.DELETED) {
            log.warn("Refugio ya eliminado: ID={}", id);
            return false;
        }

        validateNoActiveDependencies(id);

        String email = null;
        try {
            ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(userId);
            if (userResponse.getStatusCode().is2xxSuccessful()) {
                email = userResponse.getBody().email();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener email del usuario {} para notificación", userId);
        }

        String delName = shelter.getName();
        String delEmail = shelter.getEmail();
        String delPhone = shelter.getPhone();
        String delDescription = shelter.getDescription();
        String delStatus = shelter.getStatus() != null ? shelter.getStatus().name() : null;
        boolean delActive = shelter.isActive();

        shelter.setActive(false);
        shelter.setStatus(ShelterStatus.DELETED);
        shelter.setUpdatedAt(LocalDateTime.now());

        try {
            repository.save(shelter);

            historyService.recordHistory(id, "DELETED",
                    "Refugio eliminado: " + delName,
                    userId,
                    delName, null,
                    delEmail, null,
                    delPhone, null,
                    delDescription, null,
                    delStatus, ShelterStatus.DELETED.name(),
                    delActive, false);

            if (email != null) {
                sendNotification(userId, email,
                        "El refugio " + delName + " ha sido eliminado", "SHELTER_DELETED");
            }

            log.info("Refugio eliminado exitosamente: ID={}", id);
            return true;
        } catch (Exception e) {
            log.error("Error al eliminar refugio: ID={}", id, e);
            throw e;
        }
    }

    private ShelterResult toResult(Shelter shelter) {
        return new ShelterResult(
                shelter.getId(),
                shelter.getName(),
                shelter.getEmail(),
                shelter.getPhone(),
                shelter.getDescription(),
                shelter.getStatus(),
                shelter.isActive(),
                shelter.getCreatedAt(),
                shelter.getUpdatedAt()
        );
    }

    private void validateNoActiveDependencies(Long shelterId) {
        validateNoActivePets(shelterId);
        validateNoActiveStaff(shelterId);
        validateNoActiveSupplies(shelterId);
    }

    private void validateNoActivePets(Long shelterId) {
        try {
            ResponseEntity<List<PetResponse>> response = petServiceClient.getActivePetsByShelter(shelterId);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("No se pudo validar mascotas activas del refugio " + shelterId);
            }
            if (!response.getBody().isEmpty()) {
                throw new IllegalArgumentException("No se puede eliminar el refugio porque tiene mascotas activas");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validando mascotas activas del refugio {}: {}", shelterId, e.getMessage());
            throw new RuntimeException("No se pudo validar mascotas activas del refugio " + shelterId);
        }
    }

    private void validateNoActiveStaff(Long shelterId) {
        try {
            ResponseEntity<List<StaffResponse>> response = staffServiceClient.getActiveStaffByShelter(shelterId);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("No se pudo validar staff activo del refugio " + shelterId);
            }
            if (!response.getBody().isEmpty()) {
                throw new IllegalArgumentException("No se puede eliminar el refugio porque tiene staff activo");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validando staff activo del refugio {}: {}", shelterId, e.getMessage());
            throw new RuntimeException("No se pudo validar staff activo del refugio " + shelterId);
        }
    }

    private void validateNoActiveSupplies(Long shelterId) {
        try {
            ResponseEntity<List<SupplyResponse>> response = supplyServiceClient.getActiveSuppliesByShelter(shelterId);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("No se pudo validar insumos activos del refugio " + shelterId);
            }
            if (!response.getBody().isEmpty()) {
                throw new IllegalArgumentException("No se puede eliminar el refugio porque tiene insumos activos");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validando insumos activos del refugio {}: {}", shelterId, e.getMessage());
            throw new RuntimeException("No se pudo validar insumos activos del refugio " + shelterId);
        }
    }

    private void sendNotification(Long userId, String recipient, String message, String typeName) {
        try {
            NotificationRequest request = new NotificationRequest(userId, null, recipient, message, typeName, "SENT");
            notificationServiceClient.sendNotification(request);
        } catch (Exception e) {
            log.warn("Error enviando notificacion a {}: {}", recipient, e.getMessage());
        }
    }
}
