package com.gamelab;

import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.BasicWeapon;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.shop.PurchaseResult;
import com.gamelab.domain.shop.Shop;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.observer.GameLogObserver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShopIntegrationTest {

    @Test
    void shouldPurchaseItemAndPublishEvent() {
        GameEventBus eventBus = new GameEventBus();
        GameLogObserver gameLog =
                new GameLogObserver();

        eventBus.subscribe(gameLog);

        Shop shop = new Shop(eventBus);
        CoinWallet wallet = new CoinWallet(150);
        Inventory inventory = new Inventory(5);

        Weapon sword = new BasicWeapon(
                "Iron Sword",
                "Basic knight weapon.",
                100,
                "KNIGHT",
                10
        );

        shop.addItem(sword);

        PurchaseResult result = shop.purchase(
                sword,
                "KNIGHT",
                wallet,
                inventory
        );

        assertTrue(result.success());
        assertEquals(50, wallet.getBalance());
        assertEquals(1, inventory.getSize());
        assertTrue(inventory.getItems().contains(sword));

        assertEquals(
                1,
                gameLog.getEntryCount()
        );

        assertTrue(
                gameLog.getEntries()
                        .get(0)
                        .contains("ITEM_PURCHASED")
        );
    }

    @Test
    void shouldRejectPurchaseWithoutEnoughCoins() {
        GameEventBus eventBus = new GameEventBus();
        GameLogObserver gameLog =
                new GameLogObserver();

        eventBus.subscribe(gameLog);

        Shop shop = new Shop(eventBus);
        CoinWallet wallet = new CoinWallet(50);
        Inventory inventory = new Inventory(5);

        Weapon sword = new BasicWeapon(
                "Iron Sword",
                "Basic knight weapon.",
                100,
                "KNIGHT",
                10
        );

        shop.addItem(sword);

        PurchaseResult result = shop.purchase(
                sword,
                "KNIGHT",
                wallet,
                inventory
        );

        assertFalse(result.success());
        assertEquals("Not enough coins", result.message());
        assertEquals(50, wallet.getBalance());
        assertTrue(inventory.isEmpty());
        assertEquals(0, gameLog.getEntryCount());
    }

    @Test
    void shouldRejectItemForWrongCharacterClass() {
        GameEventBus eventBus = new GameEventBus();
        Shop shop = new Shop(eventBus);

        CoinWallet wallet = new CoinWallet(500);
        Inventory inventory = new Inventory(5);

        Weapon bow = new BasicWeapon(
                "Hunter Bow",
                "Basic archer weapon.",
                90,
                "ARCHER",
                8
        );

        shop.addItem(bow);

        PurchaseResult result = shop.purchase(
                bow,
                "KNIGHT",
                wallet,
                inventory
        );

        assertFalse(result.success());
        assertEquals(500, wallet.getBalance());
        assertTrue(inventory.isEmpty());

        assertTrue(
                result.message()
                        .contains("cannot use")
        );
    }

    @Test
    void shouldRejectPurchaseWhenInventoryIsFull() {
        GameEventBus eventBus = new GameEventBus();
        Shop shop = new Shop(eventBus);

        CoinWallet wallet = new CoinWallet(500);
        Inventory inventory = new Inventory(1);

        Weapon sword = new BasicWeapon(
                "Iron Sword",
                "Basic knight weapon.",
                100,
                "KNIGHT",
                10
        );

        Weapon secondSword = new BasicWeapon(
                "Steel Sword",
                "Improved knight weapon.",
                150,
                "KNIGHT",
                15
        );

        inventory.addItem(sword);
        shop.addItem(secondSword);

        PurchaseResult result = shop.purchase(
                secondSword,
                "KNIGHT",
                wallet,
                inventory
        );

        assertFalse(result.success());
        assertEquals("Inventory is full", result.message());
        assertEquals(500, wallet.getBalance());
        assertEquals(1, inventory.getSize());
    }
}