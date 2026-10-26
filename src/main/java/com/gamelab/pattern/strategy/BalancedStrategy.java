
package com.gamelab.pattern.strategy;

import com.gamelab.domain.PlayerCharacter;

public class BalancedStrategy implements PlayerCombatStrategy {

    @Override
    public int calculateDamage(PlayerCharacter player) {
        return player.getDamage();
    }

    @Override
    public String getStrategyName() {
        return "Balanced";
    }
}
