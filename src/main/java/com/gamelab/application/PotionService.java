package com.gamelab.application;

import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.item.Potion;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.observer.GameEventTypes;

import java.util.Objects;

public final class PotionService {

    private final GameEventBus eventBus;

    public PotionService(GameEventBus eventBus) {
        this.eventBus = Objects.requireNonNull(
                eventBus,
                "Event bus cannot be null"
        );
    }

    public PotionUseResult usePotion(
            Potion potion,
            PlayerCharacter player
    ) {
        Objects.requireNonNull(
                potion,
                "Potion cannot be null"
        );

        Objects.requireNonNull(
                player,
                "Player cannot be null"
        );

        if (!potion.canBeUsedBy(
                player.getCharacterClass()
        )) {
            return PotionUseResult.failed(
                    player.getCharacterClass()
                            + " cannot use "
                            + potion.getName()
            );
        }

        switch (potion.getPotionType()) {
            case HEALTH ->
                    player.heal(
                            potion.getEffectAmount()
                    );

            case DAMAGE_BOOST ->
                    player.increaseDamage(
                            potion.getEffectAmount()
                    );

            case DEFENSE_BOOST ->
                    player.increaseDefense(
                            potion.getEffectAmount()
                    );

            case MANA -> {
                return PotionUseResult.failed(
                        "Mana system is not connected yet"
                );
            }
        }

        String message =
                player.getName()
                        + " used "
                        + potion.getName();

        eventBus.publish(
                GameEventTypes.POTION_USED,
                message
        );

        return PotionUseResult.successful(message);
    }
}