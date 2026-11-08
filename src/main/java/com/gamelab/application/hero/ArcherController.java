package com.gamelab.application.hero;

import com.gamelab.domain.PlayerCharacter;

/** Archer controls: fast movement and a physical long-range arrow attack. */
public final class ArcherController implements HeroController {
    private final PlayerCharacter player;
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
}
