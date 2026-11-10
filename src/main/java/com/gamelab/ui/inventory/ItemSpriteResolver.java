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
        String name = weapon.getName().toLowerCase(Locale.ROOT);
        if (name.contains("bow")) return "/assets/All/Inventory_shop_items/Bow/" + switch (weapon.getUpgradeLevel()) {
            case 1 -> "Bow_Start.png";
            case 2 -> "Bow_Light.png";
            case 3 -> "Bow_Hunter.png";
            case 4 -> "Bow_Long.png";
            case 5 -> "Bow_Dark.png";
            default -> throw new IllegalArgumentException("Unknown bow level");
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
