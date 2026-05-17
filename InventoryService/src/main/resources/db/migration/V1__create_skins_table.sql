CREATE TABLE skins (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    weapon VARCHAR(50) NOT NULL,
    rarity VARCHAR(50) NOT NULL,
    wear VARCHAR(50) NOT NULL,
    float_value NUMERIC(9, 8) NOT NULL,
    image_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_owner_id ON skins(owner_id);

ALTER TABLE skins ADD CONSTRAINT chk_float_value 
    CHECK (float_value >= 0.0 AND float_value <= 1.0);
