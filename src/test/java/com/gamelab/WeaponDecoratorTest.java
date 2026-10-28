package com.gamelab;

import com.gamelab.domain.item.BasicWeapon;
import com.gamelab.domain.item.Weapon;
import com.gamelab.pattern.decorator.FireWeaponDecorator;
import com.gamelab.pattern.decorator.PoisonWeaponDecorator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeaponDecoratorTest {

    @Test
    void fireDecoratorShouldIncreaseDamageAndPrice() {
        Weapon sword = new BasicWeapon(
                "Iron Sword",
                "Basic knight weapon.",
                100,
                "KNIGHT",
                10
        );

        Weapon fireSword =
                new FireWeaponDecorator(sword);

        assertEquals(
                "Flaming Iron Sword",
                fireSword.getName()
        );

        assertEquals(
                15,
                fireSword.getDamageBonus()
        );

        assertEquals(
                150,
                fireSword.getPrice()
        );

        assertTrue(
                fireSword.getDescription()
                        .contains("fire")
        );

        assertTrue(
                fireSword.canBeUsedBy("KNIGHT")
        );

        assertFalse(
                fireSword.canBeUsedBy("MAGE")
        );
    }

    @Test
    void poisonDecoratorShouldIncreaseDamageAndPrice() {
        Weapon bow = new BasicWeapon(
                "Hunter Bow",
                "Basic archer weapon.",
                90,
                "ARCHER",
                8
        );

        Weapon poisonBow =
                new PoisonWeaponDecorator(bow);

        assertEquals(
                "Poisoned Hunter Bow",
                poisonBow.getName()
        );

        assertEquals(
                11,
                poisonBow.getDamageBonus()
        );

        assertEquals(
                130,
                poisonBow.getPrice()
        );

        assertTrue(
                poisonBow.getDescription()
                        .contains("poison")
        );
    }

    @Test
    void decoratorsShouldWorkTogether() {
        Weapon staff = new BasicWeapon(
                "Magic Staff",
                "Basic mage weapon.",
                120,
                "MAGE",
                12
        );

        Weapon upgradedStaff =
                new PoisonWeaponDecorator(
                        new FireWeaponDecorator(staff)
                );

        assertEquals(
                "Poisoned Flaming Magic Staff",
                upgradedStaff.getName()
        );

        assertEquals(
                20,
                upgradedStaff.getDamageBonus()
        );

        assertEquals(
                210,
                upgradedStaff.getPrice()
        );

        assertTrue(
                upgradedStaff.getDescription()
                        .contains("fire")
        );

        assertTrue(
                upgradedStaff.getDescription()
                        .contains("poison")
        );
    }
}