package com.adoptapp.adoptionservice.dto;

<<<<<<< HEAD
=======
import com.adoptapp.adoptionservice.model.AdoptionStatus;
import java.time.LocalDateTime;

>>>>>>> origin/camila-dev
public record AdoptionResult(

        Long id,
        Long userId,
        Long petId,
<<<<<<< HEAD
        String status
=======
        AdoptionStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
>>>>>>> origin/camila-dev
) {
}