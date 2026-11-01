package com.gamelab.persistence;

import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.Potion;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.item.WeaponCatalog;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneCatalog;
import com.gamelab.domain.rune.RuneLoadout;
import com.gamelab.domain.rune.RuneType;
import com.gamelab.domain.shop.ShadowfallShopCatalog;

import java.util.List;
import java.util.Objects;

public final class ItemSystemSaveMapper {

    private ItemSystemSaveMapper() {
    }

    public static ItemSystemSaveData capture(
            Inventory inventory,
            RuneLoadout loadout
    ) {
        Objects.requireNonNull(
                inventory,
                "Inventory cannot be null"
        );

        Objects.requireNonNull(
                loadout,
                "Rune loadout cannot be null"
        );

        List<RuneType> installedRunes =
                loadout.getInstalledRunes()
                        .stream()
                        .map(Rune::getRuneType)
                        .toList();

        List<RuneType> inventoryRunes =
                inventory.getItems()
                        .stream()
                        .filter(Rune.class::isInstance)
                        .map(Rune.class::cast)
                        .map(Rune::getRuneType)
                        .toList();

        int healthPotionCount =
                (int) inventory.getItems()
                        .stream()
                        .filter(Potion.class::isInstance)
                        .count();

        return new ItemSystemSaveData(
                inventory.getCapacity(),
                loadout.getWeapon()
                        .getUpgradeLevel(),
                installedRunes,
                inventoryRunes,
                healthPotionCount
        );
    }

    public static RestoredItemSystem restore(
            ItemSystemSaveData saveData,
            String characterClass
    ) {
        Objects.requireNonNull(
                saveData,
                "Save data cannot be null"
        );

        if (characterClass == null
                || characterClass.isBlank()) {
            throw new IllegalArgumentException(
                    "Character class cannot be empty"
            );
        }

        Inventory inventory =
                new Inventory(
                        saveData.inventoryCapacity()
                );

        Weapon weapon =
                WeaponCatalog.createByLevel(
                        saveData.weaponLevel()
                );

        inventory.addItem(weapon);

        inventory.equipWeapon(
                weapon,
                characterClass
        );

        RuneLoadout loadout =
                new RuneLoadout(weapon);

        for (RuneType runeType
                : saveData.installedRunes()) {
            loadout.installRune(
                    RuneCatalog.create(runeType)
            );
        }

        for (RuneType runeType
                : saveData.inventoryRunes()) {
            inventory.addItem(
                    RuneCatalog.create(runeType)
            );
        }

        for (int count = 0;
             count < saveData.healthPotionCount();
             count++) {
            inventory.addItem(
                    ShadowfallShopCatalog
                            .createHealthPotion()
            );
        }

        return new RestoredItemSystem(
                inventory,
                loadout
        );
    }

    public record RestoredItemSystem(
            Inventory inventory,
            RuneLoadout loadout
    ) {

        public RestoredItemSystem {
            Objects.requireNonNull(
                    inventory,
                    "Inventory cannot be null"
            );

            Objects.requireNonNull(
                    loadout,
                    "Rune loadout cannot be null"
            );
        }
    }
}