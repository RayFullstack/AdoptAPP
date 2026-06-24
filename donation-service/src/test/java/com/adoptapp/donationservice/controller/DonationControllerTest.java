package com.adoptapp.donationservice.controller;

import com.adoptapp.donationservice.dto.DonationResult;
import com.adoptapp.donationservice.model.DonationStatus;
import com.adoptapp.donationservice.service.DonationLinkAssembler;
import com.adoptapp.donationservice.service.DonationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonationControllerTest {

    @Mock
    private DonationService service;

    private DonationController controller;

    @BeforeEach
    void setUp() {
        controller = new DonationController(service, new DonationLinkAssembler());
    }

    @Test
    void getDonationById_shouldReturnOk_whenDonationExists() {
        DonationResult result = new DonationResult(1L, "Camila", BigDecimal.TEN, "Alimento",
                DonationStatus.PENDING, 7L, 2L, LocalDateTime.now(), LocalDateTime.now());
        when(service.getById(1L)).thenReturn(Optional.of(result));

        ResponseEntity<?> response = controller.getDonationById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getDonationById_shouldReturnNotFound_whenDonationDoesNotExist() {
        when(service.getById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getDonationById(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}