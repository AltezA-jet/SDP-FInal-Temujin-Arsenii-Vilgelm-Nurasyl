
package com.gamelab.pattern.strategy;

import com.gamelab.domain.PlayerCharacter;

public class HeavyAttackStrategy implements PlayerCombatStrategy {

    @Override
    public int calculateDamage(PlayerCharacter player) {
        return (int) Math.round(player.getDamage() * 1.5);
    }

    @Override
    public String getStrategyName() {
        return "Heavy Attack";
    }
}
