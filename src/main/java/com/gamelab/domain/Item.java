package com.gamelab.domain;

public interface Item {

    String getName();

    String getDescription();

    int getPrice();

    String getRequiredClass();

    default boolean canBeUsedBy(String characterClass) {
        if (characterClass == null || characterClass.isBlank()) {
            return false;
        }

        return "ALL".equalsIgnoreCase(getRequiredClass())
                || getRequiredClass().equalsIgnoreCase(characterClass);
    }
}