package com.gamelab.domain.item;

import com.gamelab.domain.Item;

import java.util.Objects;

public final class Potion implements Item {

    private final String name;
    private final String description;
    private final int price;
    private final String requiredClass;
    private final PotionType potionType;
    private final int effectAmount;

    public Potion(
            String name,
            String description,
            int price,
            String requiredClass,
            PotionType potionType,
            int effectAmount
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Potion name cannot be empty"
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Potion description cannot be empty"
            );
        }

        if (price < 0 || effectAmount <= 0) {
            throw new IllegalArgumentException(
                    "Invalid potion price or effect amount"
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
        this.potionType = Objects.requireNonNull(
                potionType,
                "Potion type cannot be null"
        );
        this.effectAmount = effectAmount;
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

    public PotionType getPotionType() {
        return potionType;
    }

    public int getEffectAmount() {
        return effectAmount;
    }

    @Override
    public String toString() {
        return name
                + " | type: " + potionType
                + " | effect: +" + effectAmount
                + " | price: " + price + " coins"
                + " | class: " + requiredClass;
    }
}