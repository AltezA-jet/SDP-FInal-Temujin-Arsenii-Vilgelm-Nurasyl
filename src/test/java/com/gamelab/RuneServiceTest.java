package com.gamelab;

import com.gamelab.application.RuneActionResult;
import com.gamelab.application.RuneService;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.item.WeaponCatalog;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneCatalog;
import com.gamelab.domain.rune.RuneLoadout;
import com.gamelab.domain.rune.RuneType;
import com.gamelab.pattern.observer.GameEvent;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.observer.GameEventTypes;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RuneServiceTest {

    @Test
    void shouldInstallRuneAndPublishEvent() {
        GameEventBus eventBus = new GameEventBus();
        List<GameEvent> events = new ArrayList<>();
        eventBus.subscribe(events::add);

        RuneService service = new RuneService(eventBus);
        Inventory inventory = new Inventory(5);

        Weapon weapon =
                WeaponCatalog.createByLevel(1);

        Rune rune =
                RuneCatalog.create(RuneType.FIRE);

        RuneLoadout loadout =
                new RuneLoadout(weapon);

        inventory.addItem(rune);

        RuneActionResult result =
                service.installRune(
                        rune,
                        loadout,
                        inventory
                );

        assertTrue(result.success());
        assertFalse(inventory.getItems().contains(rune));
        assertTrue(
                loadout.getInstalledRunes().contains(rune)
        );

        assertEquals(1, events.size());
        assertEquals(
                GameEventTypes.RUNE_INSTALLED,
                events.get(0).type()
        );
    }

    @Test
    void shouldRemoveRuneAndReturnItToInventory() {
        GameEventBus eventBus = new GameEventBus();
        List<GameEvent> events = new ArrayList<>();
        eventBus.subscribe(events::add);

        RuneService service = new RuneService(eventBus);
        Inventory inventory = new Inventory(5);

        Rune rune =
                RuneCatalog.create(RuneType.POISON);

        RuneLoadout loadout =
                new RuneLoadout(
                        WeaponCatalog.createByLevel(2)
                );

        inventory.addItem(rune);

        service.installRune(
                rune,
                loadout,
                inventory
        );

        RuneActionResult result =
                service.removeRune(
                        RuneType.POISON,
                        loadout,
                        inventory
                );

        assertTrue(result.success());
        assertTrue(inventory.getItems().contains(rune));
        assertTrue(loadout.getInstalledRunes().isEmpty());

        assertEquals(2, events.size());
        assertEquals(
                GameEventTypes.RUNE_REMOVED,
                events.get(1).type()
        );
    }

    @Test
    void shouldRejectRuneMissingFromInventory() {
        GameEventBus eventBus = new GameEventBus();
        RuneService service = new RuneService(eventBus);

        Inventory inventory = new Inventory(5);

        Rune rune =
                RuneCatalog.create(RuneType.ICE);

        RuneLoadout loadout =
                new RuneLoadout(
                        WeaponCatalog.createByLevel(1)
                );

        RuneActionResult result =
                service.installRune(
                        rune,
                        loadout,
                        inventory
                );

        assertFalse(result.success());
        assertTrue(loadout.getInstalledRunes().isEmpty());
    }

    @Test
    void shouldKeepRuneInstalledWhenInventoryIsFull() {
        GameEventBus eventBus = new GameEventBus();
        RuneService service = new RuneService(eventBus);

        Inventory inventory = new Inventory(1);

        Rune fireRune =
                RuneCatalog.create(RuneType.FIRE);

        RuneLoadout loadout =
                new RuneLoadout(
                        WeaponCatalog.createByLevel(1)
                );

        inventory.addItem(fireRune);

        service.installRune(
                fireRune,
                loadout,
                inventory
        );

        inventory.addItem(
                RuneCatalog.create(RuneType.HASTE)
        );

        RuneActionResult result =
                service.removeRune(
                        RuneType.FIRE,
                        loadout,
                        inventory
                );

        assertFalse(result.success());
        assertTrue(
                loadout.getInstalledRunes()
                        .contains(fireRune)
        );
    }
}