package com.adoptapp.staffservice.service;

import com.adoptapp.staffservice.client.NotificationServiceClient;
import com.adoptapp.staffservice.client.ShelterServiceClient;
import com.adoptapp.staffservice.client.UserServiceClient;
import com.adoptapp.staffservice.dto.*;
import com.adoptapp.staffservice.model.Staff;
import com.adoptapp.staffservice.model.StaffHistory;
import com.adoptapp.staffservice.model.StaffPosition;
import com.adoptapp.staffservice.model.StaffStatus;
import com.adoptapp.staffservice.repository.StaffHistoryRepository;
import com.adoptapp.staffservice.repository.StaffRepository;
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
public class StaffService {

    private final StaffRepository repository;
    private final StaffHistoryRepository historyRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final ShelterServiceClient shelterServiceClient;

    public StaffService(StaffRepository repository,
                         StaffHistoryRepository historyRepository,
                         UserServiceClient userServiceClient,
                         NotificationServiceClient notificationServiceClient,
                         ShelterServiceClient shelterServiceClient) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.userServiceClient = userServiceClient;
        this.notificationServiceClient = notificationServiceClient;
        this.shelterServiceClient = shelterServiceClient;
    }

    public List<StaffResult> getAllStaff() {
        return repository.findAll().stream()
                .map(this::toResult)
                .toList();
    }

    public List<StaffResult> getAllStaff(String status) {
        StaffStatus staffStatus = StaffStatus.valueOf(status.toUpperCase());
        return repository.findByStatus(staffStatus).stream()
                .map(this::toResult)
                .toList();
    }

    public Optional<StaffResult> getById(Long id) {
        return repository.findById(id)
                .map(this::toResult);
    }

    public List<StaffHistoryResponse> getHistory(Long staffId) {
        return historyRepository.findByStaffIdOrderByChangedAtDesc(staffId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Transactional
    public StaffResult create(StaffCommand command) {
        log.info("Creando staff: userId={}, shelterId={}, position={}",
                command.userId(), command.shelterId(), command.position());

        ResponseEntity<ShelterResponse> shelterResponse = shelterServiceClient.getShelterById(command.shelterId());
        if (!shelterResponse.getStatusCode().is2xxSuccessful()) {
            log.warn("Refugio no encontrado: ID={}", command.shelterId());
            throw new IllegalArgumentException("El refugio con ID " + command.shelterId() + " no existe");
        }

        ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(command.userId());
        if (!userResponse.getStatusCode().is2xxSuccessful()) {
            log.warn("Usuario no encontrado: ID={}", command.userId());
            throw new IllegalArgumentException("El usuario con ID " + command.userId() + " no existe");
        }

        Staff staff = new Staff();
        staff.setUserId(command.userId());
        staff.setShelterId(command.shelterId());
        staff.setPosition(command.position());
        staff.setPhone(command.phone());
        staff.setEmail(command.email());
        staff.setHireDate(command.hireDate());
        staff.setStatus(command.status() != null ? command.status() : StaffStatus.ACTIVE);

        try {
            Staff saved = repository.save(staff);

            recordHistory(saved.getId(), "CREATED",
                    "Staff creado: userId=" + command.userId() + ", position=" + command.position(),
                    null, saved.getStatus().name(),
                    null, saved.getPhone(),
                    null, saved.getPosition().name());

            String email = userResponse.getBody().email();
            sendNotification(command.userId(), email,
                    "Staff registrado para el usuario " + command.userId(), "STAFF_ADDED");

            log.info("Staff creado exitosamente: ID={}", saved.getId());
            return toResult(saved);
        } catch (Exception e) {
            log.error("Error al crear staff", e);
            throw e;
        }
    }

    @Transactional
    public Optional<StaffResult> updateById(Long id, StaffCommand command) {
        log.info("Actualizando staff: ID={}", id);

        Optional<Staff> found = repository.findById(id);
        if (found.isEmpty()) {
            log.warn("Staff no encontrado: ID={}", id);
            return Optional.empty();
        }

        ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(command.userId());
        if (!userResponse.getStatusCode().is2xxSuccessful()) {
            log.warn("Usuario no encontrado: ID={}", command.userId());
            throw new IllegalArgumentException("El usuario con ID " + command.userId() + " no existe");
        }

        Staff toUpdate = found.get();
        StaffPosition prevPosition = toUpdate.getPosition();
        StaffStatus prevStatus = toUpdate.getStatus();
        String prevPhone = toUpdate.getPhone();

        toUpdate.setUserId(command.userId());
        toUpdate.setShelterId(command.shelterId());
        toUpdate.setPosition(command.position());
        toUpdate.setPhone(command.phone());
        toUpdate.setEmail(command.email());
        toUpdate.setHireDate(command.hireDate());
        if (command.status() != null) {
            toUpdate.setStatus(command.status());
        }

        try {
            Staff updated = repository.save(toUpdate);

            String cambios = "";
            if (!Objects.equals(prevPosition, updated.getPosition()))
                cambios += "cargo: " + prevPosition + "→" + updated.getPosition() + ", ";
            if (!Objects.equals(prevStatus, updated.getStatus()))
                cambios += "estado: " + prevStatus + "→" + updated.getStatus() + ", ";
            if (!Objects.equals(prevPhone, updated.getPhone()))
                cambios += "telefono: " + prevPhone + "→" + updated.getPhone();

            recordHistory(id, "UPDATED",
                    "Staff modificado. Cambios: " + cambios,
                    prevStatus != null ? prevStatus.name() : null,
                    updated.getStatus() != null ? updated.getStatus().name() : null,
                    prevPhone, updated.getPhone(),
                    prevPosition != null ? prevPosition.name() : null,
                    updated.getPosition() != null ? updated.getPosition().name() : null);

            String email = userResponse.getBody().email();
            sendNotification(command.userId(), email,
                    "Staff " + id + " actualizado: " + cambios, "STAFF_UPDATED");

            log.info("Staff actualizado exitosamente: ID={}", id);
            return Optional.of(toResult(updated));
        } catch (Exception e) {
            log.error("Error al actualizar staff: ID={}", id, e);
            throw e;
        }
    }

    @Transactional
    public boolean deleteById(Long id) {
        log.info("Eliminando staff: ID={}", id);

        Optional<Staff> found = repository.findById(id);
        if (found.isEmpty()) {
            log.warn("Staff a eliminar no encontrado: ID={}", id);
            return false;
        }

        Staff staff = found.get();
        String delStatus = staff.getStatus() != null ? staff.getStatus().name() : null;
        String delPhone = staff.getPhone();
        String delPosition = staff.getPosition() != null ? staff.getPosition().name() : null;

        String email = null;
        try {
            ResponseEntity<UserResponse> userResponse = userServiceClient.getUserById(staff.getUserId());
            if (userResponse.getStatusCode().is2xxSuccessful()) {
                email = userResponse.getBody().email();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener email del usuario {} para notificación", staff.getUserId());
        }

        recordHistory(id, "DELETED",
                "Staff eliminado: userId=" + staff.getUserId(),
                delStatus, null,
                delPhone, null,
                delPosition, null);

        if (email != null) {
            sendNotification(staff.getUserId(), email,
                    "Staff " + id + " eliminado: userId=" + staff.getUserId(), "STAFF_REMOVED");
        }

        try {
            repository.deleteById(id);
            log.info("Staff eliminado exitosamente: ID={}", id);
            return true;
        } catch (Exception e) {
            log.error("Error al eliminar staff: ID={}", id, e);
            throw e;
        }
    }

    private void recordHistory(Long staffId, String action, String comment,
                                String prevStatus, String newStatus,
                                String prevPhone, String newPhone,
                                String prevPosition, String newPosition) {
        StaffHistory history = new StaffHistory();
        repository.findById(staffId).ifPresent(history::setStaff);
        history.setAction(action);
        history.setComment(comment);
        history.setPreviousStatus(prevStatus);
        history.setNewStatus(newStatus);
        history.setPreviousPhone(prevPhone);
        history.setNewPhone(newPhone);
        history.setPreviousPosition(prevPosition);
        history.setNewPosition(newPosition);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    private void sendNotification(Long userId, String recipient, String message, String typeName) {
        try {
            NotificationRequest request = new NotificationRequest(userId, recipient, message, typeName, "SENT");
            notificationServiceClient.sendNotification(request);
        } catch (Exception e) {
            log.warn("Error enviando notificacion a {}: {}", recipient, e.getMessage());
        }
    }

    private StaffResult toResult(Staff staff) {
        return new StaffResult(
                staff.getId(),
                staff.getUserId(),
                staff.getShelterId(),
                staff.getPosition(),
                staff.getPhone(),
                staff.getEmail(),
                staff.getHireDate(),
                staff.getStatus(),
                staff.getCreatedAt(),
                staff.getUpdatedAt()
        );
    }

    private StaffHistoryResponse toHistoryResponse(StaffHistory history) {
        return new StaffHistoryResponse(
                history.getStaff().getId(),
                history.getAction(),
                history.getPreviousPosition(),
                history.getNewPosition(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getPreviousPhone(),
                history.getNewPhone(),
                history.getComment(),
                history.getChangedByUserId(),
                history.getChangedAt()
        );
    }
}
