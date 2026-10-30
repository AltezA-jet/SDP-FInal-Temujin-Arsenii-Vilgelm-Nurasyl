package com.gamelab.domain.item;

public final class BasicWeapon implements Weapon {

    private final String name;
    private final String description;
    private final int price;
    private final String requiredClass;
    private final int damageBonus;

    public BasicWeapon(
            String name,
            String description,
            int price,
            String requiredClass,
            int damageBonus
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

        this.name = name;
        this.description = description;
        this.price = price;
        this.requiredClass = requiredClass;
        this.damageBonus = damageBonus;
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
    public String toString() {
        return name
                + " | damage: +" + damageBonus
                + " | price: " + price
                + " coins"
                + " | class: " + requiredClass;
    }
}