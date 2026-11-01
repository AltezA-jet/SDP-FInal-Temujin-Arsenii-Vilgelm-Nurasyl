package com.gamelab.domain.rune;

import java.util.List;
import java.util.Objects;

public final class RuneCatalog {

    private RuneCatalog() {
    }

    public static Rune create(RuneType type) {
        Objects.requireNonNull(
                type,
                "Rune type cannot be null"
        );

        return switch (type) {
            case FIRE -> new Rune(
                    type,
                    "Fire Rune",
                    "Deals additional damage to ice enemies and burns them.",
                    0
            );

            case ICE -> new Rune(
                    type,
                    "Ice Rune",
                    "Deals additional damage to fire enemies and slows them.",
                    0
            );

            case PURITY -> new Rune(
                    type,
                    "Purity Rune",
                    "Deals additional damage to poisoned and corrupted enemies.",
                    0
            );

            case HOLY -> new Rune(
                    type,
                    "Holy Rune",
                    "Deals additional damage to shadow and undead enemies.",
                    0
            );

            case ANCIENT -> new Rune(
                    type,
                    "Ancient Rune",
                    "Increases damage against bosses and breaks boss protection.",
                    0
            );

            case POISON -> new Rune(
                    type,
                    "Poison Rune",
                    "Applies poison damage over time.",
                    90
            );

            case VAMPIRE -> new Rune(
                    type,
                    "Vampire Rune",
                    "Restores part of the damage as health.",
                    160
            );

            case LIGHTNING -> new Rune(
                    type,
                    "Lightning Rune",
                    "Transfers part of the damage to another enemy.",
                    150
            );

            case FURY -> new Rune(
                    type,
                    "Fury Rune",
                    "Increases weapon damage.",
                    120
            );

            case HASTE -> new Rune(
                    type,
                    "Haste Rune",
                    "Increases attack speed.",
                    120
            );

            case CRITICAL -> new Rune(
                    type,
                    "Critical Rune",
                    "Adds a chance to deal critical damage.",
                    140
            );
        };
    }

    public static List<Rune> createSpecialRunes() {
        return List.of(
                create(RuneType.FIRE),
                create(RuneType.ICE),
                create(RuneType.PURITY),
                create(RuneType.HOLY),
                create(RuneType.ANCIENT)
        );
    }

    public static List<Rune> createShopRunes() {
        return List.of(
                create(RuneType.POISON),
                create(RuneType.VAMPIRE),
                create(RuneType.LIGHTNING),
                create(RuneType.FURY),
                create(RuneType.HASTE),
                create(RuneType.CRITICAL)
        );
    }
}