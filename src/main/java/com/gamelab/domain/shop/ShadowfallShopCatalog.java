package com.gamelab.domain.shop;

import com.gamelab.domain.item.Potion;
import com.gamelab.domain.item.PotionType;
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
        Objects.requireNonNull(
                eventBus,
                "Event bus cannot be null"
        );

        Shop shop = new Shop(eventBus);

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