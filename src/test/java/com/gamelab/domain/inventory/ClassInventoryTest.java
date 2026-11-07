package com.gamelab.domain.inventory;

import com.gamelab.domain.item.BasicWeapon;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClassInventoryTest {
    @Test void fixedSlotsRulesOwnerAndRemovalAreIndependent() {
        InventoryRules warriorRules = new InventoryRules(2, 100, null);
        InventoryRules assassinRules = new InventoryRules(1, 10, null);
        Inventory warrior = new Inventory("WARRIOR", warriorRules);
        Inventory assassin = new Inventory("ASSASSIN", assassinRules);
        BasicWeapon allowed = new BasicWeapon("Blade", "Blade", 0, "ALL", 1);
        BasicWeapon second = new BasicWeapon("Charm", "Charm", 0, "ALL", 1);
        BasicWeapon forbidden = new BasicWeapon("Bow", "Bow", 0, "ARCHER", 1);
        assertTrue(warrior.tryAddItem(allowed).isEmpty());
        assertNotNull(warrior.getSlots()[0]);
        assertTrue(warrior.tryAddItem(second).isEmpty());
        assertTrue(warrior.tryAddItem(forbidden).isPresent());
        assertTrue(assassin.tryAddItem(allowed).isEmpty());
        assertTrue(assassin.tryAddItem(second).isPresent());
        assertTrue(warrior.removeItem(allowed));
        assertNull(warrior.getSlots()[0]);
        assertEquals(1, assassin.getSize());
        assertTrue(assassin.getItems().contains(allowed));
    }
}
