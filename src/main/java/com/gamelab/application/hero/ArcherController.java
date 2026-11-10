package com.gamelab.application.hero;

import com.gamelab.domain.PlayerCharacter;

/** Archer controls: fast movement and a physical long-range arrow attack. */
public final class ArcherController implements HeroController {
    public static final int BASE_QUIVER_CAPACITY = 10;
    private final PlayerCharacter player;
    private int capacity = BASE_QUIVER_CAPACITY;
    private int arrows = BASE_QUIVER_CAPACITY;
    private double restoreProgress;
    private double refillSeconds = 2.0;
    public ArcherController(PlayerCharacter player) { this.player = java.util.Objects.requireNonNull(player); }
    @Override public PlayerCharacter player() { return player; }
    @Override public String classId() { return "ARCHER"; }
    @Override public boolean ranged() { return true; }
    @Override public boolean magical() { return false; }
    @Override public double movementSpeed(boolean crouching, boolean slowed, boolean attacking, boolean dodging) {
        double speed = crouching ? 175 : 360;
        if (slowed) speed *= .6;
        if (attacking) speed *= .65;
        return dodging ? 640 : speed;
    }
    @Override public double projectileSpeed() { return 720; }
    @Override public double projectileRadius() { return 10; }
    @Override public String projectileSource() { return "player-archer"; }
    @Override public int resourceCost() { return 0; }
    @Override public String attackLabel() { return "Выстрел из лука"; }
    @Override public boolean canAttack() { return arrows > 0; }
    @Override public boolean beginAttack() { if (arrows <= 0) return false; arrows--; return true; }
    @Override public void tick(double seconds) {
        if (arrows >= capacity) { restoreProgress = 0; return; }
        restoreProgress += seconds;
        while (restoreProgress >= refillSeconds && arrows < capacity) {
            arrows++;
            restoreProgress -= refillSeconds;
        }
    }
    @Override public void restore() { player.heal(player.getMaxHealth()); arrows = capacity; restoreProgress = 0; }
    @Override public String resourceHud() { return "Arrows " + arrows + " / " + capacity + "  (+1 every " + String.format(java.util.Locale.ROOT,"%.1f",refillSeconds) + " s)"; }
    public void configureQuiver(int newCapacity, double newRefillSeconds) {
        capacity = Math.max(BASE_QUIVER_CAPACITY, newCapacity);
        refillSeconds = Math.min(2.0, Math.max(.5, newRefillSeconds));
        arrows = Math.min(capacity, Math.max(arrows, BASE_QUIVER_CAPACITY));
    }
    public boolean consumeArrows(int amount) {
        if (amount < 1 || arrows < amount) return false;
        arrows -= amount;
        return true;
    }
}
