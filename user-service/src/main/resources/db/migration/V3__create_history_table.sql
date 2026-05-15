-- V3__create_history_table.sql
-- Crea la tabla user_history para el registro de cambios de usuarios

CREATE TABLE user_history (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              previous_status VARCHAR(20),
                              new_status VARCHAR(20),
                              previous_name VARCHAR(50),
                              new_name VARCHAR(50),
                              previous_surname VARCHAR(50),
                              new_surname VARCHAR(50),
                              previous_username VARCHAR(50),
                              new_username VARCHAR(50),
                              previous_email VARCHAR(150),
                              new_email VARCHAR(150),
                              previous_phone VARCHAR(100),
                              new_phone VARCHAR(100),
                              changed_at TIMESTAMP NOT NULL,
                              comment VARCHAR(255),

                              CONSTRAINT fk_user_history_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES users(id)
                                      ON DELETE CASCADE
);

CREATE INDEX idx_user_history_user_id
    ON user_history(user_id);

CREATE INDEX idx_user_history_changed_at
    ON user_history(changed_at);