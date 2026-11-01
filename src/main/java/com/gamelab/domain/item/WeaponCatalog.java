package com.gamelab.domain.item;

import java.util.List;

public final class WeaponCatalog {

    private WeaponCatalog() {
    }

    public static Weapon createByLevel(int level) {
        return switch (level) {
            case 1 -> createStarterSword();
            case 2 -> createSwiftBlade();
            case 3 -> createGreatsword();
            case 4 -> createDarksteelSword();
            case 5 -> createAncientBlade();

            default -> throw new IllegalArgumentException(
                    "Weapon level must be between 1 and 5"
            );
        };
    }

    public static Weapon createStarterSword() {
        return new BasicWeapon(
                "Starter Sword",
                "A simple sword for a new knight.",
                0,
                "KNIGHT",
                10,
                1.0,
                1,
                1
        );
    }

    public static Weapon createSwiftBlade() {
        return new BasicWeapon(
                "Swift Blade",
                "A light blade with increased attack speed.",
                120,
                "KNIGHT",
                12,
                1.35,
                2,
                2
        );
    }

    public static Weapon createGreatsword() {
        return new BasicWeapon(
                "Greatsword",
                "A heavy sword that deals high damage.",
                220,
                "KNIGHT",
                18,
                0.80,
                2,
                3
        );
    }

    public static Weapon createDarksteelSword() {
        return new BasicWeapon(
                "Darksteel Sword",
                "A balanced sword forged from dark steel.",
                350,
                "KNIGHT",
                21,
                1.0,
                3,
                4
        );
    }

    public static Weapon createAncientBlade() {
        return new BasicWeapon(
                "Ancient Blade",
                "A legendary blade created from light and dark metal.",
                500,
                "KNIGHT",
                26,
                1.10,
                4,
                5
        );
    }

    public static List<Weapon> createAll() {
        return List.of(
                createStarterSword(),
                createSwiftBlade(),
                createGreatsword(),
                createDarksteelSword(),
                createAncientBlade()
        );
    }
}