package com.gamelab.domain.inventory;

import com.gamelab.domain.Item;

import com.gamelab.domain.item.Weapon;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class Inventory {

    private final int capacity;
    private final List<Item> items;

    private Weapon equippedWeapon;


    public Inventory(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Inventory capacity must be positive"
            );
        }

        this.capacity = capacity;
        this.items = new ArrayList<>();

    }

    public void addItem(Item item) {
        Objects.requireNonNull(
                item,
                "Item cannot be null"
        );

        if (isFull()) {
            throw new IllegalStateException(
                    "Inventory is full"
            );
        }

        items.add(item);
    }

    public boolean removeItem(Item item) {
        if (item == null) {
            return false;
        }

        boolean removed = items.remove(item);

        if (!removed) {
            return false;
        }

        if (equippedWeapon == item) {
            equippedWeapon = null;
        }



        return true;
    }

    public void equipWeapon(
            Weapon weapon,
            String characterClass
    ) {
        Objects.requireNonNull(
                weapon,
                "Weapon cannot be null"
        );

        ensureItemExists(weapon);

        if (!weapon.canBeUsedBy(characterClass)) {
            throw new IllegalArgumentException(
                    "This character cannot use "
                            + weapon.getName()
            );
        }

        equippedWeapon = weapon;
    }



    private void ensureItemExists(Item item) {
        if (!items.contains(item)) {
            throw new IllegalStateException(
                    "Item is not in the inventory"
            );
        }
    }

    public Optional<Weapon> getEquippedWeapon() {
        return Optional.ofNullable(equippedWeapon);
    }



    public int getWeaponDamageBonus() {
        if (equippedWeapon == null) {
            return 0;
        }

        return equippedWeapon.getDamageBonus();
    }





    public List<Item> getItems() {
        return List.copyOf(items);
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSize() {
        return items.size();
    }

    public int getRemainingSlots() {
        return capacity - items.size();
    }

    public boolean isFull() {
        return items.size() >= capacity;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}