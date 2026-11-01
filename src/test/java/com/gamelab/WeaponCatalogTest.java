package com.gamelab;

import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.item.WeaponCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeaponCatalogTest {

    @Test
    void shouldCreateAllWeaponLevels() {
        List<Weapon> weapons =
                WeaponCatalog.createAll();

        assertEquals(5, weapons.size());

        assertEquals(
                "Starter Sword",
                weapons.get(0).getName()
        );

        assertEquals(
                "Ancient Blade",
                weapons.get(4).getName()
        );

        assertEquals(
                1,
                weapons.get(0).getUpgradeLevel()
        );

        assertEquals(
                5,
                weapons.get(4).getUpgradeLevel()
        );
    }

    @Test
    void shouldIncreaseRuneSlotsWithUpgrades() {
        assertEquals(
                1,
                WeaponCatalog
                        .createStarterSword()
                        .getRuneSlotCount()
        );

        assertEquals(
                2,
                WeaponCatalog
                        .createSwiftBlade()
                        .getRuneSlotCount()
        );

        assertEquals(
                2,
                WeaponCatalog
                        .createGreatsword()
                        .getRuneSlotCount()
        );

        assertEquals(
                3,
                WeaponCatalog
                        .createDarksteelSword()
                        .getRuneSlotCount()
        );

        assertEquals(
                4,
                WeaponCatalog
                        .createAncientBlade()
                        .getRuneSlotCount()
        );
    }

    @Test
    void shouldUseDifferentAttackSpeeds() {
        assertEquals(
                1.35,
                WeaponCatalog
                        .createSwiftBlade()
                        .getAttackSpeed(),
                0.001
        );

        assertEquals(
                0.80,
                WeaponCatalog
                        .createGreatsword()
                        .getAttackSpeed(),
                0.001
        );
    }

    @Test
    void shouldRejectInvalidWeaponLevel() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WeaponCatalog.createByLevel(6)
        );
    }
}