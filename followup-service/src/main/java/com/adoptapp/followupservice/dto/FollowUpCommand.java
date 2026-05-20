package com.adoptapp.followupservice.dto;

import com.adoptapp.followupservice.model.FollowUpStatus;

<<<<<<< HEAD
public record FollowUpCommand(

        String adopterName,
        String petName,
        String visitDate,
        String comments,
        FollowUpStatus status
) {
}
=======
import java.time.LocalDateTime;

public record FollowUpCommand(
        String adopterName,
        String petName,
        Long userId,
        Long petId,
        Long adoptionId,
        LocalDateTime visitDate,
        String comments,
        FollowUpStatus status
) {
}
>>>>>>> origin/camila-dev
