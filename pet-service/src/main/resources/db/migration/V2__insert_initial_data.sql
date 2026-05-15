WITH health1 AS (
    INSERT INTO pet_health (vaccinated, sterilized, diseases)
    VALUES (TRUE, FALSE, 'Ninguna')
    RETURNING id
)
INSERT INTO pets (name, species, race, color, age, size, personality, status, foster_id, health_id, created_at)
SELECT 'Yoni', 'Perro', 'Doberman', 'Cafe', 7, 'Grande', 'Arisco', 'AVAILABLE', 1, id, CURRENT_TIMESTAMP
FROM health1;

WITH health2 AS (
    INSERT INTO pet_health (vaccinated, sterilized, diseases)
    VALUES (FALSE, FALSE, 'NO SANO')
    RETURNING id
)
INSERT INTO pets (name, species, race, color, age, size, personality, status, foster_id, health_id, created_at)
SELECT 'Loki', 'Gato', 'Domestico pelo largo', 'Negro', 4, 'Mediano', 'Serena', 'AVAILABLE', 1, id, CURRENT_TIMESTAMP
FROM health2;

WITH health3 AS (
    INSERT INTO pet_health (vaccinated, sterilized, diseases)
    VALUES (TRUE, FALSE, 'Ninguna')
    RETURNING id
)
INSERT INTO pets (name, species, race, color, age, size, personality, status, foster_id, health_id, created_at)
SELECT 'Oso', 'Perro', 'Cocker', 'Crema', 2, 'Mediano', 'Regalon', 'NOT_AVAILABLE', 1, id, CURRENT_TIMESTAMP
FROM health3;
