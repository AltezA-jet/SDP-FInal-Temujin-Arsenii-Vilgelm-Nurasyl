package com.gamelab;

import com.gamelab.application.MinibossRuneRewardService;
import com.gamelab.application.RuneActionResult;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneType;
import com.gamelab.pattern.observer.GameEvent;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.observer.GameEventTypes;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MinibossRuneRewardServiceTest {

    @Test
    void shouldGrantCorrectRuneForEveryStage() {
        GameEventBus eventBus = new GameEventBus();
        List<GameEvent> events = new ArrayList<>();
        eventBus.subscribe(events::add);

        MinibossRuneRewardService service =
                new MinibossRuneRewardService(
                        eventBus
                );

        Inventory inventory = new Inventory(5);

        RuneType[] expectedTypes = {
                RuneType.FIRE,
                RuneType.ICE,
                RuneType.PURITY,
                RuneType.HOLY,
                RuneType.ANCIENT
        };

        for (int stage = 1; stage <= 5; stage++) {
            RuneActionResult result =
                    service.grantStageRune(
                            stage,
                            inventory
                    );

            assertTrue(result.success());

            Rune receivedRune =
                    (Rune) inventory.getItems()
                            .get(stage - 1);

            assertEquals(
                    expectedTypes[stage - 1],
                    receivedRune.getRuneType()
            );
        }

        assertEquals(5, inventory.getSize());
        assertEquals(5, events.size());

        assertEquals(
                GameEventTypes.RUNE_REWARDED,
                events.get(0).type()
        );
    }

    @Test
    void shouldNotLoseRewardWhenInventoryIsFull() {
        GameEventBus eventBus = new GameEventBus();

        MinibossRuneRewardService service =
                new MinibossRuneRewardService(
                        eventBus
                );

        Inventory inventory = new Inventory(1);

        service.grantStageRune(
                1,
                inventory
        );

        RuneActionResult result =
                service.grantStageRune(
                        2,
                        inventory
                );

        assertFalse(result.success());
        assertEquals(1, inventory.getSize());
        assertTrue(
                result.message().contains(
                        "waiting"
                )
        );
    }

    @Test
    void shouldRejectInvalidStageNumber() {
        MinibossRuneRewardService service =
                new MinibossRuneRewardService(
                        new GameEventBus()
                );

        Inventory inventory = new Inventory(5);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.grantStageRune(
                        6,
                        inventory
                )
        );
    }
}