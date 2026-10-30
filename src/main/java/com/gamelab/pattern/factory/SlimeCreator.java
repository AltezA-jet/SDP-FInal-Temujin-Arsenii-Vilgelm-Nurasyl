package com.gamelab.pattern.factory;
import com.gamelab.domain.Enemy;
import com.gamelab.pattern.strategy.DefensiveBehavior;
public final class SlimeCreator extends EnemyCreator {
    @Override public Enemy createEnemy(String id, double x, double y) { return new Enemy(id, "Slime", 35, 7, x, y, 35, 5, 12, new DefensiveBehavior()); }
}
