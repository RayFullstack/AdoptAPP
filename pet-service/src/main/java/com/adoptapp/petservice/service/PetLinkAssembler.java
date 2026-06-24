package com.adoptapp.petservice.service;

import com.adoptapp.petservice.controller.PetController;
import com.adoptapp.petservice.dto.PetResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PetLinkAssembler {

    public EntityModel<PetResponse> toModel(PetResponse pet) {
        EntityModel<PetResponse> model = EntityModel.of(pet);

        model.add(linkTo(methodOn(PetController.class)
                .getPetById(pet.id()))
                .withSelfRel());

        model.add(linkTo(methodOn(PetController.class)
                .getPetHealth(pet.id()))
                .withRel("health"));

        model.add(linkTo(methodOn(PetController.class)
                .getHistory(pet.id(), null))
                .withRel("history"));

        if (!"DELETED".equalsIgnoreCase(pet.status())) {
            model.add(linkTo(methodOn(PetController.class)
                    .updatePetById(pet.id(), null, null))
                    .withRel("update"));

            model.add(linkTo(methodOn(PetController.class)
                    .updatePetByStatus(pet.id(), null, null))
                    .withRel("update-status"));

            model.add(linkTo(methodOn(PetController.class)
                    .deletePetById(pet.id()))
                    .withRel("delete"));
        }

        return model;
    }
}