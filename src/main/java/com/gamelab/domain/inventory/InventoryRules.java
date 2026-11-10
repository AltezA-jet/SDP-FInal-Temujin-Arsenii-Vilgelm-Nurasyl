package com.gamelab.domain.inventory;

import com.gamelab.domain.Item;
import java.util.Set;

/** Central configurable limits by hero class; numeric defaults remain easy to tune. */
public record InventoryRules(int maxItems, double maxWeight, Set<String> allowedCategories) {
    public InventoryRules {
        if (maxItems <= 0 || maxWeight < 0 || !Double.isFinite(maxWeight)) throw new IllegalArgumentException("Invalid inventory rules");
        allowedCategories = allowedCategories == null ? Set.of() : Set.copyOf(allowedCategories);
    }
    public boolean permits(Item item, String heroClass) {
        return classMatches(item.getRequiredClass(), heroClass)
                && (allowedCategories.isEmpty() || allowedCategories.contains(item.getInventoryCategory()));
    }
    private static boolean classMatches(String required, String owner) {
        if (required == null || owner == null) return false;
        return "ALL".equalsIgnoreCase(required) || normalize(required).equals(normalize(owner));
    }
    private static String normalize(String name) {
        return switch (name.replaceAll("[^A-Za-z]", "").toUpperCase()) {
            case "KNIGHT", "WARRIOR" -> "WARRIOR";
            case "ROGUE", "ASSASSIN" -> "ASSASSIN";
            default -> name.replaceAll("[^A-Za-z]", "").toUpperCase();
        };
    }
    public static InventoryRules defaults(String heroClass, int fallbackCapacity) {
        int capacity = "ASSASSIN".equalsIgnoreCase(heroClass) ? Math.max(1, fallbackCapacity / 2) : fallbackCapacity;
        return new InventoryRules(capacity, Double.MAX_VALUE, Set.of());
    }
}
