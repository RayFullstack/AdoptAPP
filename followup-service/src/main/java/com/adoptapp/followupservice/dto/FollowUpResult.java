package com.adoptapp.followupservice.dto;

import com.adoptapp.followupservice.model.FollowUpStatus;

<<<<<<< HEAD
public record FollowUpResult(

        Long id,
        String adopterName,
        String petName,
        String visitDate,
        String comments,
        FollowUpStatus status
) {
}
=======
import java.time.LocalDateTime;

public record FollowUpResult(
        Long id,
        String adopterName,
        String petName,
        Long userId,
        Long petId,
        Long adoptionId,
        LocalDateTime visitDate,
        String comments,
        FollowUpStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
>>>>>>> origin/camila-dev
