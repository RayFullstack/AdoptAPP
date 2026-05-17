INSERT INTO notification_types (name, template, channel) VALUES
    -- User Service
    ('USER_CREATED', 'El usuario {userName} ha sido registrado', 'EMAIL'),
    ('USER_UPDATED', 'Los datos del usuario {userName} han sido actualizados', 'EMAIL'),
    ('USER_DELETED', 'El usuario {userName} ha sido eliminado', 'EMAIL'),

    -- Donation Service
    ('DONATION_RECEIVED', 'Se ha recibido una donación de {amount} de {donorName}', 'EMAIL'),
    ('DONATION_UPDATED', 'La donación {donationId} ha sido actualizada', 'EMAIL'),
    ('DONATION_CANCELLED', 'La donación {donationId} ha sido cancelada', 'EMAIL'),

    -- Follow-up Service
    ('FOLLOWUP_SCHEDULED', 'Se ha programado un seguimiento para la adopción {adoptionId}', 'EMAIL'),
    ('FOLLOWUP_COMPLETED', 'El seguimiento {followUpId} ha sido completado', 'EMAIL'),
    ('FOLLOWUP_CANCELLED', 'El seguimiento {followUpId} ha sido cancelado', 'EMAIL'),

    -- Health Service
    ('HEALTH_CHECK_CREATED', 'Se ha registrado un control de salud para la mascota {petId}', 'PUSH'),
    ('HEALTH_CHECK_UPDATED', 'El control de salud {healthId} ha sido actualizado', 'PUSH'),
    ('HEALTH_ALERT', 'Alerta de salud para la mascota {petId}: {description}', 'PUSH'),

    -- Shelter Service
    ('SHELTER_CREATED', 'El refugio {shelterName} ha sido registrado', 'EMAIL'),
    ('SHELTER_UPDATED', 'Los datos del refugio {shelterName} han sido actualizados', 'EMAIL'),
    ('SHELTER_DELETED', 'El refugio {shelterName} ha sido eliminado', 'EMAIL'),

    -- Staff Service
    ('STAFF_ADDED', 'El usuario {userName} ha sido añadido al staff de {shelterName}', 'EMAIL'),
    ('STAFF_REMOVED', 'El usuario {userName} ha sido removido del staff de {shelterName}', 'EMAIL'),

    -- Supply Service
    ('SUPPLY_LOW_STOCK', 'El insumo {supplyName} tiene stock bajo: {quantity}', 'PUSH'),
    ('SUPPLY_ORDERED', 'Se ha solicitado {supplyName} para el refugio {shelterName}', 'EMAIL'),
    ('SUPPLY_RECEIVED', 'Se ha recibido {supplyName} en el refugio {shelterName}', 'EMAIL');
