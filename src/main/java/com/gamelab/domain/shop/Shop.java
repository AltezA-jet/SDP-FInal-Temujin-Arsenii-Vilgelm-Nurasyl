package com.gamelab.domain.shop;

import com.gamelab.domain.Item;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.Potion;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.rune.Rune;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.observer.GameEventTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Shop {

    private final List<Item> catalog;
    private final GameEventBus eventBus;

    public Shop(GameEventBus eventBus) {
        this.eventBus = Objects.requireNonNull(
                eventBus,
                "Event bus cannot be null"
        );

        this.catalog = new ArrayList<>();
    }

    public void addItem(Item item) {
        Objects.requireNonNull(
                item,
                "Shop item cannot be null"
        );

        catalog.add(item);
    }

    public PurchaseResult purchase(
            Item item,
            String characterClass,
            CoinWallet wallet,
            Inventory inventory
    ) {
        Objects.requireNonNull(
                item,
                "Purchased item cannot be null"
        );

        Objects.requireNonNull(
                wallet,
                "Wallet cannot be null"
        );

        Objects.requireNonNull(
                inventory,
                "Inventory cannot be null"
        );

        if (!catalog.contains(item)) {
            return PurchaseResult.failed(
                    "Item is not available in the shop"
            );
        }

        if (!item.canBeUsedBy(characterClass)) {
            return PurchaseResult.failed(
                    characterClass
                            + " cannot use "
                            + item.getName()
            );
        }

        if (item instanceof Rune rune
                && inventory.hasAcquiredRuneType(rune.getRuneType())) {
            return PurchaseResult.failed(
                    "A rune of this type is already in the inventory"
            );
        }

        if (item instanceof Weapon weapon
                && inventory.hasAcquiredWeaponLevel(weapon.getUpgradeLevel())) {
            return PurchaseResult.failed(
                    "A weapon of this level has already been purchased"
            );
        }

        if (inventory.isFull()) {
            return PurchaseResult.failed(
                    "Inventory is full"
            );
        }

        if (!wallet.canAfford(item.getPrice())) {
            return PurchaseResult.failed(
                    "Not enough coins"
            );
        }

        Item purchasedItem = createPurchasedItem(item);

        wallet.spendCoins(item.getPrice());
        inventory.addItem(purchasedItem);

        String message =
                item.getName()
                        + " purchased for "
                        + item.getPrice()
                        + " coins";

        eventBus.publish(
                GameEventTypes.ITEM_PURCHASED,
                message
        );

        return PurchaseResult.successful(message);
    }

    /** Consumables are templates in the shop catalog, so every purchase gets its own item instance. */
    private Item createPurchasedItem(Item catalogItem) {
        if (catalogItem instanceof Potion potion) {
            return new Potion(
                    potion.getName(),
                    potion.getDescription(),
                    potion.getPrice(),
                    potion.getRequiredClass(),
                    potion.getPotionType(),
                    potion.getEffectAmount()
            );
        }

        return catalogItem;
    }

    public List<Item> getCatalog() {
        return List.copyOf(catalog);
    }

    public int getCatalogSize() {
        return catalog.size();
    }
}
