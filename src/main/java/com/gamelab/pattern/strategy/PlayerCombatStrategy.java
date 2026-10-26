
package com.gamelab.pattern.strategy;

import com.gamelab.domain.PlayerCharacter;

public interface PlayerCombatStrategy {
    int calculateDamage(PlayerCharacter player);
    String getStrategyName();
}
