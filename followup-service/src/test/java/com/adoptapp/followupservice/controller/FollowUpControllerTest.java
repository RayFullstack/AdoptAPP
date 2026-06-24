package com.adoptapp.followupservice.controller;

import com.adoptapp.followupservice.dto.FollowUpResult;
import com.adoptapp.followupservice.model.FollowUpStatus;
import com.adoptapp.followupservice.service.FollowUpLinkAssembler;
import com.adoptapp.followupservice.service.FollowUpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowUpControllerTest {

    @Mock
    private FollowUpService service;

    private FollowUpController controller;

    @BeforeEach
    void setUp() {
        controller = new FollowUpController(service, new FollowUpLinkAssembler());
    }

    @Test
    void getById_shouldReturnOk_whenFollowUpExists() {
        FollowUpResult result = new FollowUpResult(1L, "Camila", "Benito", 7L, 10L, 3L,
                LocalDateTime.now(), "Todo bien", FollowUpStatus.PENDING, LocalDateTime.now(), LocalDateTime.now());
        when(service.getById(1L)).thenReturn(Optional.of(result));

        ResponseEntity<?> response = controller.getById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getById_shouldReturnNotFound_whenFollowUpDoesNotExist() {
        when(service.getById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getById(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}