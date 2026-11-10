package com.gamelab.domain.item;

import com.gamelab.domain.Item;

/** Permanent archer equipment upgrade, applied by the campaign when combat starts. */
public record QuiverUpgrade(String name, String description, int price, int level,
                            int capacity, double refillSeconds) implements Item {
    public QuiverUpgrade {
        if (name == null || name.isBlank() || description == null || description.isBlank()
                || price < 0 || level < 1 || capacity < 10 || refillSeconds <= 0) {
            throw new IllegalArgumentException("Invalid quiver upgrade");
        }
    }
    @Override public String getName() { return name; }
    @Override public String getDescription() { return description; }
    @Override public int getPrice() { return price; }
    @Override public String getRequiredClass() { return "ARCHER"; }
    @Override public String getInventoryCategory() { return "QUIVER"; }
}
