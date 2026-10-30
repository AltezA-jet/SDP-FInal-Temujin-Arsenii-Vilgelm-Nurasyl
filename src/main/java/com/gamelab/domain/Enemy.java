package com.gamelab.domain;

import com.gamelab.pattern.strategy.EnemyBehavior;
import java.util.Objects;

/** Domain model for an enemy. It has no JavaFX dependency. */
public final class Enemy {
    private final String id;
    private final String name;
    private final int maxHealth;
    private final int damage;
    private final int rewardCoins;
    private final int rewardExperience;
    private final double movementSpeed;
    private int health;
    private double x;
    private double y;
    private EnemyBehavior behavior;

    public Enemy(String id, String name, int maxHealth, int damage,
                 double x, double y, double movementSpeed,
                 int rewardCoins, int rewardExperience, EnemyBehavior behavior) {
        if (id == null || id.isBlank() || name == null || name.isBlank())
            throw new IllegalArgumentException("Enemy id and name are required");
        if (maxHealth <= 0 || damage < 0 || movementSpeed < 0 || rewardCoins < 0 || rewardExperience < 0)
            throw new IllegalArgumentException("Invalid enemy stats");
        if (!Double.isFinite(x) || !Double.isFinite(y)) throw new IllegalArgumentException("Position must be finite");
        this.id = id; this.name = name; this.maxHealth = maxHealth; this.health = maxHealth;
        this.damage = damage; this.x = x; this.y = y; this.movementSpeed = movementSpeed;
        this.rewardCoins = rewardCoins; this.rewardExperience = rewardExperience;
        this.behavior = Objects.requireNonNull(behavior, "Behavior cannot be null");
    }

    public void takeDamage(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Damage cannot be negative");
        health = Math.max(0, health - amount);
    }
    public void update(double playerX, double playerY, double deltaSeconds) {
        if (!Double.isFinite(deltaSeconds) || deltaSeconds < 0) throw new IllegalArgumentException("Delta must be finite and non-negative");
        if (isAlive()) behavior.update(this, playerX, playerY, deltaSeconds);
    }
    public void moveTowards(double targetX, double targetY, double deltaSeconds) {
        double dx = targetX - x, dy = targetY - y, distance = Math.hypot(dx, dy);
        if (distance == 0 || deltaSeconds == 0) return;
        double step = Math.min(distance, movementSpeed * deltaSeconds);
        x += dx / distance * step; y += dy / distance * step;
    }
    public void moveAway(double targetX, double targetY, double deltaSeconds) {
        double dx = x - targetX, dy = y - targetY, distance = Math.hypot(dx, dy);
        if (distance == 0 || deltaSeconds == 0) return;
        double step = movementSpeed * deltaSeconds;
        x += dx / distance * step; y += dy / distance * step;
    }
    public void setBehavior(EnemyBehavior behavior) { this.behavior = Objects.requireNonNull(behavior); }
    public String getId() { return id; }
    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getDamage() { return damage; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getMovementSpeed() { return movementSpeed; }
    public int getRewardCoins() { return rewardCoins; }
    public int getRewardExperience() { return rewardExperience; }
    public EnemyBehavior getBehavior() { return behavior; }
    public boolean isAlive() { return health > 0; }
}
