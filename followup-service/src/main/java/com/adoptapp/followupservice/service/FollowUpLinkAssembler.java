package com.adoptapp.followupservice.service;

import com.adoptapp.followupservice.controller.FollowUpController;
import com.adoptapp.followupservice.dto.FollowUpResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FollowUpLinkAssembler {

    public EntityModel<FollowUpResponse> toModel(FollowUpResponse followUp) {
        EntityModel<FollowUpResponse> model = EntityModel.of(followUp);

        model.add(linkTo(methodOn(FollowUpController.class)
                .getById(followUp.id()))
                .withSelfRel());

        model.add(linkTo(methodOn(FollowUpController.class)
                .getHistory(followUp.id()))
                .withRel("history"));

        model.add(linkTo(methodOn(FollowUpController.class)
                .update(followUp.id(), null))
                .withRel("update"));

        model.add(linkTo(methodOn(FollowUpController.class)
                .delete(followUp.id()))
                .withRel("delete"));

        return model;
    }
}