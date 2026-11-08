package com.gamelab.application.audio;

/** Actual weapon damage, kept separate from diagnostic damage and status effects. */
public record WeaponImpact(Surface surface, int damage) {
    public enum Surface { FLESH, HARD, DEMON, SLIME, MYSTIC, ETHEREAL }
}
