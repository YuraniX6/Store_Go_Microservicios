CREATE TABLE catalog_skins (
    id            VARCHAR(50)      NOT NULL,
    name          VARCHAR(200)     NOT NULL,
    description   TEXT,
    image         VARCHAR(500),
    weapon_id     VARCHAR(50),
    weapon_name   VARCHAR(100),
    category_id   VARCHAR(100),
    category_name VARCHAR(100),
    rarity_id     VARCHAR(50),
    rarity_name   VARCHAR(50),
    rarity_color  VARCHAR(10),
    min_float     NUMERIC(10, 8),
    max_float     NUMERIC(10, 8),
    stattrak      BOOLEAN          NOT NULL DEFAULT FALSE,
    souvenir      BOOLEAN          NOT NULL DEFAULT FALSE,
    paint_index   VARCHAR(20),
    legacy_model  BOOLEAN          NOT NULL DEFAULT FALSE,
    raw_data      JSONB            NOT NULL,
    created_at    TIMESTAMPTZ      NOT NULL,
    updated_at    TIMESTAMPTZ      NOT NULL,

    CONSTRAINT pk_catalog_skins PRIMARY KEY (id)
);

CREATE INDEX idx_catalog_skins_weapon_name   ON catalog_skins (weapon_name);
CREATE INDEX idx_catalog_skins_category_name ON catalog_skins (category_name);
CREATE INDEX idx_catalog_skins_rarity_name   ON catalog_skins (rarity_name);
CREATE INDEX idx_catalog_skins_name_lower    ON catalog_skins (LOWER(name));
