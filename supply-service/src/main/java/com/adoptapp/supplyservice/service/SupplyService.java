package com.adoptapp.supplyservice.service;

import com.adoptapp.supplyservice.client.NotificationServiceClient;
import com.adoptapp.supplyservice.client.ShelterServiceClient;
import com.adoptapp.supplyservice.client.StaffServiceClient;
import com.adoptapp.supplyservice.client.UserServiceClient;
import com.adoptapp.sharedkernel.dto.UserAuthResponse;
import com.adoptapp.supplyservice.dto.*;
import com.adoptapp.supplyservice.model.Supply;
import com.adoptapp.supplyservice.model.SupplyCategory;
import com.adoptapp.supplyservice.model.SupplyHistory;
import com.adoptapp.supplyservice.model.SupplyStatus;
import com.adoptapp.supplyservice.repository.SupplyHistoryRepository;
import com.adoptapp.supplyservice.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplyService {

    private final SupplyRepository supplyRepository;
    private final SupplyHistoryRepository supplyHistoryRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final ShelterServiceClient shelterServiceClient;
    private final StaffServiceClient staffServiceClient;

    public List<SupplyResult> getSupplies() {
        return supplyRepository.findByStatusNot(SupplyStatus.INACTIVE).stream()
                .map(this::toResult)
                .toList();
    }

    public List<SupplyResult> getSupplies(String status) {
        if (status == null || status.isBlank()) {
            return getSupplies();
        }
        try {
            SupplyStatus supplyStatus = SupplyStatus.valueOf(status.toUpperCase());
            return supplyRepository.findByStatus(supplyStatus).stream()
                    .map(this::toResult)
                    .toList();
        } catch (IllegalArgumentException e) {
            log.warn("Estado inválido para supply: '{}'", status);
            throw new IllegalArgumentException("Status invalido: " + status);
        }
    }

    public Optional<SupplyResult> getById(Long id) {
        return supplyRepository.findById(id)
                .filter(supply -> supply.getStatus() != SupplyStatus.INACTIVE)
                .map(this::toResult);
    }

    public Optional<SupplyResult> getByIdForShelter(Long id, Long shelterId) {
        return supplyRepository.findById(id)
                .filter(supply -> supply.getStatus() != SupplyStatus.INACTIVE)
                .filter(supply -> shelterId.equals(supply.getShelterId()))
                .map(this::toResult);
    }

    public List<SupplyResult> findByShelterId(Long shelterId) {
        return supplyRepository.findByShelterIdAndStatusNot(shelterId, SupplyStatus.INACTIVE).stream()
                .map(this::toResult)
                .toList();
    }

    public List<SupplyResult> findByShelterId(Long shelterId, String status) {
        if (status == null || status.isBlank()) {
            return findByShelterId(shelterId);
        }

        try {
            SupplyStatus supplyStatus = SupplyStatus.valueOf(status.toUpperCase());
            return supplyRepository.findByShelterIdAndStatus(shelterId, supplyStatus).stream()
                    .map(this::toResult)
                    .toList();
        } catch (IllegalArgumentException e) {
            log.warn("Estado invalido para supply: '{}'", status);
            throw new IllegalArgumentException("Status invalido: " + status);
        }
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

    @Transactional
    public SupplyResult create(SupplyCommand command) {
        log.info("Creando supply: name={}, shelterId={}, userId={}", command.name(), command.shelterId(), command.userId());

        validateShelter(command.shelterId());
        validateUser(command.userId());

        Supply supply = new Supply();
        applyCommandToEntity(supply, command);

        Supply saved = supplyRepository.save(supply);
        log.info("Supply creado exitosamente: id={}, name={}", saved.getId(), saved.getName());

        recordHistory(saved.getId(), "CREATED",
                "Supply creado: name=" + command.name() + ", quantity=" + command.quantity(),
                command.userId(),
                null, saved.getStatus().name(),
                null, saved.getQuantity(),
                null, saved.getCategory().name());

        sendNotification(command.userId(), "SUPPLY_CREATED",
                "Nuevo supply registrado: " + saved.getName());
        sendShelterNotification(supply.getShelterId(), "SUPPLY_CREATED",
                "Supply creado: " + supply.getName());

        return toResult(saved);
    }

    @Transactional
    public Optional<SupplyResult> update(Long id, SupplyCommand command) {
        log.info("Actualizando supply: id={}", id);

        Optional<Supply> found = supplyRepository.findById(id);
        if (found.isEmpty()) {
            log.warn("Supply no encontrado: id={}", id);
            return Optional.empty();
        }

        validateShelter(command.shelterId());
        validateUser(command.userId());

        Supply toUpdate = found.get();
        if (toUpdate.getStatus() == SupplyStatus.INACTIVE) {
            throw new IllegalArgumentException("No se puede actualizar un insumo inactivo");
        }

        String prevStatus = toUpdate.getStatus().name();
        Integer prevQuantity = toUpdate.getQuantity();
        String prevCategory = toUpdate.getCategory().name();

        applyCommandToEntity(toUpdate, command);

        Supply saved = supplyRepository.save(toUpdate);
        log.info("Supply actualizado exitosamente: id={}, name={}", saved.getId(), saved.getName());

        recordHistory(saved.getId(), "UPDATED",
                "Supply actualizado: name=" + command.name(),
                command.userId(),
                prevStatus, saved.getStatus().name(),
                prevQuantity, saved.getQuantity(),
                prevCategory, saved.getCategory().name());

        sendNotification(command.userId(), "SUPPLY_UPDATED",
                "Supply actualizado: " + saved.getName());
        sendShelterNotification(saved.getShelterId(), "SUPPLY_UPDATED",
                "Supply actualizado: " + saved.getName());

        return Optional.of(toResult(saved));
    }

    @Transactional
    public boolean delete(Long id) {
        log.info("Eliminando supply: id={}", id);

        Optional<Supply> found = supplyRepository.findById(id);
        if (found.isEmpty()) {
            log.warn("Supply a eliminar no encontrado: id={}", id);
            return false;
        }

        Supply supply = found.get();
        if (supply.getStatus() == SupplyStatus.INACTIVE) {
            log.warn("Supply ya inactivo: id={}", id);
            return false;
        }

        String prevStatus = supply.getStatus() != null ? supply.getStatus().name() : null;
        Integer prevQuantity = supply.getQuantity();
        String prevCategory = supply.getCategory() != null ? supply.getCategory().name() : null;

        supply.setStatus(SupplyStatus.INACTIVE);
        Supply saved = supplyRepository.save(supply);

        recordHistory(saved.getId(), "INACTIVE",
                "Supply marcado como inactivo: name=" + saved.getName(),
                null,
                prevStatus, saved.getStatus().name(),
                prevQuantity, saved.getQuantity(),
                prevCategory, saved.getCategory().name());

        log.info("Supply marcado como inactivo exitosamente: id={}, name={}", id, supply.getName());

        sendShelterNotification(supply.getShelterId(), "SUPPLY_DELETED",
                "Supply eliminado: " + supply.getName());

        return true;
    }

    public Optional<List<SupplyHistoryResponse>> getHistory(Long supplyId) {
        if (!supplyRepository.existsById(supplyId)) {
            return Optional.empty();
        }
        List<SupplyHistoryResponse> history = supplyHistoryRepository.findBySupplyIdOrderByCreatedAtDesc(supplyId).stream()
                .map(h -> new SupplyHistoryResponse(
                        h.getId(),
                        h.getSupply().getId(),
                        h.getAction(),
                        h.getComment(),
                        h.getPrevStatus(),
                        h.getNewStatus(),
                        h.getPrevQuantity(),
                        h.getNewQuantity(),
                        h.getPrevCategory(),
                        h.getNewCategory(),
                        h.getChangedByUserId(),
                        h.getCreatedAt()
                ))
                .toList();
        return Optional.of(history);
    }

    private void applyCommandToEntity(Supply supply, SupplyCommand command) {
        if (command.status() == SupplyStatus.INACTIVE && supply.getStatus() != SupplyStatus.INACTIVE) {
            throw new IllegalArgumentException("No se puede marcar un insumo como inactivo desde update; use delete");
        }

        supply.setName(command.name());
        supply.setDescription(command.description());
        supply.setQuantity(command.quantity());
        supply.setUnit(command.unit());
        supply.setCategory(parseSupplyCategory(command.category()));
        supply.setShelterId(command.shelterId());
        supply.setSupplierName(command.supplierName());
        supply.setMinimumStock(command.minimumStock() != null ? command.minimumStock() : 5);
        supply.setStatus(command.status());
    }

    private void recordHistory(Long supplyId, String action, String comment, Long changedByUserId,
                                String prevStatus, String newStatus,
                                Integer prevQuantity, Integer newQuantity,
                                String prevCategory, String newCategory) {
        Supply supply = supplyRepository.findById(supplyId).orElse(null);
        if (supply == null) return;

        SupplyHistory history = SupplyHistory.builder()
                .supply(supply)
                .action(action)
                .comment(comment)
                .changedByUserId(changedByUserId)
                .prevStatus(prevStatus)
                .newStatus(newStatus)
                .prevQuantity(prevQuantity)
                .newQuantity(newQuantity)
                .prevCategory(prevCategory)
                .newCategory(newCategory)
                .build();
        supplyHistoryRepository.save(history);
    }

    private void validateShelter(Long shelterId) {
        ResponseEntity<ShelterResponse> shelterResponse = shelterServiceClient.getShelterById(shelterId);
        if (shelterResponse == null || !shelterResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Refugio no encontrado: shelterId={}", shelterId);
            throw new IllegalArgumentException("El refugio con ID " + shelterId + " no existe");
        }
    }

    private void validateUser(Long userId) {
        ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(userId);
        if (userResponse == null || !userResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Usuario no encontrado: userId={}", userId);
            throw new IllegalArgumentException("El usuario con ID " + userId + " no existe");
        }
    }

    private void sendNotification(Long userId, String type, String message) {
        try {
            String email = "sistema@adoptapp.com";
            ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(userId);
            if (userResponse != null && userResponse.getStatusCode().is2xxSuccessful()
                    && userResponse.getBody() != null && userResponse.getBody().email() != null) {
                email = userResponse.getBody().email();
            }
            NotificationRequest notif = new NotificationRequest(userId, null, email, message, type, "SENT");
            ResponseEntity<Void> response = notificationServiceClient.sendNotification(notif);
            if (response != null && !response.getStatusCode().is2xxSuccessful()) {
                log.warn("Notificación enviada con error: status={}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación: {}", e.getMessage());
        }
    }

    private void sendShelterNotification(Long shelterId, String type, String message) {
        try {
            String email = "sistema@adoptapp.com";

            ResponseEntity<ShelterResponse> shelterResponse = shelterServiceClient.getShelterById(shelterId);

            if (shelterResponse != null
                    && shelterResponse.getStatusCode().is2xxSuccessful()
                    && shelterResponse.getBody() != null
                    && shelterResponse.getBody().email() != null) {
                email = shelterResponse.getBody().email();
            }

            NotificationRequest notif = new NotificationRequest(
                    null,
                    shelterId,
                    email,
                    message,
                    type,
                    "SENT"
            );

            notificationServiceClient.sendNotification(notif);
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación al refugio {}: {}", shelterId, e.getMessage());
        }
    }

    private SupplyCategory parseSupplyCategory(String category) {
        try {
            return SupplyCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Categoría inválida para supply: '{}'", category);
            throw new IllegalArgumentException("Categoría de supply inválida: " + category);
        }
    }

    private SupplyResult toResult(Supply supply) {
        return new SupplyResult(
                supply.getId(),
                supply.getName(),
                supply.getDescription(),
                supply.getQuantity(),
                supply.getUnit(),
                supply.getCategory().name(),
                supply.getShelterId(),
                supply.getSupplierName(),
                supply.getMinimumStock(),
                supply.getStatus().name(),
                supply.getCreatedAt(),
                supply.getUpdatedAt()
        );
    }
}
