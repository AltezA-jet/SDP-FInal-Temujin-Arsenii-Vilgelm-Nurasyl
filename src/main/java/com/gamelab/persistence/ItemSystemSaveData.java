package com.gamelab.persistence;

import com.gamelab.domain.rune.RuneType;

import java.util.List;
import java.util.Objects;

public record ItemSystemSaveData(
        int inventoryCapacity,
        int weaponLevel,
        List<RuneType> installedRunes,
        List<RuneType> inventoryRunes,
        int healthPotionCount
) {

    public ItemSystemSaveData {
        if (inventoryCapacity <= 0) {
            throw new IllegalArgumentException(
                    "Inventory capacity must be positive"
            );
        }

        if (weaponLevel < 1
                || weaponLevel > 5) {
            throw new IllegalArgumentException(
                    "Weapon level must be from 1 to 5"
            );
        }

        if (healthPotionCount < 0) {
            throw new IllegalArgumentException(
                    "Potion count cannot be negative"
            );
        }

        installedRunes = List.copyOf(
                Objects.requireNonNull(
                        installedRunes,
                        "Installed runes cannot be null"
                )
        );

        inventoryRunes = List.copyOf(
                Objects.requireNonNull(
                        inventoryRunes,
                        "Inventory runes cannot be null"
                )
        );
    }
}