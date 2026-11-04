package com.gamelab.application;

/** Immutable health snapshot published after combat changes an enemy. */
public record EnemyHealthState(String id, String name, int health, int maxHealth,
                               double x, double y, boolean boss, boolean alive) { }
