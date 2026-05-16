package com.adoptapp.followupservice.dto;

import com.adoptapp.followupservice.model.FollowUpStatus;

public record FollowUpResponse(

        Long id,
        String adopterName,
        String petName,
        String visitDate,
        String comments,
        FollowUpStatus status
) {
}