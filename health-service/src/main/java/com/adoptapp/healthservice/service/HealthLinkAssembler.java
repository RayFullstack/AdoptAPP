package com.adoptapp.healthservice.service;

import com.adoptapp.healthservice.controller.HealthController;
import com.adoptapp.healthservice.dto.HealthResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class HealthLinkAssembler {

    public EntityModel<HealthResponse> toModel(HealthResponse health) {
        EntityModel<HealthResponse> model = EntityModel.of(health);

        model.add(linkTo(methodOn(HealthController.class)
                .getHealthById(health.id()))
                .withSelfRel());

        model.add(linkTo(methodOn(HealthController.class)
                .getHealthByPetId(health.petId()))
                .withRel("pet-health"));

        model.add(linkTo(methodOn(HealthController.class)
                .getHistory(health.id()))
                .withRel("history"));

        model.add(linkTo(methodOn(HealthController.class)
                .updateById(health.id(), null, null))
                .withRel("update"));

        model.add(linkTo(methodOn(HealthController.class)
                .deleteById(health.id(), null))
                .withRel("delete"));

        return model;
    }
}