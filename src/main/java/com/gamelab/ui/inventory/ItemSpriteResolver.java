package com.gamelab.ui.inventory;

import com.gamelab.domain.Item;
import com.gamelab.domain.item.Potion;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.item.QuiverUpgrade;
import com.gamelab.domain.rune.Rune;

import java.util.Locale;
import java.util.Objects;

public final class ItemSpriteResolver {

    private static final String ROOT =
            "/assets/items/";

    private ItemSpriteResolver() {
    }

    public static String resolve(Item item) {
        Objects.requireNonNull(
                item,
                "Item cannot be null"
        );

        if (item instanceof Weapon weapon) {
            return resolveWeapon(weapon);
        }

        if (item instanceof Rune rune) {
            return ROOT
                    + "runes/"
                    + rune.getRuneType()
                    .name()
                    .toLowerCase(Locale.ROOT)
                    + "_rune.png";
        }

        if (item instanceof Potion) {
            return ROOT
                    + "consumables/"
                    + "health_potion.png";
        }

        // A distinct quiver image is optional in the current art pack; use the
        // equipment placeholder so market and inventory rendering never fails.
        if (item instanceof QuiverUpgrade) return ROOT + "weapons/starter_sword.png";

        throw new IllegalArgumentException(
                "No sprite registered for "
                        + item.getClass().getSimpleName()
        );
    }

    public static String coin() {
        return ROOT + "currency/coin.png";
    }

    private static String resolveWeapon(
            Weapon weapon
    ) {
        String weaponClass = weapon.getRequiredClass().toUpperCase(Locale.ROOT);
        int level = weapon.getUpgradeLevel();
        if (weaponClass.equals("ARCHER")) return "/assets/All/Inventory_shop_items/Bow/" + switch (level) {
            case 1 -> "Bow_Start.png"; case 2 -> "Bow_Light.png"; case 3 -> "Bow_Hunter.png"; case 4 -> "Bow_Long.png"; case 5 -> "Bow_Dark.png"; default -> throw new IllegalArgumentException("Unknown bow level");
        };
        if (weaponClass.equals("MAGE")) return "/assets/All/Inventory_shop_items/Staff/" + switch (level) {
            case 1 -> "Staff_Start.png"; case 2 -> "Staff_Arcane.png"; case 3 -> "Staff_Dark.png"; case 4 -> "Staff_Holy.png"; case 5 -> "Staff_Nature.png"; default -> throw new IllegalArgumentException("Unknown staff level");
        };
        if (weaponClass.equals("ASSASSIN")) return "/assets/All/Inventory_shop_items/Dager/" + switch (level) {
            case 1 -> "Dagger_Start.png"; case 2 -> "Dagger_Curved.png"; case 3 -> "Dagger_Royal.png"; case 4 -> "Dagger_Shadow.png"; case 5 -> "Dagger_Venom.png"; default -> throw new IllegalArgumentException("Unknown dagger level");
        };
        if (weaponClass.equals("KNIGHT")) return "/assets/All/Inventory_shop_items/Sword/" + switch (level) {
            case 1 -> "starter_sword.png"; case 2 -> "swift_blade.png"; case 3 -> "greatsword.png"; case 4 -> "darksteel_sword.png"; case 5 -> "Ancient_Blade.png"; default -> throw new IllegalArgumentException("Unknown sword level");
        };
        String fileName = switch (
                weapon.getUpgradeLevel()
                ) {
            case 1 -> "starter_sword.png";
            case 2 -> "swift_blade.png";
            case 3 -> "greatsword.png";
            case 4 -> "darksteel_sword.png";
            case 5 -> "ancient_blade.png";

            default -> throw new IllegalArgumentException(
                    "Unknown weapon level: "
                            + weapon.getUpgradeLevel()
            );
        };

        return ROOT
                + "weapons/"
                + fileName;
    }
}
