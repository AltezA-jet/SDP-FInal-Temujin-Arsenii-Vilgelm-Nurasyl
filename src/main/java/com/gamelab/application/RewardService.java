package com.gamelab.application;

import com.gamelab.domain.Item;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.observer.GameEventTypes;

import java.util.Objects;

public final class RewardService {

    private final GameEventBus eventBus;

    public RewardService(GameEventBus eventBus) {
        this.eventBus = Objects.requireNonNull(
                eventBus,
                "Event bus cannot be null"
        );
    }

    public void grantEnemyReward(
            String enemyName,
            int coins,
            int experience,
            CoinWallet wallet,
            PlayerCharacter player
    ) {
        validateReward(
                enemyName,
                coins,
                experience,
                wallet,
                player
        );

        eventBus.publish(
                GameEventTypes.ENEMY_KILLED,
                enemyName + " was defeated"
        );

        applyReward(
                coins,
                experience,
                wallet,
                player
        );
    }

    public void grantBossReward(
            String bossName,
            int coins,
            int experience,
            CoinWallet wallet,
            PlayerCharacter player
    ) {
        validateReward(
                bossName,
                coins,
                experience,
                wallet,
                player
        );

        eventBus.publish(
                GameEventTypes.BOSS_DEFEATED,
                bossName + " was defeated"
        );

        applyReward(
                coins,
                experience,
                wallet,
                player
        );
    }

    private void applyReward(
            int coins,
            int experience,
            CoinWallet wallet,
            PlayerCharacter player
    ) {
        wallet.addCoins(coins);
        player.gainExperience(experience);

        eventBus.publish(
                GameEventTypes.REWARD_RECEIVED,
                "Received "
                        + coins
                        + " coins and "
                        + experience
                        + " experience"
        );
    }

    public boolean pickupItem(
            Item item,
            Inventory inventory
    ) {
        Objects.requireNonNull(
                item,
                "Picked item cannot be null"
        );

        Objects.requireNonNull(
                inventory,
                "Inventory cannot be null"
        );

        if (inventory.isFull()) {
            return false;
        }

        inventory.addItem(item);

        eventBus.publish(
                GameEventTypes.ITEM_PICKED_UP,
                item.getName() + " was picked up"
        );

        return true;
    }

    public void completeWave(int waveNumber) {
        if (waveNumber <= 0) {
            throw new IllegalArgumentException(
                    "Wave number must be positive"
            );
        }

        eventBus.publish(
                GameEventTypes.WAVE_COMPLETED,
                "Wave " + waveNumber + " completed"
        );
    }

    private void validateReward(
            String enemyName,
            int coins,
            int experience,
            CoinWallet wallet,
            PlayerCharacter player
    ) {
        if (enemyName == null || enemyName.isBlank()) {
            throw new IllegalArgumentException(
                    "Enemy name cannot be empty"
            );
        }

        if (coins <= 0 || experience <= 0) {
            throw new IllegalArgumentException(
                    "Reward values must be positive"
            );
        }

        Objects.requireNonNull(
                wallet,
                "Wallet cannot be null"
        );

        Objects.requireNonNull(
                player,
                "Player cannot be null"
        );
    }
}