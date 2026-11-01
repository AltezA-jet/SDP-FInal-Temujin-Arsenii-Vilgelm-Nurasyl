package com.gamelab.domain.item;

import com.gamelab.domain.Item;

public interface Weapon extends Item {

    int getDamageBonus();

    double getAttackSpeed();

    int getRuneSlotCount();

    int getUpgradeLevel();
}