
package com.gamelab;

import com.gamelab.domain.PlayerCharacter;
import com.gamelab.pattern.builder.CharacterBuilder;
import com.gamelab.pattern.strategy.BalancedStrategy;
import com.gamelab.pattern.strategy.HeavyAttackStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerCombatStrategyTest {

    @Test
    void balancedStrategyShouldUseBaseDamage() {
        PlayerCharacter player = CharacterBuilder
                .warrior("Shadow")
                .build();

        BalancedStrategy strategy = new BalancedStrategy();

        assertEquals(player.getDamage(), strategy.calculateDamage(player));
        assertEquals("Balanced", strategy.getStrategyName());
    }

    @Test
    void heavyAttackShouldDealMoreDamage() {
        PlayerCharacter player = CharacterBuilder
                .warrior("Shadow")
                .build();

        HeavyAttackStrategy strategy = new HeavyAttackStrategy();

        assertEquals(27, strategy.calculateDamage(player));
        assertTrue(
                strategy.calculateDamage(player) > player.getDamage()
        );
    }
}
