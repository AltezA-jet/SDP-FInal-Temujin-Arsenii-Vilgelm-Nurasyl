package com.gamelab.pattern.strategy;
import com.gamelab.domain.Enemy;
/** Moves directly towards the player. */
public final class ChaseBehavior implements EnemyBehavior {
    @Override public void update(Enemy enemy, double playerX, double playerY, double deltaSeconds) { enemy.moveTowards(playerX, playerY, deltaSeconds); }
}
