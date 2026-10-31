package com.gamelab;

import com.gamelab.domain.Enemy;
import com.gamelab.pattern.strategy.ChaseBehavior;
import com.gamelab.pattern.strategy.DefensiveBehavior;
import com.gamelab.pattern.strategy.RangedBehavior;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnemyBehaviorTest {
    private Enemy enemy() { return new Enemy("e1", "Test", 50, 5, 0, 0, 10, 1, 1, new ChaseBehavior()); }
    @Test void chaseMovesTowardsPlayer() { Enemy e=enemy(); e.update(100, 0, 1); assertEquals(10, e.getX(), 0.001); }
    @Test void defensiveBehaviorHoldsPosition() { Enemy e=enemy(); e.setBehavior(new DefensiveBehavior()); e.update(100, 0, 1); assertEquals(0, e.getX(), 0.001); }
    @Test void rangedBehaviorRetreatsWhenPlayerIsTooClose() { Enemy e=enemy(); e.setBehavior(new RangedBehavior(100)); e.update(10, 0, 1); assertEquals(-10, e.getX(), 0.001); }
    @Test void deadEnemyDoesNotMove() { Enemy e=enemy(); e.takeDamage(50); e.update(100, 0, 1); assertEquals(0, e.getX(), 0.001); }
    @Test void rejectsNegativeDamage() { assertThrows(IllegalArgumentException.class, () -> enemy().takeDamage(-1)); }
}
