package com.adoptapp.donationservice.service;

import com.adoptapp.donationservice.controller.DonationController;
import com.adoptapp.donationservice.dto.DonationResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class DonationLinkAssembler {

    public EntityModel<DonationResponse> toModel(DonationResponse donation) {
        EntityModel<DonationResponse> model = EntityModel.of(donation);

        model.add(linkTo(methodOn(DonationController.class)
                .getDonationById(donation.id()))
                .withSelfRel());

        model.add(linkTo(methodOn(DonationController.class)
                .getHistory(donation.id()))
                .withRel("history"));

        model.add(linkTo(methodOn(DonationController.class)
                .updateDonationById(donation.id(), null))
                .withRel("update"));

        model.add(linkTo(methodOn(DonationController.class)
                .deleteDonationById(donation.id()))
                .withRel("delete"));

        return model;
    }
}