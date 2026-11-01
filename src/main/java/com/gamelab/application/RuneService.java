package com.gamelab.application;

import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneLoadout;
import com.gamelab.domain.rune.RuneType;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.observer.GameEventTypes;

import java.util.Objects;

public final class RuneService {

    private final GameEventBus eventBus;

    public RuneService(GameEventBus eventBus) {
        this.eventBus = Objects.requireNonNull(
                eventBus,
                "Event bus cannot be null"
        );
    }

    public RuneActionResult installRune(
            Rune rune,
            RuneLoadout loadout,
            Inventory inventory
    ) {
        Objects.requireNonNull(rune, "Rune cannot be null");
        Objects.requireNonNull(loadout, "Rune loadout cannot be null");
        Objects.requireNonNull(inventory, "Inventory cannot be null");

        if (!inventory.getItems().contains(rune)) {
            return RuneActionResult.failed(
                    "Rune is not in the inventory"
            );
        }

        try {
            loadout.installRune(rune);
        } catch (IllegalArgumentException
                 | IllegalStateException exception) {
            return RuneActionResult.failed(
                    exception.getMessage()
            );
        }

        inventory.removeItem(rune);

        eventBus.publish(
                GameEventTypes.RUNE_INSTALLED,
                rune.getName()
                        + " installed on "
                        + loadout.getWeapon().getName()
        );

        return RuneActionResult.successful(
                rune.getName() + " installed"
        );
    }

    public RuneActionResult removeRune(
            RuneType runeType,
            RuneLoadout loadout,
            Inventory inventory
    ) {
        Objects.requireNonNull(
                runeType,
                "Rune type cannot be null"
        );
        Objects.requireNonNull(
                loadout,
                "Rune loadout cannot be null"
        );
        Objects.requireNonNull(
                inventory,
                "Inventory cannot be null"
        );

        if (inventory.isFull()) {
            return RuneActionResult.failed(
                    "Inventory is full"
            );
        }

        try {
            Rune removedRune =
                    loadout.removeRune(runeType);

            inventory.addItem(removedRune);

            eventBus.publish(
                    GameEventTypes.RUNE_REMOVED,
                    removedRune.getName()
                            + " removed from "
                            + loadout.getWeapon().getName()
            );

            return RuneActionResult.successful(
                    removedRune.getName() + " removed"
            );
        } catch (IllegalArgumentException
                 | IllegalStateException exception) {
            return RuneActionResult.failed(
                    exception.getMessage()
            );
        }
    }
}