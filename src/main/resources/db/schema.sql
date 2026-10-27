-- PostgreSQL schema. Run this in the shadowfall database (or let DatabaseManager create game_saves).
CREATE TABLE IF NOT EXISTS game_saves (
                                          save_slot INTEGER PRIMARY KEY,
                                          player_name VARCHAR(80) NOT NULL,
    character_class VARCHAR(40) NOT NULL,
    pos_x DOUBLE PRECISION NOT NULL,
    pos_y DOUBLE PRECISION NOT NULL,
    velocity_y DOUBLE PRECISION NOT NULL DEFAULT 0,
    health INTEGER NOT NULL,
    max_health INTEGER NOT NULL,
    level INTEGER NOT NULL DEFAULT 1,
    experience INTEGER NOT NULL DEFAULT 0,
    coins INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );
