INSERT INTO notification_types (name, template, channel) VALUES
    ('ADOPTION_CREATED', 'Se ha creado la adopción de la mascota {petId} por el usuario {userName}', 'EMAIL'),
    ('ADOPTION_UPDATED', 'La adopción {adoptionId} ha sido actualizada a estado {status}', 'EMAIL'),
    ('ADOPTION_DELETED', 'La adopción {adoptionId} de la mascota {petId} ha sido eliminada', 'EMAIL'),
    ('PET_CREATED', 'La mascota {name} ha sido registrada', 'PUSH'),
    ('PET_UPDATED', 'Los datos de {name} han sido actualizados', 'PUSH'),
    ('PET_DELETED', 'La mascota {name} ha sido eliminada', 'PUSH'),
    ('PET_STATUS_CHANGED', 'La mascota {name} cambió a estado {status}', 'PUSH');
