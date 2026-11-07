package com.gamelab.pattern.strategy;

import com.gamelab.domain.Enemy;
import java.util.Objects;

/** Selects a distance-keeping strategy below 30% HP; bosses retain their normal behavior. */
public final class WoundedRetreatBehavior implements EnemyBehavior {
    private final EnemyBehavior normal;
    private final EnemyBehavior retreat;

    public WoundedRetreatBehavior(EnemyBehavior normal, double distance) {
        this.normal = Objects.requireNonNull(normal);
        retreat = new RangedBehavior(distance);
    }

    public boolean isRetreating(Enemy enemy) {
        return !enemy.isBoss() && enemy.isAlive() && enemy.getHealth() < enemy.getMaxHealth() * .3;
    }

    @Override public void update(Enemy enemy, double x, double y, double dt) {
        (isRetreating(enemy) ? retreat : normal).update(enemy, x, y, dt);
    }
}
