package com.gamelab.application;

import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.persistence.DatabaseManager;
import com.gamelab.persistence.SaveData;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Facade pattern: the UI has one small API for save/load and event notifications. */
public final class GameFacade {
    private final DatabaseManager database = new DatabaseManager();
    private final GameEventBus events = new GameEventBus();
    private final Path localSave = Path.of(System.getProperty("user.home"), ".shadowfall", "save.properties");
    public GameEventBus events() { return events; }
    public void save(SaveData data) throws IOException {
        if (database.isConfigured()) {
            try { database.save(data); events.publish("SAVE", "Игра сохранена в PostgreSQL"); return; }
            catch (Exception ex) { events.publish("WARNING", "PostgreSQL недоступен, сохранение в файл: " + ex.getMessage()); }
        }
        saveLocal(data); events.publish("SAVE", "Игра сохранена локально: " + localSave);
    }
    public SaveData load() throws IOException {
        if (database.isConfigured()) {
            try { SaveData d=database.load(); if (d != null) { events.publish("LOAD", "Загрузка из PostgreSQL"); return d; } }
            catch (Exception ex) { events.publish("WARNING", "PostgreSQL недоступен, пробую локальный файл: " + ex.getMessage()); }
        }
        SaveData d=loadLocal(); if (d != null) events.publish("LOAD", "Загрузка из локального файла"); return d;
    }
    private void saveLocal(SaveData d) throws IOException {
        Properties p=new Properties(); p.setProperty("name",d.playerName()); p.setProperty("class",d.characterClass()); p.setProperty("x",Double.toString(d.x())); p.setProperty("y",Double.toString(d.y())); p.setProperty("vy",Double.toString(d.velocityY())); p.setProperty("health",Integer.toString(d.health())); p.setProperty("maxHealth",Integer.toString(d.maxHealth())); p.setProperty("level",Integer.toString(d.level())); p.setProperty("xp",Integer.toString(d.experience())); p.setProperty("coins",Integer.toString(d.coins()));
        Files.createDirectories(localSave.getParent()); try (OutputStream out=Files.newOutputStream(localSave)) { p.store(out,"Shadowfall save"); }
    }
    private SaveData loadLocal() throws IOException {
        if (!Files.exists(localSave)) return null; Properties p=new Properties(); try (InputStream in=Files.newInputStream(localSave)) { p.load(in); }
        try { return new SaveData(p.getProperty("name","Hero"),p.getProperty("class","Rogue"),Double.parseDouble(p.getProperty("x","100")),Double.parseDouble(p.getProperty("y","350")),Double.parseDouble(p.getProperty("vy","0")),Integer.parseInt(p.getProperty("health","100")),Integer.parseInt(p.getProperty("maxHealth","100")),Integer.parseInt(p.getProperty("level","1")),Integer.parseInt(p.getProperty("xp","0")),Integer.parseInt(p.getProperty("coins","0"))); } catch (RuntimeException ex) { throw new IOException("Файл сохранения повреждён",ex); }
    }
}
