package com.gamelab.domain.item;

import com.gamelab.domain.Item;

import java.util.Objects;

public final class Equipment implements Item {

    private final String name;
    private final String description;
    private final int price;
    private final String requiredClass;
    private final EquipmentType equipmentType;
    private final int defenseBonus;
    private final int healthBonus;

    public Equipment(
            String name,
            String description,
            int price,
            String requiredClass,
            EquipmentType equipmentType,
            int defenseBonus,
            int healthBonus
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Equipment name cannot be empty"
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Equipment description cannot be empty"
            );
        }

        if (price < 0 || defenseBonus < 0 || healthBonus < 0) {
            throw new IllegalArgumentException(
                    "Equipment values cannot be negative"
            );
        }

        if (defenseBonus == 0 && healthBonus == 0) {
            throw new IllegalArgumentException(
                    "Equipment must provide at least one bonus"
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
        this.equipmentType = Objects.requireNonNull(
                equipmentType,
                "Equipment type cannot be null"
        );
        this.defenseBonus = defenseBonus;
        this.healthBonus = healthBonus;
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

    public EquipmentType getEquipmentType() {
        return equipmentType;
    }

    public int getDefenseBonus() {
        return defenseBonus;
    }

    public int getHealthBonus() {
        return healthBonus;
    }

    @Override
    public String toString() {
        return name
                + " | type: " + equipmentType
                + " | defense: +" + defenseBonus
                + " | health: +" + healthBonus
                + " | price: " + price + " coins"
                + " | class: " + requiredClass;
    }
}