package com.gamelab.application.hero;

import com.gamelab.domain.PlayerCharacter;

/** Default controller for warrior and future non-ranged classes. */
public final class BasicHeroController implements HeroController {
    private final PlayerCharacter player;
    public BasicHeroController(PlayerCharacter player) { this.player = java.util.Objects.requireNonNull(player); }
    @Override public PlayerCharacter player() { return player; }
    @Override public String classId() {
        String value = player.getCharacterClass().toUpperCase(java.util.Locale.ROOT);
        return value.contains("ASSASSIN") || value.contains("ROGUE") ? "ASSASSIN" : "WARRIOR";
    }
    @Override public boolean ranged() { return false; }
    @Override public boolean magical() { return false; }
    @Override public double movementSpeed(boolean crouching, boolean slowed, boolean attacking, boolean dodging) {
        double speed = crouching ? 150 : classId().equals("ASSASSIN") ? 350 : 310;
        if (slowed) speed *= .6;
        if (attacking) speed *= .65;
        return dodging ? 640 : speed;
    }
    @Override public double projectileSpeed() { return 0; }
    @Override public double projectileRadius() { return 0; }
    @Override public String projectileSource() { return ""; }
    @Override public int resourceCost() { return 0; }
    @Override public String attackLabel() { return "Удар оружием"; }
}
