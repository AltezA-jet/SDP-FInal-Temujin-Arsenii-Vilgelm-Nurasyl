package com.gamelab.pattern.strategy;
import com.gamelab.domain.Enemy;
/** Closes distance for a melee strike. */
public final class MeleeBehavior implements EnemyBehavior { public void update(Enemy e,double x,double y,double dt){e.moveTowards(x,y,dt);} }
