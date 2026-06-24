package com.adoptapp.shelterservice.service;

import com.adoptapp.shelterservice.controller.ShelterController;
import com.adoptapp.shelterservice.dto.ShelterResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ShelterLinkAssembler {

    public EntityModel<ShelterResponse> toModel(ShelterResponse shelter) {
        EntityModel<ShelterResponse> model = EntityModel.of(shelter);

        model.add(linkTo(methodOn(ShelterController.class)
                .getShelterById(shelter.id()))
                .withSelfRel());

        model.add(linkTo(methodOn(ShelterController.class)
                .getHistory(shelter.id()))
                .withRel("history"));

        model.add(linkTo(methodOn(ShelterController.class)
                .updateShelterById(shelter.id(), null, null))
                .withRel("update"));

        model.add(linkTo(methodOn(ShelterController.class)
                .deleteShelterById(shelter.id(), null))
                .withRel("delete"));

        return model;
    }
}