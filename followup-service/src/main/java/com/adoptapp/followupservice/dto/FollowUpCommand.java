package com.adoptapp.followupservice.dto;

import com.adoptapp.followupservice.model.FollowUpStatus;

public record FollowUpCommand(

        String adopterName,
        String petName,
        String visitDate,
        String comments,
        FollowUpStatus status
) {
}