-- V2__insert_initial_data.sql

INSERT INTO users (
    username,
    name,
    surname,
    email,
    status,
    created_at
) VALUES
      (
          'LisaS',
          'Lisa',
          'Simpson',
          'lsimpson@mail.com',
          'ACTIVE',
          NOW()
      ),
      (
          'HomoSimp',
          'Homero',
          'Simpson',
          'homerosimp@mail.com',
          'ACTIVE',
          NOW()
      ),
      (
          'StacyBakr',
          'Stacy',
          'Baker',
          'fakemail123@mail.com',
          'ACTIVE',
          NOW()
      );

INSERT INTO user_phones (
    number,
    user_id
) VALUES
      (
          '123456789',
          1
      ),
      (
          '954456881',
          2
      ),
      (
          '9564422331',
          3
      );

INSERT INTO user_addresses (
    country,
    city,
    street,
    home_number,
    postal_code,
    type,
    primary_address,
    user_id
) VALUES
      (
          'Chile',
          'Santiago',
          'Av. Siempreviva',
          '123',
          '123456',
          'HOME',
          true,
          1
      ),
      (
          'Chile',
          'Santiago',
          'Av. Siempreviva',
          '456',
          '345678',
          'HOME',
          true,
          2
      ),
      (
          'Chile',
          'Santiago',
          'Av. Real',
          '7865',
          '22222455',
          'WORK',
          true,
          3
      );