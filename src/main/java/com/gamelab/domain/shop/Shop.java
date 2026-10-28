package com.gamelab.domain.shop;

import com.gamelab.domain.Item;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
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

        wallet.spendCoins(item.getPrice());
        inventory.addItem(item);

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

    public List<Item> getCatalog() {
        return List.copyOf(catalog);
    }

    public int getCatalogSize() {
        return catalog.size();
    }
}