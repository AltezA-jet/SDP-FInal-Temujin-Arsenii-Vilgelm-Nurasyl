package com.gamelab.domain.shop;

import com.gamelab.domain.item.Potion;
import com.gamelab.domain.item.PotionType;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.item.WeaponCatalog;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneCatalog;
import com.gamelab.pattern.observer.GameEventBus;

import java.util.Objects;

public final class ShadowfallShopCatalog {

    private ShadowfallShopCatalog() {
    }

    public static Shop createDefaultShop(
            GameEventBus eventBus
    ) {
        return createShop(eventBus, false, "");
    }

    public static Shop createDefaultShop(GameEventBus eventBus, String characterClass) {
        return createShop(eventBus, true, characterClass);
    }

    private static Shop createShop(GameEventBus eventBus, boolean includeWeapons, String characterClass) {
        Objects.requireNonNull(
                eventBus,
                "Event bus cannot be null"
        );

        Shop shop = new Shop(eventBus);

        if (includeWeapons) for (Weapon weapon : WeaponCatalog.createAllClassWeapons()) shop.addItem(weapon);
        if (characterClass != null && characterClass.toUpperCase(java.util.Locale.ROOT).contains("ARCHER")) {
            shop.addItem(new com.gamelab.domain.item.QuiverUpgrade("Reinforced Quiver", "Holds 15 arrows; one arrow returns every 1.5 seconds.", 140, 1, 15, 1.5));
            shop.addItem(new com.gamelab.domain.item.QuiverUpgrade("Master Quiver", "Holds 20 arrows; one arrow returns every second.", 320, 2, 20, 1.0));
        }

        for (Rune rune
                : RuneCatalog.createShopRunes()) {
            shop.addItem(rune);
        }

        shop.addItem(createHealthPotion());

        return shop;
    }

    public static Potion createHealthPotion() {
        return new Potion(
                "Health Potion",
                "Restores 50 health points.",
                30,
                "ALL",
                PotionType.HEALTH,
                50
        );
    }
}
