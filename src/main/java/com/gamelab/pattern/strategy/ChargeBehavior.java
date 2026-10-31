package com.gamelab.pattern.strategy;
import com.gamelab.domain.Enemy;
/** Aggressively closes distance; attack timing is handled by CombatService/game loop. */
public final class ChargeBehavior implements EnemyBehavior { private final double range; public ChargeBehavior(){this(220);} public ChargeBehavior(double range){if(range<=0)throw new IllegalArgumentException("Range must be positive");this.range=range;} public void update(Enemy e,double x,double y,double dt){if(Math.hypot(x-e.getX(),y-e.getY())<=range)e.moveTowards(x,y,dt*1.65);else e.moveTowards(x,y,dt);} }
