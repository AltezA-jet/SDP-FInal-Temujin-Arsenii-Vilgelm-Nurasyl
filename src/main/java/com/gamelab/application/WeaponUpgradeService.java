package com.gamelab.application;

import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.item.WeaponCatalog;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneLoadout;

import java.util.List;
import java.util.Objects;

public final class WeaponUpgradeService {

    public WeaponUpgradeResult upgrade(
            RuneLoadout currentLoadout,
            Inventory inventory,
            CoinWallet wallet,
            String characterClass
    ) {
        Objects.requireNonNull(
                currentLoadout,
                "Current loadout cannot be null"
        );

        Objects.requireNonNull(
                inventory,
                "Inventory cannot be null"
        );

        Objects.requireNonNull(
                wallet,
                "Wallet cannot be null"
        );

        Weapon currentWeapon =
                currentLoadout.getWeapon();

        if (currentWeapon.getUpgradeLevel() >= 5) {
            return WeaponUpgradeResult.failed(
                    "The weapon already has the maximum level.",
                    currentLoadout
            );
        }

        if (!inventory.getItems()
                .contains(currentWeapon)) {
            return WeaponUpgradeResult.failed(
                    "The current weapon is not in the inventory.",
                    currentLoadout
            );
        }

        if (inventory.getEquippedWeapon()
                .orElse(null) != currentWeapon) {
            return WeaponUpgradeResult.failed(
                    "The current weapon is not equipped.",
                    currentLoadout
            );
        }

        Weapon nextWeapon =
                WeaponCatalog.createByLevel(
                        currentWeapon.getUpgradeLevel() + 1
                );

        if (!nextWeapon.canBeUsedBy(characterClass)) {
            return WeaponUpgradeResult.failed(
                    "This character cannot use the next weapon.",
                    currentLoadout
            );
        }

        int runeCount =
                currentLoadout
                        .getInstalledRunes()
                        .size();

        if (inventory.getRemainingSlots()
                < runeCount) {
            return WeaponUpgradeResult.failed(
                    "Not enough inventory space for removed runes.",
                    currentLoadout
            );
        }

        if (!wallet.canAfford(
                nextWeapon.getPrice()
        )) {
            return WeaponUpgradeResult.failed(
                    "Not enough coins for the weapon upgrade.",
                    currentLoadout
            );
        }

        wallet.spendCoins(
                nextWeapon.getPrice()
        );

        List<Rune> removedRunes =
                currentLoadout.removeAllRunes();

        inventory.removeItem(currentWeapon);

        for (Rune rune : removedRunes) {
            inventory.addItem(rune);
        }

        inventory.addItem(nextWeapon);

        inventory.equipWeapon(
                nextWeapon,
                characterClass
        );

        RuneLoadout upgradedLoadout =
                new RuneLoadout(nextWeapon);

        return WeaponUpgradeResult.successful(
                currentWeapon.getName()
                        + " was upgraded to "
                        + nextWeapon.getName()
                        + ".",
                upgradedLoadout
        );
    }
}