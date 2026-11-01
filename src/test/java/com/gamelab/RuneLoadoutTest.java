package com.gamelab;

import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.item.WeaponCatalog;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneCatalog;
import com.gamelab.domain.rune.RuneLoadout;
import com.gamelab.domain.rune.RuneType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuneLoadoutTest {

    @Test
    void shouldInstallRunesIntoAvailableSlots() {
        Weapon weapon =
                WeaponCatalog.createSwiftBlade();

        RuneLoadout loadout =
                new RuneLoadout(weapon);

        loadout.installRune(
                RuneCatalog.create(RuneType.FIRE)
        );

        loadout.installRune(
                RuneCatalog.create(RuneType.LIGHTNING)
        );

        assertEquals(
                2,
                loadout.getInstalledRunes().size()
        );

        assertEquals(
                0,
                loadout.getAvailableSlotCount()
        );
    }

    @Test
    void shouldRejectRuneWhenSlotsAreFull() {
        RuneLoadout loadout = new RuneLoadout(
                WeaponCatalog.createStarterSword()
        );

        loadout.installRune(
                RuneCatalog.create(RuneType.FURY)
        );

        assertThrows(
                IllegalStateException.class,
                () -> loadout.installRune(
                        RuneCatalog.create(
                                RuneType.CRITICAL
                        )
                )
        );
    }

    @Test
    void shouldRejectDuplicateRune() {
        RuneLoadout loadout = new RuneLoadout(
                WeaponCatalog.createSwiftBlade()
        );

        loadout.installRune(
                RuneCatalog.create(RuneType.FURY)
        );

        assertThrows(
                IllegalStateException.class,
                () -> loadout.installRune(
                        RuneCatalog.create(
                                RuneType.FURY
                        )
                )
        );
    }

    @Test
    void shouldAllowOnlyOneSpecialRune() {
        RuneLoadout loadout = new RuneLoadout(
                WeaponCatalog.createDarksteelSword()
        );

        loadout.installRune(
                RuneCatalog.create(RuneType.FIRE)
        );

        assertThrows(
                IllegalStateException.class,
                () -> loadout.installRune(
                        RuneCatalog.create(
                                RuneType.ICE
                        )
                )
        );
    }

    @Test
    void shouldRejectIncompatibleRunes() {
        RuneLoadout purityLoadout =
                new RuneLoadout(
                        WeaponCatalog
                                .createDarksteelSword()
                );

        purityLoadout.installRune(
                RuneCatalog.create(RuneType.PURITY)
        );

        assertThrows(
                IllegalStateException.class,
                () -> purityLoadout.installRune(
                        RuneCatalog.create(
                                RuneType.POISON
                        )
                )
        );

        RuneLoadout holyPoisonLoadout =
                new RuneLoadout(
                        WeaponCatalog
                                .createDarksteelSword()
                );

        holyPoisonLoadout.installRune(
                RuneCatalog.create(RuneType.HOLY)
        );

        assertThrows(
                IllegalStateException.class,
                () -> holyPoisonLoadout.installRune(
                        RuneCatalog.create(
                                RuneType.POISON
                        )
                )
        );

        RuneLoadout holyVampireLoadout =
                new RuneLoadout(
                        WeaponCatalog
                                .createDarksteelSword()
                );

        holyVampireLoadout.installRune(
                RuneCatalog.create(RuneType.HOLY)
        );

        assertThrows(
                IllegalStateException.class,
                () -> holyVampireLoadout.installRune(
                        RuneCatalog.create(
                                RuneType.VAMPIRE
                        )
                )
        );
    }

    @Test
    void shouldRemoveRuneAndReturnIt() {
        RuneLoadout loadout = new RuneLoadout(
                WeaponCatalog.createSwiftBlade()
        );

        loadout.installRune(
                RuneCatalog.create(RuneType.FURY)
        );

        Rune removedRune =
                loadout.removeRune(RuneType.FURY);

        assertEquals(
                RuneType.FURY,
                removedRune.getRuneType()
        );

        assertTrue(
                loadout.getInstalledRunes().isEmpty()
        );

        assertEquals(
                2,
                loadout.getAvailableSlotCount()
        );
    }

    @Test
    void shouldRemoveAllRunesBeforeUpgrade() {
        RuneLoadout loadout = new RuneLoadout(
                WeaponCatalog.createSwiftBlade()
        );

        loadout.installRune(
                RuneCatalog.create(RuneType.FIRE)
        );

        loadout.installRune(
                RuneCatalog.create(RuneType.HASTE)
        );

        assertEquals(
                2,
                loadout.removeAllRunes().size()
        );

        assertTrue(
                loadout.getInstalledRunes().isEmpty()
        );
    }
}