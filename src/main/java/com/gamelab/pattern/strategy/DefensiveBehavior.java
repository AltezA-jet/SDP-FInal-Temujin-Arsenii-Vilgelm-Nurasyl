package com.gamelab.pattern.strategy;
import com.gamelab.domain.Enemy;
/** Holds position instead of pursuing the player. */
public final class DefensiveBehavior implements EnemyBehavior {
    @Override public void update(Enemy enemy, double playerX, double playerY, double deltaSeconds) { /* Hold position. */ }
}
