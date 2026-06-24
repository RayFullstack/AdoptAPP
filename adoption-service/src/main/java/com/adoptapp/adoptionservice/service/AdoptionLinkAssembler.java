package com.adoptapp.adoptionservice.service;

import com.adoptapp.adoptionservice.controller.AdoptionController;
import com.adoptapp.adoptionservice.dto.AdoptionResponse;
import com.adoptapp.adoptionservice.model.AdoptionStatus;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AdoptionLinkAssembler {

    public EntityModel<AdoptionResponse> toModel(AdoptionResponse adoption) {
        EntityModel<AdoptionResponse> model = EntityModel.of(adoption);

        model.add(linkTo(methodOn(AdoptionController.class)
                .getAdoptionById(adoption.id(), null))
                .withSelfRel());

        model.add(linkTo(methodOn(AdoptionController.class)
                .getHistory(adoption.id(), null))
                .withRel("history"));

        if (adoption.status() == AdoptionStatus.PENDING) {
            model.add(linkTo(methodOn(AdoptionController.class)
                    .updateAdoptionById(adoption.id(), null, null))
                    .withRel("update-status"));

            model.add(linkTo(methodOn(AdoptionController.class)
                    .deleteAdoptionById(adoption.id(), null))
                    .withRel("cancel"));
        }

        return model;
    }
}
