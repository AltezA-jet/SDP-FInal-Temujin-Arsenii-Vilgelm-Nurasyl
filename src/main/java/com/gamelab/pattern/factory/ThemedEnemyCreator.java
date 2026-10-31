package com.gamelab.pattern.factory;
import com.gamelab.domain.Enemy;
import com.gamelab.domain.EnemyElement;
import com.gamelab.pattern.strategy.*;
/** Factory Method implementation for all campaign enemy archetypes. */
public final class ThemedEnemyCreator extends EnemyCreator { private final EnemyType type; public ThemedEnemyCreator(EnemyType type){this.type=java.util.Objects.requireNonNull(type);} @Override public Enemy createEnemy(String id,double x,double y){EnemyBehavior b=switch(type.behavior){case MELEE->new MeleeBehavior();case RANGED->new RangedBehavior(135);case CHARGE->new ChargeBehavior();case SUMMON->new SummonBehavior();case BOSS->new BossBehavior();}; return new Enemy(id,type.displayName,type.health,type.damage,x,y,type.speed,type.coins,type.xp,type.element,type.boss,b);} public EnemyType getType(){return type;} }
