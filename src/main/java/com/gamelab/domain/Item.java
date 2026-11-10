package com.gamelab.domain;

public interface Item {

    String getName();

    String getDescription();

    int getPrice();

    String getRequiredClass();

    default double getWeight() { return 0.0; }

    default String getInventoryCategory() { return "GENERAL"; }

    default boolean canBeUsedBy(String characterClass) {
        if (characterClass == null || characterClass.isBlank()) {
            return false;
        }

        return "ALL".equalsIgnoreCase(getRequiredClass())
                || normalizeClass(getRequiredClass()).equals(normalizeClass(characterClass));
    }

    /** Keeps Russian UI class names compatible with catalog identifiers. */
    private static String normalizeClass(String value) {
        String normalized = value.trim().toUpperCase(java.util.Locale.ROOT);
        return switch (normalized) {
            case "ВОИН", "WARRIOR", "KNIGHT" -> "KNIGHT";
            case "ЛУЧНИК", "ARCHER" -> "ARCHER";
            case "МАГ", "MAGE" -> "MAGE";
            case "АССАСИН", "ASSASSIN" -> "ASSASSIN";
            default -> normalized;
        };
    }
}
