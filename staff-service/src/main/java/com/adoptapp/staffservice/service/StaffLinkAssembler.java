package com.adoptapp.staffservice.service;

import com.adoptapp.staffservice.controller.StaffController;
import com.adoptapp.staffservice.dto.StaffResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class StaffLinkAssembler {

    public EntityModel<StaffResponse> toModel(StaffResponse staff) {
        EntityModel<StaffResponse> model = EntityModel.of(staff);

        model.add(linkTo(methodOn(StaffController.class)
                .getStaffById(staff.id(), null))
                .withSelfRel());

        model.add(linkTo(methodOn(StaffController.class)
                .getStaffByUserId(staff.userId()))
                .withRel("user-staff"));

        model.add(linkTo(methodOn(StaffController.class)
                .getHistory(staff.id()))
                .withRel("history"));

        model.add(linkTo(methodOn(StaffController.class)
                .updateStaffById(staff.id(), null, null))
                .withRel("update"));

        model.add(linkTo(methodOn(StaffController.class)
                .deleteStaffById(staff.id(), null))
                .withRel("delete"));

        return model;
    }
}