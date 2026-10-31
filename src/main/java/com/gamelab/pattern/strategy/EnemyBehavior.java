package com.gamelab.pattern.strategy;
import com.gamelab.domain.Enemy;
/** Strategy for choosing an enemy's movement during one game tick. */
@FunctionalInterface
public interface EnemyBehavior {
    void update(Enemy enemy, double playerX, double playerY, double deltaSeconds);
}
