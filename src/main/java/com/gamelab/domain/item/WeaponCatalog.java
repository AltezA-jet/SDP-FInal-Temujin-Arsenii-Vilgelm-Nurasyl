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

    /** Class-specific weapons used for a new run and the shop. */
    public static Weapon createStarterWeapon(String characterClass) { return createForClass(characterClass, 1); }

    public static List<Weapon> createUpgradesForClass(String characterClass) {
        return List.of(createForClass(characterClass, 1), createForClass(characterClass, 2),
                createForClass(characterClass, 3), createForClass(characterClass, 4), createForClass(characterClass, 5));
    }

    /** Full shop assortment: every class' five weapon tiers. Ownership is still class-restricted. */
    public static List<Weapon> createAllClassWeapons() {
        java.util.List<Weapon> weapons = new java.util.ArrayList<>();
        for (String characterClass : List.of("KNIGHT", "ARCHER", "MAGE", "ASSASSIN")) {
            weapons.addAll(createUpgradesForClass(characterClass));
        }
        return List.copyOf(weapons);
    }

    private static Weapon createForClass(String characterClass, int level) {
        String value = characterClass == null ? "" : characterClass.toUpperCase(java.util.Locale.ROOT);
        if (value.contains("ARCHER")) return new BasicWeapon(
                new String[]{"Recruit Bow", "Light Bow", "Hunter Bow", "Long Bow", "Dark Bow"}[level-1],
                "A bow upgrade for the archer.", new int[]{0,120,220,350,500}[level-1], "ARCHER",
                new int[]{10,12,18,21,26}[level-1], new double[]{1,1.35,.8,1,1.1}[level-1], Math.min(4,level), level);
        if (value.contains("MAGE")) return new BasicWeapon(
                new String[]{"Starter Staff", "Arcane Staff", "Dark Staff", "Holy Staff", "Nature Staff"}[level-1],
                "A staff upgrade for the mage.", new int[]{0,120,220,350,500}[level-1], "MAGE",
                new int[]{10,12,18,21,26}[level-1], new double[]{1,1.35,.8,1,1.1}[level-1], Math.min(4,level), level);
        if (value.contains("ASSASSIN")) return new BasicWeapon(
                new String[]{"Starter Daggers", "Curved Daggers", "Royal Daggers", "Shadow Daggers", "Venom Daggers"}[level-1],
                "A dagger upgrade for the assassin.", new int[]{0,120,220,350,500}[level-1], "ASSASSIN",
                new int[]{10,12,18,21,26}[level-1], new double[]{1,1.35,.8,1,1.1}[level-1], Math.min(4,level), level);
        return createByLevel(level);
    }
}
