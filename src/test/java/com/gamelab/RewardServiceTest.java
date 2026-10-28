package com.gamelab;

import com.gamelab.application.RewardService;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.Potion;
import com.gamelab.domain.item.PotionType;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.observer.GameLogObserver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RewardServiceTest {

    @Test
    void shouldGrantCoinsAndExperienceForEnemy() {
        GameEventBus eventBus = new GameEventBus();
        GameLogObserver gameLog =
                new GameLogObserver();

        eventBus.subscribe(gameLog);

        RewardService rewardService =
                new RewardService(eventBus);

        PlayerCharacter player =
                new PlayerCharacter(
                        "Arthur",
                        "KNIGHT",
                        100,
                        20,
                        5
                );

        CoinWallet wallet = new CoinWallet();

        rewardService.grantEnemyReward(
                "Skeleton",
                25,
                20,
                wallet,
                player
        );

        assertEquals(25, wallet.getBalance());
        assertEquals(20, player.getExperience());

        assertEquals(
                2,
                gameLog.getEntryCount()
        );

        assertTrue(
                gameLog.getEntries()
                        .get(0)
                        .contains("ENEMY_KILLED")
        );

        assertTrue(
                gameLog.getEntries()
                        .get(1)
                        .contains("REWARD_RECEIVED")
        );
    }

    @Test
    void shouldGrantBossReward() {
        GameEventBus eventBus = new GameEventBus();
        GameLogObserver gameLog =
                new GameLogObserver();

        eventBus.subscribe(gameLog);

        RewardService rewardService =
                new RewardService(eventBus);

        PlayerCharacter player =
                new PlayerCharacter(
                        "Merlin",
                        "MAGE",
                        80,
                        25,
                        2
                );

        CoinWallet wallet = new CoinWallet();

        rewardService.grantBossReward(
                "Shadow Knight",
                200,
                150,
                wallet,
                player
        );

        assertEquals(200, wallet.getBalance());

        assertEquals(
                2,
                player.getLevel()
        );

        assertEquals(
                50,
                player.getExperience()
        );

        assertTrue(
                gameLog.getEntries()
                        .get(0)
                        .contains("BOSS_DEFEATED")
        );
    }

    @Test
    void shouldPickupItemAndPublishEvent() {
        GameEventBus eventBus = new GameEventBus();
        GameLogObserver gameLog =
                new GameLogObserver();

        eventBus.subscribe(gameLog);

        RewardService rewardService =
                new RewardService(eventBus);

        Inventory inventory = new Inventory(5);

        Potion healthPotion = new Potion(
                "Health Potion",
                "Restores health.",
                30,
                "ALL",
                PotionType.HEALTH,
                50
        );

        boolean pickedUp =
                rewardService.pickupItem(
                        healthPotion,
                        inventory
                );

        assertTrue(pickedUp);
        assertEquals(1, inventory.getSize());

        assertTrue(
                gameLog.getEntries()
                        .get(0)
                        .contains("ITEM_PICKED_UP")
        );
    }

    @Test
    void shouldRejectPickupWhenInventoryIsFull() {
        GameEventBus eventBus = new GameEventBus();

        RewardService rewardService =
                new RewardService(eventBus);

        Inventory inventory = new Inventory(1);

        Potion firstPotion = new Potion(
                "Health Potion",
                "Restores health.",
                30,
                "ALL",
                PotionType.HEALTH,
                50
        );

        Potion secondPotion = new Potion(
                "Large Health Potion",
                "Restores more health.",
                60,
                "ALL",
                PotionType.HEALTH,
                100
        );

        inventory.addItem(firstPotion);

        boolean pickedUp =
                rewardService.pickupItem(
                        secondPotion,
                        inventory
                );

        assertFalse(pickedUp);
        assertEquals(1, inventory.getSize());
    }

    @Test
    void shouldPublishWaveCompletedEvent() {
        GameEventBus eventBus = new GameEventBus();
        GameLogObserver gameLog =
                new GameLogObserver();

        eventBus.subscribe(gameLog);

        RewardService rewardService =
                new RewardService(eventBus);

        rewardService.completeWave(5);

        assertEquals(
                1,
                gameLog.getEntryCount()
        );

        assertTrue(
                gameLog.getEntries()
                        .get(0)
                        .contains("Wave 5 completed")
        );
    }
}