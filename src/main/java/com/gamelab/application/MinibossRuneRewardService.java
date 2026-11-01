package com.gamelab.application;

import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneCatalog;
import com.gamelab.domain.rune.RuneType;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.observer.GameEventTypes;

import java.util.Objects;

public final class MinibossRuneRewardService {

    private final GameEventBus eventBus;

    public MinibossRuneRewardService(
            GameEventBus eventBus
    ) {
        this.eventBus = Objects.requireNonNull(
                eventBus,
                "Event bus cannot be null"
        );
    }

    public RuneActionResult grantStageRune(
            int completedStage,
            Inventory inventory
    ) {
        Objects.requireNonNull(
                inventory,
                "Inventory cannot be null"
        );

        RuneType runeType =
                getRewardRuneType(completedStage);

        if (inventory.isFull()) {
            return RuneActionResult.failed(
                    "Inventory is full. "
                            + "Rune reward is waiting"
            );
        }

        Rune rune = RuneCatalog.create(runeType);

        inventory.addItem(rune);

        eventBus.publish(
                GameEventTypes.RUNE_REWARDED,
                rune.getName()
                        + " received for completing stage "
                        + completedStage
        );

        return RuneActionResult.successful(
                rune.getName() + " received"
        );
    }

    private RuneType getRewardRuneType(
            int completedStage
    ) {
        return switch (completedStage) {
            case 1 -> RuneType.FIRE;
            case 2 -> RuneType.ICE;
            case 3 -> RuneType.PURITY;
            case 4 -> RuneType.HOLY;
            case 5 -> RuneType.ANCIENT;

            default -> throw new IllegalArgumentException(
                    "Completed stage must be from 1 to 5"
            );
        };
    }
}