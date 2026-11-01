package com.gamelab;

import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneCatalog;
import com.gamelab.domain.rune.RuneCategory;
import com.gamelab.domain.rune.RuneType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuneTest {

    @Test
    void shouldCreateSpecialRune() {
        Rune fireRune = RuneCatalog.create(
                RuneType.FIRE
        );

        assertEquals(
                RuneType.FIRE,
                fireRune.getRuneType()
        );

        assertEquals(
                RuneCategory.SPECIAL,
                fireRune.getCategory()
        );

        assertTrue(fireRune.isSpecial());
        assertEquals(0, fireRune.getPrice());
        assertTrue(fireRune.canBeUsedBy("KNIGHT"));
    }

    @Test
    void shouldCreateShopRune() {
        Rune poisonRune = RuneCatalog.create(
                RuneType.POISON
        );

        assertEquals(
                RuneType.POISON,
                poisonRune.getRuneType()
        );

        assertEquals(
                RuneCategory.COMMON,
                poisonRune.getCategory()
        );

        assertFalse(poisonRune.isSpecial());
        assertEquals(90, poisonRune.getPrice());
    }

    @Test
    void shouldCreateAllRuneGroups() {
        assertEquals(
                5,
                RuneCatalog.createSpecialRunes().size()
        );

        assertEquals(
                6,
                RuneCatalog.createShopRunes().size()
        );
    }

    @Test
    void shouldRejectNegativeRunePrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Rune(
                        RuneType.FURY,
                        "Invalid Rune",
                        "Invalid negative price.",
                        -1
                )
        );
    }
}