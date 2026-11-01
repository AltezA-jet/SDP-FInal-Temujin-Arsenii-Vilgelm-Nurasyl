package com.gamelab.domain.item;

import com.gamelab.domain.Enemy;
import com.gamelab.domain.Item;

import java.util.Objects;

public interface Weapon extends Item {

    int getDamageBonus();

    double getAttackSpeed();

    int getRuneSlotCount();

    int getUpgradeLevel();

    default int getDamageBonusAgainst(
            Enemy enemy
    ) {
        Objects.requireNonNull(
                enemy,
                "Enemy cannot be null"
        );

        return getDamageBonus();
    }
}