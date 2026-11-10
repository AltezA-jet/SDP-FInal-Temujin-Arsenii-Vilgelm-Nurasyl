package com.gamelab.domain.inventory;

import com.gamelab.domain.Item;

import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class Inventory {

    private final int capacity;
    private final List<Item> items;
    private final Set<RuneType> acquiredRuneTypes;
    private final Set<Integer> acquiredWeaponLevels;

    private Weapon equippedWeapon;


    public Inventory(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Inventory capacity must be positive"
            );
        }

        this.capacity = capacity;
        this.items = new ArrayList<>();
        this.acquiredRuneTypes = new HashSet<>();
        this.acquiredWeaponLevels = new HashSet<>();

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
        if (item instanceof Rune rune) {
            acquiredRuneTypes.add(rune.getRuneType());
        }
        if (item instanceof Weapon weapon) {
            acquiredWeaponLevels.add(weapon.getUpgradeLevel());
        }
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

    /** Remains true after a rune is installed and removed from the inventory grid. */
    public boolean hasAcquiredRuneType(RuneType runeType) {
        return acquiredRuneTypes.contains(
                Objects.requireNonNull(runeType, "Rune type cannot be null")
        );
    }

    /** Remains true even if the weapon is later equipped, replaced or removed. */
    public boolean hasAcquiredWeaponLevel(int upgradeLevel) {
        return acquiredWeaponLevels.contains(upgradeLevel);
    }
}
