package com.gamelab.pattern.strategy;

import com.gamelab.domain.Enemy;
import java.util.Objects;
import java.util.random.RandomGenerator;

/** Wounded enemies retreat for 1–3 seconds, approach for one attack, and repeat. */
public final class WoundedRetreatBehavior implements EnemyBehavior {
    private final EnemyBehavior normal;
    private final EnemyBehavior retreat;
    private final EnemyBehavior approach;
    private final RandomGenerator random;
    private boolean approaching, woundedActive;
    private double phaseTime;

    public WoundedRetreatBehavior(EnemyBehavior normal, double distance) {
        this(normal,distance,new MeleeBehavior());
    }
    public WoundedRetreatBehavior(EnemyBehavior normal, double distance, EnemyBehavior approach) {
        this(normal,distance,approach,RandomGenerator.getDefault());
    }
    public WoundedRetreatBehavior(EnemyBehavior normal, double distance, EnemyBehavior approach, RandomGenerator random) {
        this.normal = Objects.requireNonNull(normal);
        retreat = new RangedBehavior(distance);
        this.approach=Objects.requireNonNull(approach);
        this.random=Objects.requireNonNull(random);
    }

    public boolean isWounded(Enemy enemy) {
        return !enemy.isBoss() && enemy.isAlive() && enemy.getHealth() < enemy.getMaxHealth() * .3;
    }
    public boolean isRetreating(Enemy enemy) { return isWounded(enemy) && !approaching; }
    public boolean isApproaching(Enemy enemy) { return isWounded(enemy) && approaching; }
    private void beginRetreat() {
        woundedActive=true; approaching=false; phaseTime=random.nextDouble(1,3);
    }
    public void attackCompleted(Enemy enemy) { if (isWounded(enemy)) beginRetreat(); }

    @Override public void update(Enemy enemy, double x, double y, double dt) {
        if (!isWounded(enemy)) { approaching=false; woundedActive=false; normal.update(enemy,x,y,dt); return; }
        if (!woundedActive) beginRetreat();
        phaseTime-=dt;
        if (phaseTime<=0) {
            if (approaching) beginRetreat();
            else { approaching=true; phaseTime=6; }
        }
        (approaching ? approach : retreat).update(enemy,x,y,dt);
    }
}
