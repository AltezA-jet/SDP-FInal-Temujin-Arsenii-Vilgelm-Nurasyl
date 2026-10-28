package com.gamelab.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** PostgreSQL persistence. Configure SHADOWFALL_DB_URL, SHADOWFALL_DB_USER and SHADOWFALL_DB_PASSWORD. */
public final class DatabaseManager {
    private final String url = System.getenv().getOrDefault("SHADOWFALL_DB_URL", "");
    private final String user = System.getenv().getOrDefault("SHADOWFALL_DB_USER", "postgres");
    private final String password = System.getenv().getOrDefault("SHADOWFALL_DB_PASSWORD", "");
    public boolean isConfigured() { return !url.isBlank(); }
    private Connection connect() throws SQLException {
        if (!isConfigured()) throw new SQLException("PostgreSQL is not configured; using local save file.");
        return DriverManager.getConnection(url, user, password);
    }
    public void initialize() throws SQLException {
        if (!isConfigured()) return;
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS game_saves (save_slot INTEGER PRIMARY KEY, player_name VARCHAR(80) NOT NULL, character_class VARCHAR(40) NOT NULL, pos_x DOUBLE PRECISION NOT NULL, pos_y DOUBLE PRECISION NOT NULL, velocity_y DOUBLE PRECISION NOT NULL, health INTEGER NOT NULL, max_health INTEGER NOT NULL, level INTEGER NOT NULL, experience INTEGER NOT NULL, coins INTEGER NOT NULL, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        }
    }
    public void save(SaveData d) throws SQLException {
        initialize();
        String sql = "INSERT INTO game_saves(save_slot,player_name,character_class,pos_x,pos_y,velocity_y,health,max_health,level,experience,coins,updated_at) VALUES(1,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP) ON CONFLICT(save_slot) DO UPDATE SET player_name=EXCLUDED.player_name, character_class=EXCLUDED.character_class, pos_x=EXCLUDED.pos_x, pos_y=EXCLUDED.pos_y, velocity_y=EXCLUDED.velocity_y, health=EXCLUDED.health, max_health=EXCLUDED.max_health, level=EXCLUDED.level, experience=EXCLUDED.experience, coins=EXCLUDED.coins, updated_at=CURRENT_TIMESTAMP";
        try (Connection c=connect(); PreparedStatement p=c.prepareStatement(sql)) { bind(p,d); p.executeUpdate(); }
    }
    private void bind(PreparedStatement p, SaveData d) throws SQLException { p.setString(1,d.playerName()); p.setString(2,d.characterClass()); p.setDouble(3,d.x()); p.setDouble(4,d.y()); p.setDouble(5,d.velocityY()); p.setInt(6,d.health()); p.setInt(7,d.maxHealth()); p.setInt(8,d.level()); p.setInt(9,d.experience()); p.setInt(10,d.coins()); }
    public SaveData load() throws SQLException {
        initialize();
        try (Connection c=connect(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT player_name,character_class,pos_x,pos_y,velocity_y,health,max_health,level,experience,coins FROM game_saves WHERE save_slot=1")) {
            if (!r.next()) return null;
            return new SaveData(r.getString(1),r.getString(2),r.getDouble(3),r.getDouble(4),r.getDouble(5),r.getInt(6),r.getInt(7),r.getInt(8),r.getInt(9),r.getInt(10));
        }
    }
}
