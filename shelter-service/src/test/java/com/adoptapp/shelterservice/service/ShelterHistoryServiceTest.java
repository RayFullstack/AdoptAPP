package com.adoptapp.shelterservice.service;

import com.adoptapp.shelterservice.dto.ShelterHistoryResponse;
import com.adoptapp.shelterservice.model.Shelter;
import com.adoptapp.shelterservice.model.ShelterHistory;
import com.adoptapp.shelterservice.model.ShelterStatus;
import com.adoptapp.shelterservice.repository.ShelterHistoryRepository;
import com.adoptapp.shelterservice.repository.ShelterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShelterHistoryServiceTest {

    @Mock private ShelterHistoryRepository repository;
    @Mock private ShelterRepository shelterRepository;

    @InjectMocks
    private ShelterHistoryService service;

    @Test
    void recordHistory_shouldSaveHistory_whenShelterExists() {
        Shelter shelter = shelter();
        when(shelterRepository.findById(2L)).thenReturn(Optional.of(shelter));

        service.recordHistory(2L, "CREATED", "Refugio creado", 1L,
                null, "Refugio Central", null, "refugio@mail.com",
                null, "123456789", null, "Rescate animal",
                null, "ACTIVE", null, true);

        verify(repository).save(any(ShelterHistory.class));
    }

    @Test
    void getHistory_shouldReturnHistory_whenHistoryExists() {
        Shelter shelter = shelter();
        ShelterHistory history = new ShelterHistory();
        history.setShelter(shelter);
        history.setAction("CREATED");
        history.setComment("Refugio creado");
        history.setNewName("Refugio Central");
        history.setChangedAt(LocalDateTime.now());

        when(repository.findByShelterIdOrderByChangedAtDesc(2L)).thenReturn(List.of(history));

        List<ShelterHistoryResponse> result = service.getHistory(2L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().shelterId()).isEqualTo(2L);
        assertThat(result.getFirst().action()).isEqualTo("CREATED");
    }

    private Shelter shelter() {
        Shelter shelter = new Shelter();
        shelter.setId(2L);
        shelter.setName("Refugio Central");
        shelter.setEmail("refugio@mail.com");
        shelter.setPhone("123456789");
        shelter.setDescription("Rescate animal");
        shelter.setStatus(ShelterStatus.ACTIVE);
        shelter.setActive(true);
        return shelter;
    }
}
