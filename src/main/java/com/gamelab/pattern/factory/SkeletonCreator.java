package com.gamelab.pattern.factory;
import com.gamelab.domain.Enemy;
import com.gamelab.pattern.strategy.ChaseBehavior;
public final class SkeletonCreator extends EnemyCreator {
    @Override public Enemy createEnemy(String id, double x, double y) { return new Enemy(id, "Skeleton", 55, 12, x, y, 75, 8, 20, new ChaseBehavior()); }
}
