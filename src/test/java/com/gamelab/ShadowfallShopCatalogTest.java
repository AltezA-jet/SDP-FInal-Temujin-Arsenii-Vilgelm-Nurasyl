package com.gamelab;

import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.Potion;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneType;
import com.gamelab.domain.shop.PurchaseResult;
import com.gamelab.domain.shop.ShadowfallShopCatalog;
import com.gamelab.domain.shop.Shop;
import com.gamelab.pattern.observer.GameEvent;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.observer.GameEventTypes;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShadowfallShopCatalogTest {

    @Test
    void defaultShopShouldContainSixRunesAndPotion() {
        Shop shop =
                ShadowfallShopCatalog.createDefaultShop(
                        new GameEventBus()
                );

        long runeCount = shop.getCatalog()
                .stream()
                .filter(Rune.class::isInstance)
                .count();

        long potionCount = shop.getCatalog()
                .stream()
                .filter(Potion.class::isInstance)
                .count();

        long specialRuneCount = shop.getCatalog()
                .stream()
                .filter(Rune.class::isInstance)
                .map(Rune.class::cast)
                .filter(Rune::isSpecial)
                .count();

        assertEquals(7, shop.getCatalogSize());
        assertEquals(6, runeCount);
        assertEquals(1, potionCount);
        assertEquals(0, specialRuneCount);
    }

    @Test
    void shouldPurchaseCommonRune() {
        GameEventBus eventBus = new GameEventBus();
        List<GameEvent> events = new ArrayList<>();
        eventBus.subscribe(events::add);

        Shop shop =
                ShadowfallShopCatalog.createDefaultShop(
                        eventBus
                );

        Rune poisonRune = shop.getCatalog()
                .stream()
                .filter(Rune.class::isInstance)
                .map(Rune.class::cast)
                .filter(rune ->
                        rune.getRuneType()
                                == RuneType.POISON
                )
                .findFirst()
                .orElseThrow();

        CoinWallet wallet = new CoinWallet(200);
        Inventory inventory = new Inventory(10);

        PurchaseResult result = shop.purchase(
                poisonRune,
                "KNIGHT",
                wallet,
                inventory
        );

        assertTrue(result.success());
        assertEquals(110, wallet.getBalance());
        assertTrue(
                inventory.getItems()
                        .contains(poisonRune)
        );

        assertEquals(1, events.size());
        assertEquals(
                GameEventTypes.ITEM_PURCHASED,
                events.get(0).type()
        );
    }

    @Test
    void specialRunesShouldNotBeSoldInShop() {
        Shop shop =
                ShadowfallShopCatalog.createDefaultShop(
                        new GameEventBus()
                );

        boolean containsSpecialRune =
                shop.getCatalog()
                        .stream()
                        .filter(Rune.class::isInstance)
                        .map(Rune.class::cast)
                        .anyMatch(Rune::isSpecial);

        assertFalse(containsSpecialRune);
    }
}