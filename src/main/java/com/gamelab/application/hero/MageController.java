package com.gamelab.application.hero;

import com.gamelab.domain.PlayerCharacter;

/** Mage controls: slower movement, mana management and a fireball attack. */
public final class MageController implements HeroController {
    public static final int FIREBALL_COST = 24;
    private final PlayerCharacter player;
    private double manaRemainder;
    public MageController(PlayerCharacter player) { this.player = java.util.Objects.requireNonNull(player); }
    @Override public PlayerCharacter player() { return player; }
    @Override public String classId() { return "MAGE"; }
    @Override public boolean ranged() { return true; }
    @Override public boolean magical() { return true; }
    @Override public double movementSpeed(boolean crouching, boolean slowed, boolean attacking, boolean dodging) {
        double speed = crouching ? 150 : 275;
        if (slowed) speed *= .6;
        if (attacking) speed *= .65;
        return dodging ? 640 : speed;
    }
    @Override public double projectileSpeed() { return 560; }
    @Override public double projectileRadius() { return 18; }
    @Override public String projectileSource() { return "player-mage"; }
    @Override public int resourceCost() { return FIREBALL_COST; }
    @Override public String attackLabel() { return "Колдовство: огненный шар"; }
    @Override public void tick(double seconds) {
        if (player.getMana() >= player.getMaxMana()) return;
        manaRemainder += seconds * 7.0 * (1 + player.getResourceRegenerationBonusPercent() / 100.0);
        int restored = (int) manaRemainder;
        if (restored > 0) { player.restoreMana(restored); manaRemainder -= restored; }
    }
}
