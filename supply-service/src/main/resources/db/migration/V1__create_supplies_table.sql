CREATE TABLE supplies
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    description   TEXT,
    quantity         INTEGER      NOT NULL DEFAULT 0,
    unit          VARCHAR(50)  NOT NULL,
    category      VARCHAR(20)  NOT NULL CHECK (category IN ('FOOD', 'MEDICINE', 'TOYS', 'CLEANING', 'EQUIPMENT', 'OTHER')),
    shelter_id    BIGINT       NOT NULL,
    supplier_name VARCHAR(200),
    minimum_stock INTEGER      NOT NULL DEFAULT 5,
    status        VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'LOW_STOCK', 'OUT_OF_STOCK', 'DISCONTINUED')),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE supplies_history
(
    id            BIGSERIAL PRIMARY KEY,
    supply_id     BIGINT       NOT NULL REFERENCES supplies (id) ON DELETE CASCADE,
    action        VARCHAR(50)  NOT NULL,
    comment       TEXT,
    prev_status   VARCHAR(20),
    new_status    VARCHAR(20),
    prev_quantity INTEGER,
    new_quantity  INTEGER,
    prev_category VARCHAR(20),
    new_category  VARCHAR(20),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_supplies_shelter_id ON supplies (shelter_id);
CREATE INDEX idx_supplies_status ON supplies (status);
CREATE INDEX idx_supplies_category ON supplies (category);
CREATE INDEX idx_supplies_history_supply_id ON supplies_history (supply_id);
