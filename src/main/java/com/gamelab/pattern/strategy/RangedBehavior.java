package com.gamelab.pattern.strategy;
import com.gamelab.domain.Enemy;
/** Keeps a preferred distance: retreats when too close and advances when too far. */
public final class RangedBehavior implements EnemyBehavior {
    private final double preferredDistance;
    public RangedBehavior() { this(120.0); }
    public RangedBehavior(double preferredDistance) {
        if (!Double.isFinite(preferredDistance) || preferredDistance <= 0) throw new IllegalArgumentException("Preferred distance must be positive");
        this.preferredDistance = preferredDistance;
    }
    @Override public void update(Enemy enemy, double playerX, double playerY, double deltaSeconds) {
        double distance = Math.hypot(playerX - enemy.getX(), playerY - enemy.getY());
        if (distance < preferredDistance * 0.8) enemy.moveAway(playerX, playerY, deltaSeconds);
        else if (distance > preferredDistance * 1.2) enemy.moveTowards(playerX, playerY, deltaSeconds);
    }
}
