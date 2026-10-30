package com.gamelab.pattern.factory;
import com.gamelab.domain.Enemy;
import com.gamelab.pattern.strategy.RangedBehavior;
public final class BossCreator extends EnemyCreator {
    @Override public Enemy createEnemy(String id, double x, double y) { return new Enemy(id, "Shadow Boss", 300, 28, x, y, 55, 100, 250, new RangedBehavior(180)); }
}
