package com.gamelab;

import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.BasicWeapon;
import com.gamelab.domain.item.Weapon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    @Test
    void shouldAddItemsToInventory() {
        Inventory inventory = new Inventory(5);

        Weapon sword = new BasicWeapon(
                "Iron Sword",
                "Basic knight weapon.",
                100,
                "KNIGHT",
                10
        );

        inventory.addItem(sword);

        assertEquals(1, inventory.getSize());
        assertEquals(4, inventory.getRemainingSlots());
        assertFalse(inventory.isEmpty());
        assertTrue(inventory.getItems().contains(sword));
    }

    @Test
    void shouldEquipWeaponForCorrectClass() {
        Inventory inventory = new Inventory(5);

        Weapon sword = new BasicWeapon(
                "Iron Sword",
                "Basic knight weapon.",
                100,
                "KNIGHT",
                10
        );

        inventory.addItem(sword);
        inventory.equipWeapon(sword, "KNIGHT");

        assertTrue(
                inventory.getEquippedWeapon().isPresent()
        );

        assertEquals(
                sword,
                inventory.getEquippedWeapon().get()
        );

        assertEquals(
                10,
                inventory.getWeaponDamageBonus()
        );
    }

    @Test
    void shouldRejectWeaponForWrongClass() {
        Inventory inventory = new Inventory(5);

        Weapon bow = new BasicWeapon(
                "Hunter Bow",
                "Basic archer weapon.",
                90,
                "ARCHER",
                8
        );

        inventory.addItem(bow);

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.equipWeapon(
                        bow,
                        "KNIGHT"
                )
        );
    }



    @Test
    void shouldRejectItemWhenInventoryIsFull() {
        Inventory inventory = new Inventory(1);

        Weapon sword = new BasicWeapon(
                "Iron Sword",
                "Basic knight weapon.",
                100,
                "KNIGHT",
                10
        );

        Weapon bow = new BasicWeapon(
                "Hunter Bow",
                "Basic archer weapon.",
                90,
                "ARCHER",
                8
        );

        inventory.addItem(sword);

        assertTrue(inventory.isFull());

        assertThrows(
                IllegalStateException.class,
                () -> inventory.addItem(bow)
        );
    }

    @Test
    void removingEquippedWeaponShouldUnequipIt() {
        Inventory inventory = new Inventory(5);

        Weapon sword = new BasicWeapon(
                "Iron Sword",
                "Basic knight weapon.",
                100,
                "KNIGHT",
                10
        );

        inventory.addItem(sword);
        inventory.equipWeapon(sword, "KNIGHT");
        inventory.removeItem(sword);

        assertTrue(
                inventory.getEquippedWeapon().isEmpty()
        );

        assertEquals(
                0,
                inventory.getWeaponDamageBonus()
        );
    }
}