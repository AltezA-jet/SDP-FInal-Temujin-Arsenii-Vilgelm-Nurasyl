package com.gamelab.application.hero;

import com.gamelab.domain.PlayerCharacter;

/** Class-specific rules used by the arena to control a playable hero. */
public interface HeroController {
    PlayerCharacter player();
    String classId();
    boolean ranged();
    boolean magical();
    double movementSpeed(boolean crouching, boolean slowed, boolean attacking, boolean dodging);
    double projectileSpeed();
    double projectileRadius();
    String projectileSource();
    int resourceCost();
    String attackLabel();

    default boolean canAttack() {
        return !magical() || player().getMana() >= resourceCost();
    }

    default boolean beginAttack() {
        return !magical() || player().spendMana(resourceCost());
    }

    default void tick(double seconds) { }

    /** Optional class resource displayed by the combat HUD. */
    default String resourceHud() { return ""; }

    default void receiveDamage(int damage) { player().takeDamage(damage); }
    default int health() { return player().getHealth(); }
    default int maxHealth() { return player().getMaxHealth(); }
    default boolean alive() { return player().isAlive(); }
    default void restore() { player().heal(player().getMaxHealth()); player().restoreFullMana(); }
}
