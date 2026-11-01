package com.gamelab.domain.rune;

import com.gamelab.domain.Item;

import java.util.Objects;

public final class Rune implements Item {

    private final RuneType runeType;
    private final String name;
    private final String description;
    private final int price;

    public Rune(
            RuneType runeType,
            String name,
            String description,
            int price
    ) {
        this.runeType = Objects.requireNonNull(
                runeType,
                "Rune type cannot be null"
        );

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Rune name cannot be empty"
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Rune description cannot be empty"
            );
        }

        if (price < 0) {
            throw new IllegalArgumentException(
                    "Rune price cannot be negative"
            );
        }

        this.name = name;
        this.description = description;
        this.price = price;
    }

    public RuneType getRuneType() {
        return runeType;
    }

    public RuneCategory getCategory() {
        return runeType.getCategory();
    }

    public boolean isSpecial() {
        return runeType.isSpecial();
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
        return "ALL";
    }

    @Override
    public String toString() {
        return name
                + " | type: " + runeType
                + " | category: " + getCategory()
                + " | price: " + price
                + " coins";
    }
}