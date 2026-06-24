package com.adoptapp.notificationservice.service;

import com.adoptapp.notificationservice.controller.NotificationController;
import com.adoptapp.notificationservice.dto.NotificationResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class NotificationLinkAssembler {

    public EntityModel<NotificationResponse> toModel(NotificationResponse notification) {
        EntityModel<NotificationResponse> model = EntityModel.of(notification);

        model.add(linkTo(methodOn(NotificationController.class)
                .getNotificationById(notification.id(), null))
                .withSelfRel());

        model.add(linkTo(methodOn(NotificationController.class)
                .updateNotificationById(notification.id(), null))
                .withRel("update"));

        model.add(linkTo(methodOn(NotificationController.class)
                .deleteNotificationById(notification.id()))
                .withRel("delete"));

        return model;
    }
}