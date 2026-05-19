package com.adoptapp.supplyservice.service;

import com.adoptapp.supplyservice.client.NotificationServiceClient;
import com.adoptapp.supplyservice.client.ShelterServiceClient;
import com.adoptapp.supplyservice.client.UserServiceClient;
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

    public List<SupplyResult> getSupplies() {
        return supplyRepository.findAll().stream()
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
            return List.of();
        }
    }

    public Optional<SupplyResult> getById(Long id) {
        return supplyRepository.findById(id)
                .map(this::toResult);
    }

    public List<SupplyResult> findByShelterId(Long shelterId) {
        return supplyRepository.findByShelterId(shelterId).stream()
                .map(this::toResult)
                .toList();
    }

    @Transactional
    public SupplyResult create(SupplyCommand command) {
        log.info("Creando supply: name={}, shelterId={}, userId={}", command.getName(), command.getShelterId(), command.getUserId());

        validateShelter(command.getShelterId());
        validateUser(command.getUserId());

        Supply supply = Supply.builder()
                .name(command.getName())
                .description(command.getDescription())
                .quantity(command.getQuantity())
                .unit(command.getUnit())
                .category(SupplyCategory.valueOf(command.getCategory().toUpperCase()))
                .shelterId(command.getShelterId())
                .supplierName(command.getSupplierName())
                .minimumStock(command.getMinimumStock() != null ? command.getMinimumStock() : 5)
                .status(command.getStatus())
                .build();

        Supply saved = supplyRepository.save(supply);
        log.info("Supply creado exitosamente: id={}, name={}", saved.getId(), saved.getName());

        recordHistory(saved.getId(), "CREATED",
                "Supply creado: name=" + command.getName() + ", quantity=" + command.getQuantity(),
                command.getUserId(),
                null, saved.getStatus().name(),
                null, saved.getQuantity(),
                null, saved.getCategory().name());

        sendNotification(command.getUserId(), "SUPPLY_CREATED",
                "Nuevo supply registrado: " + saved.getName());

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

        validateShelter(command.getShelterId());

        Supply toUpdate = found.get();
        String prevStatus = toUpdate.getStatus().name();
        Integer prevQuantity = toUpdate.getQuantity();
        String prevCategory = toUpdate.getCategory().name();

        toUpdate.setName(command.getName());
        toUpdate.setDescription(command.getDescription());
        toUpdate.setQuantity(command.getQuantity());
        toUpdate.setUnit(command.getUnit());
        toUpdate.setCategory(SupplyCategory.valueOf(command.getCategory().toUpperCase()));
        toUpdate.setShelterId(command.getShelterId());
        toUpdate.setSupplierName(command.getSupplierName());
        toUpdate.setMinimumStock(command.getMinimumStock() != null ? command.getMinimumStock() : 5);
        toUpdate.setStatus(SupplyStatus.valueOf(command.getStatus().toUpperCase()));

        Supply saved = supplyRepository.save(toUpdate);
        log.info("Supply actualizado exitosamente: id={}, name={}", saved.getId(), saved.getName());

        recordHistory(saved.getId(), "UPDATED",
                "Supply actualizado: name=" + command.getName(),
                command.getUserId(),
                prevStatus, saved.getStatus().name(),
                prevQuantity, saved.getQuantity(),
                prevCategory, saved.getCategory().name());

        sendNotification(command.getUserId(), "SUPPLY_UPDATED",
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

        supplyRepository.deleteById(id);
        log.info("Supply eliminado exitosamente: id={}, name={}", id, supply.getName());

        sendNotification(supply.getShelterId(), "SUPPLY_DELETED",
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
                    && userResponse.getBody() != null && userResponse.getBody().getEmail() != null) {
                email = userResponse.getBody().getEmail();
            }
            NotificationRequest notif = new NotificationRequest(userId, email, message, type, "SENT");
            ResponseEntity<Void> response = notificationServiceClient.sendNotification(notif);
            if (response != null && !response.getStatusCode().is2xxSuccessful()) {
                log.warn("Notificación enviada con error: status={}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación: {}", e.getMessage());
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
