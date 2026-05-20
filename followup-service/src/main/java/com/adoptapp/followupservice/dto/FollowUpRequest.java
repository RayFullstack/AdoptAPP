package com.adoptapp.followupservice.dto;

import com.adoptapp.followupservice.model.FollowUpStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

<<<<<<< HEAD
=======
import java.time.LocalDateTime;

>>>>>>> origin/camila-dev
public record FollowUpRequest(

        @NotBlank(message = "Adopter name is required")
        String adopterName,

        @NotBlank(message = "Pet name is required")
        String petName,

<<<<<<< HEAD
        @NotBlank(message = "Visit date is required")
        String visitDate,

        @NotBlank(message = "Comments are required")
=======
        Long userId,

        Long petId,

        Long adoptionId,

        @NotNull(message = "Visit date is required")
        LocalDateTime visitDate,

>>>>>>> origin/camila-dev
        String comments,

        @NotNull(message = "Status is required")
        FollowUpStatus status
) {
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/camila-dev
