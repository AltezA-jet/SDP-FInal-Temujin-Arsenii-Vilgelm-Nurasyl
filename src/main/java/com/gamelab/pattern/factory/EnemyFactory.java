package com.gamelab.pattern.factory;
import com.gamelab.domain.Enemy;
/** Convenient registry; each archetype is still instantiated through Factory Method. */
public final class EnemyFactory { private EnemyFactory(){} public static Enemy create(EnemyType type,String id,double x,double y){return new ThemedEnemyCreator(type).createEnemy(id,x,y);} }
