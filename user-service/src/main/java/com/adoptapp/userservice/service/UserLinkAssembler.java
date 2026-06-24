package com.adoptapp.userservice.service;

import com.adoptapp.userservice.controller.UserController;
import com.adoptapp.userservice.dto.UserResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserLinkAssembler {

    public EntityModel<UserResponse> toModel(UserResponse user) {
        EntityModel<UserResponse> model = EntityModel.of(user);

        model.add(linkTo(methodOn(UserController.class)
                .getUserById(user.id()))
                .withSelfRel());

        model.add(linkTo(methodOn(UserController.class)
                .getUserByEmail(user.email()))
                .withRel("by-email"));

        model.add(linkTo(methodOn(UserController.class)
                .getHistory(user.id()))
                .withRel("history"));

        model.add(linkTo(methodOn(UserController.class)
                .updateUserById(user.id(), null))
                .withRel("update"));

        model.add(linkTo(methodOn(UserController.class)
                .deleteUserById(user.id()))
                .withRel("delete"));

        return model;
    }
}