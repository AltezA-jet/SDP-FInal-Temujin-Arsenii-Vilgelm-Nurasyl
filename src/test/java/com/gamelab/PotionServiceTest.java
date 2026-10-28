package com.gamelab;

import com.gamelab.application.PotionService;
import com.gamelab.application.PotionUseResult;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.item.Potion;
import com.gamelab.domain.item.PotionType;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.observer.GameLogObserver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PotionServiceTest {

    @Test
    void healthPotionShouldRestoreHealth() {
        GameEventBus eventBus = new GameEventBus();
        GameLogObserver gameLog =
                new GameLogObserver();

        eventBus.subscribe(gameLog);

        PotionService potionService =
                new PotionService(eventBus);

        PlayerCharacter player =
                new PlayerCharacter(
                        "Arthur",
                        "KNIGHT",
                        100,
                        20,
                        0
                );

        player.takeDamage(70);

        Potion healthPotion = new Potion(
                "Health Potion",
                "Restores 50 health points.",
                30,
                "ALL",
                PotionType.HEALTH,
                50
        );

        PotionUseResult result =
                potionService.usePotion(
                        healthPotion,
                        player
                );

        assertTrue(result.success());
        assertEquals(80, player.getHealth());

        assertEquals(
                1,
                gameLog.getEntryCount()
        );

        assertTrue(
                gameLog.getEntries()
                        .get(0)
                        .contains("POTION_USED")
        );
    }

    @Test
    void healthShouldNotExceedMaximum() {
        GameEventBus eventBus = new GameEventBus();

        PotionService potionService =
                new PotionService(eventBus);

        PlayerCharacter player =
                new PlayerCharacter(
                        "Arthur",
                        "KNIGHT",
                        100,
                        20,
                        0
                );

        player.takeDamage(20);

        Potion healthPotion = new Potion(
                "Large Health Potion",
                "Restores 100 health points.",
                60,
                "ALL",
                PotionType.HEALTH,
                100
        );

        potionService.usePotion(
                healthPotion,
                player
        );

        assertEquals(
                player.getMaxHealth(),
                player.getHealth()
        );
    }

    @Test
    void damagePotionShouldIncreaseDamage() {
        GameEventBus eventBus = new GameEventBus();

        PotionService potionService =
                new PotionService(eventBus);

        PlayerCharacter player =
                new PlayerCharacter(
                        "Robin",
                        "ARCHER",
                        80,
                        15,
                        2
                );

        Potion damagePotion = new Potion(
                "Damage Potion",
                "Temporarily increases damage.",
                50,
                "ALL",
                PotionType.DAMAGE_BOOST,
                5
        );

        PotionUseResult result =
                potionService.usePotion(
                        damagePotion,
                        player
                );

        assertTrue(result.success());
        assertEquals(20, player.getDamage());
    }

    @Test
    void wrongClassShouldNotUsePotion() {
        GameEventBus eventBus = new GameEventBus();

        PotionService potionService =
                new PotionService(eventBus);

        PlayerCharacter knight =
                new PlayerCharacter(
                        "Arthur",
                        "KNIGHT",
                        100,
                        20,
                        5
                );

        Potion manaPotion = new Potion(
                "Mana Potion",
                "Restores mana points.",
                35,
                "MAGE",
                PotionType.MANA,
                40
        );

        PotionUseResult result =
                potionService.usePotion(
                        manaPotion,
                        knight
                );

        assertFalse(result.success());

        assertTrue(
                result.message()
                        .contains("cannot use")
        );
    }

    @Test
    void manaPotionShouldWaitForManaIntegration() {
        GameEventBus eventBus = new GameEventBus();

        PotionService potionService =
                new PotionService(eventBus);

        PlayerCharacter mage =
                new PlayerCharacter(
                        "Merlin",
                        "MAGE",
                        80,
                        25,
                        2
                );

        Potion manaPotion = new Potion(
                "Mana Potion",
                "Restores mana points.",
                35,
                "MAGE",
                PotionType.MANA,
                40
        );

        PotionUseResult result =
                potionService.usePotion(
                        manaPotion,
                        mage
                );

        assertFalse(result.success());

        assertEquals(
                "Mana system is not connected yet",
                result.message()
        );
    }
}