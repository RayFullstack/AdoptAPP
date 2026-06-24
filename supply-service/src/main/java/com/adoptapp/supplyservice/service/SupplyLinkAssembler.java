package com.adoptapp.supplyservice.service;

import com.adoptapp.supplyservice.controller.SupplyController;
import com.adoptapp.supplyservice.dto.SupplyResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SupplyLinkAssembler {

    public EntityModel<SupplyResponse> toModel(SupplyResponse supply) {
        EntityModel<SupplyResponse> model = EntityModel.of(supply);

        model.add(linkTo(methodOn(SupplyController.class)
                .getSupplyById(supply.id(), null))
                .withSelfRel());

        model.add(linkTo(methodOn(SupplyController.class)
                .getSuppliesByShelter(supply.shelterId(), null))
                .withRel("shelter-supplies"));

        model.add(linkTo(methodOn(SupplyController.class)
                .getSupplyHistory(supply.id()))
                .withRel("history"));

        model.add(linkTo(methodOn(SupplyController.class)
                .updateSupply(supply.id(), null, null))
                .withRel("update"));

        model.add(linkTo(methodOn(SupplyController.class)
                .deleteSupply(supply.id(), null))
                .withRel("delete"));

        return model;
    }
}