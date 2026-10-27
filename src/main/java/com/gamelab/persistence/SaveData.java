package com.gamelab.persistence;

public record SaveData(String playerName, String characterClass, double x, double y, double velocityY, int health, int maxHealth, int level, int experience, int coins) {}
