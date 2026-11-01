package com.gamelab;

import com.gamelab.application.WeaponUpgradeResult;
import com.gamelab.application.WeaponUpgradeService;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.item.WeaponCatalog;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneCatalog;
import com.gamelab.domain.rune.RuneLoadout;
import com.gamelab.domain.rune.RuneType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeaponUpgradeServiceTest {

    private final WeaponUpgradeService service =
            new WeaponUpgradeService();

    @Test
    void shouldUpgradeWeaponAndReturnRune() {
        Inventory inventory = new Inventory(10);
        CoinWallet wallet = new CoinWallet(500);

        Weapon starterSword =
                WeaponCatalog.createStarterSword();

        inventory.addItem(starterSword);
        inventory.equipWeapon(
                starterSword,
                "KNIGHT"
        );

        RuneLoadout loadout =
                new RuneLoadout(starterSword);

        Rune fireRune =
                RuneCatalog.create(RuneType.FIRE);

        loadout.installRune(fireRune);

        WeaponUpgradeResult result =
                service.upgrade(
                        loadout,
                        inventory,
                        wallet,
                        "KNIGHT"
                );

        assertTrue(result.success());
        assertEquals(380, wallet.getBalance());

        assertEquals(
                "Swift Blade",
                result.runeLoadout()
                        .getWeapon()
                        .getName()
        );

        assertEquals(
                2,
                result.runeLoadout()
                        .getWeapon()
                        .getUpgradeLevel()
        );

        assertTrue(
                result.runeLoadout()
                        .getInstalledRunes()
                        .isEmpty()
        );

        assertTrue(
                inventory.getItems()
                        .contains(fireRune)
        );

        assertFalse(
                inventory.getItems()
                        .contains(starterSword)
        );

        assertEquals(
                "Swift Blade",
                inventory.getEquippedWeapon()
                        .orElseThrow()
                        .getName()
        );
    }

    @Test
    void shouldRejectUpgradeWithoutEnoughCoins() {
        Inventory inventory = new Inventory(5);
        CoinWallet wallet = new CoinWallet(100);

        Weapon starterSword =
                WeaponCatalog.createStarterSword();

        inventory.addItem(starterSword);
        inventory.equipWeapon(
                starterSword,
                "KNIGHT"
        );

        RuneLoadout loadout =
                new RuneLoadout(starterSword);

        WeaponUpgradeResult result =
                service.upgrade(
                        loadout,
                        inventory,
                        wallet,
                        "KNIGHT"
                );

        assertFalse(result.success());
        assertEquals(100, wallet.getBalance());
        assertSame(loadout, result.runeLoadout());

        assertSame(
                starterSword,
                inventory.getEquippedWeapon()
                        .orElseThrow()
        );
    }

    @Test
    void shouldRejectUpgradeAtMaximumLevel() {
        Inventory inventory = new Inventory(5);
        CoinWallet wallet = new CoinWallet(1000);

        Weapon ancientBlade =
                WeaponCatalog.createAncientBlade();

        inventory.addItem(ancientBlade);
        inventory.equipWeapon(
                ancientBlade,
                "KNIGHT"
        );

        RuneLoadout loadout =
                new RuneLoadout(ancientBlade);

        WeaponUpgradeResult result =
                service.upgrade(
                        loadout,
                        inventory,
                        wallet,
                        "KNIGHT"
                );

        assertFalse(result.success());
        assertSame(loadout, result.runeLoadout());
        assertEquals(1000, wallet.getBalance());
    }

    @Test
    void shouldRejectUpgradeWithoutInventorySpace() {
        Inventory inventory = new Inventory(2);
        CoinWallet wallet = new CoinWallet(1000);

        Weapon swiftBlade =
                WeaponCatalog.createSwiftBlade();

        Weapon additionalWeapon =
                WeaponCatalog.createStarterSword();

        inventory.addItem(swiftBlade);
        inventory.addItem(additionalWeapon);

        inventory.equipWeapon(
                swiftBlade,
                "KNIGHT"
        );

        RuneLoadout loadout =
                new RuneLoadout(swiftBlade);

        loadout.installRune(
                RuneCatalog.create(RuneType.FIRE)
        );

        loadout.installRune(
                RuneCatalog.create(RuneType.HASTE)
        );

        WeaponUpgradeResult result =
                service.upgrade(
                        loadout,
                        inventory,
                        wallet,
                        "KNIGHT"
                );

        assertFalse(result.success());
        assertEquals(1000, wallet.getBalance());

        assertEquals(
                2,
                loadout.getInstalledRunes().size()
        );

        assertSame(
                swiftBlade,
                inventory.getEquippedWeapon()
                        .orElseThrow()
        );
    }
}