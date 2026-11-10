package com.gamelab.application.audio;

/** Actual health loss, allowing presentation to distinguish a hit from a fatal blow. */
public record PlayerDamage(int amount,boolean fatal) {
    public PlayerDamage { if(amount<=0) throw new IllegalArgumentException("Damage must be positive"); }
}
