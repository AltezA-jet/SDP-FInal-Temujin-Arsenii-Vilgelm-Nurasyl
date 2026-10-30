package com.gamelab.pattern.factory;
import com.gamelab.domain.Enemy;
/** Factory Method: subclasses decide which configured enemy to instantiate. */
public abstract class EnemyCreator {
    public abstract Enemy createEnemy(String id, double x, double y);
}
