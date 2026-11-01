package com.gamelab.domain.item;

public final class BasicWeapon implements Weapon {

    private final String name;
    private final String description;
    private final int price;
    private final String requiredClass;
    private final int damageBonus;
    private final double attackSpeed;
    private final int runeSlotCount;
    private final int upgradeLevel;

    public BasicWeapon(
            String name,
            String description,
            int price,
            String requiredClass,
            int damageBonus
    ) {
        this(
                name,
                description,
                price,
                requiredClass,
                damageBonus,
                1.0,
                1,
                1
        );
    }

    public BasicWeapon(
            String name,
            String description,
            int price,
            String requiredClass,
            int damageBonus,
            double attackSpeed,
            int runeSlotCount,
            int upgradeLevel
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Weapon name cannot be empty"
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Weapon description cannot be empty"
            );
        }

        if (price < 0 || damageBonus < 0) {
            throw new IllegalArgumentException(
                    "Price and damage cannot be negative"
            );
        }

        if (requiredClass == null || requiredClass.isBlank()) {
            throw new IllegalArgumentException(
                    "Required class cannot be empty"
            );
        }

        if (!Double.isFinite(attackSpeed)
                || attackSpeed <= 0) {
            throw new IllegalArgumentException(
                    "Attack speed must be positive"
            );
        }

        if (runeSlotCount < 0 || runeSlotCount > 4) {
            throw new IllegalArgumentException(
                    "Rune slot count must be between 0 and 4"
            );
        }

        if (upgradeLevel < 1 || upgradeLevel > 5) {
            throw new IllegalArgumentException(
                    "Upgrade level must be between 1 and 5"
            );
        }

        this.name = name;
        this.description = description;
        this.price = price;
        this.requiredClass = requiredClass;
        this.damageBonus = damageBonus;
        this.attackSpeed = attackSpeed;
        this.runeSlotCount = runeSlotCount;
        this.upgradeLevel = upgradeLevel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public String getRequiredClass() {
        return requiredClass;
    }

    @Override
    public int getDamageBonus() {
        return damageBonus;
    }

    @Override
    public double getAttackSpeed() {
        return attackSpeed;
    }

    @Override
    public int getRuneSlotCount() {
        return runeSlotCount;
    }

    @Override
    public int getUpgradeLevel() {
        return upgradeLevel;
    }

    @Override
    public String toString() {
        return name
                + " | damage: +" + damageBonus
                + " | attack speed: " + attackSpeed
                + " | rune slots: " + runeSlotCount
                + " | level: " + upgradeLevel
                + " | price: " + price
                + " coins"
                + " | class: " + requiredClass;
    }
}