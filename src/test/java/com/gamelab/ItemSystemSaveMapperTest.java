package com.gamelab;

import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.Potion;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.item.WeaponCatalog;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneCatalog;
import com.gamelab.domain.rune.RuneLoadout;
import com.gamelab.domain.rune.RuneType;
import com.gamelab.domain.shop.ShadowfallShopCatalog;
import com.gamelab.persistence.ItemSystemSaveData;
import com.gamelab.persistence.ItemSystemSaveMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemSystemSaveMapperTest {

    @Test
    void shouldCaptureAndRestoreItemSystem() {
        Inventory inventory =
                new Inventory(10);

        Weapon weapon =
                WeaponCatalog.createByLevel(3);

        inventory.addItem(weapon);
        inventory.equipWeapon(
                weapon,
                "KNIGHT"
        );

        RuneLoadout loadout =
                new RuneLoadout(weapon);

        loadout.installRune(
                RuneCatalog.create(RuneType.FIRE)
        );

        loadout.installRune(
                RuneCatalog.create(RuneType.HASTE)
        );

        inventory.addItem(
                RuneCatalog.create(RuneType.POISON)
        );

        inventory.addItem(
                RuneCatalog.create(RuneType.CRITICAL)
        );

        inventory.addItem(
                ShadowfallShopCatalog
                        .createHealthPotion()
        );

        inventory.addItem(
                ShadowfallShopCatalog
                        .createHealthPotion()
        );

        ItemSystemSaveData saveData =
                ItemSystemSaveMapper.capture(
                        inventory,
                        loadout
                );

        assertEquals(
                3,
                saveData.weaponLevel()
        );

        assertEquals(
                List.of(
                        RuneType.FIRE,
                        RuneType.HASTE
                ),
                saveData.installedRunes()
        );

        assertEquals(
                List.of(
                        RuneType.POISON,
                        RuneType.CRITICAL
                ),
                saveData.inventoryRunes()
        );

        assertEquals(
                2,
                saveData.healthPotionCount()
        );

        ItemSystemSaveMapper.RestoredItemSystem
                restored =
                ItemSystemSaveMapper.restore(
                        saveData,
                        "KNIGHT"
                );

        assertEquals(
                10,
                restored.inventory()
                        .getCapacity()
        );

        assertEquals(
                3,
                restored.loadout()
                        .getWeapon()
                        .getUpgradeLevel()
        );

        assertSame(
                restored.loadout().getWeapon(),
                restored.inventory()
                        .getEquippedWeapon()
                        .orElseThrow()
        );

        assertEquals(
                2,
                restored.loadout()
                        .getInstalledRunes()
                        .size()
        );

        long storedRuneCount =
                restored.inventory()
                        .getItems()
                        .stream()
                        .filter(Rune.class::isInstance)
                        .count();

        long potionCount =
                restored.inventory()
                        .getItems()
                        .stream()
                        .filter(Potion.class::isInstance)
                        .count();

        assertEquals(2, storedRuneCount);
        assertEquals(2, potionCount);
    }
}