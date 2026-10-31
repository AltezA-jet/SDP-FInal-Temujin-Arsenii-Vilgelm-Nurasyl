package com.gamelab;
import com.gamelab.pattern.factory.*;
import com.gamelab.domain.Enemy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EnemyFactoryTest {
 @Test void createsConfiguredEnemyVariants() {
  Enemy skeleton=new SkeletonCreator().createEnemy("s",1,2);
  Enemy slime=new SlimeCreator().createEnemy("l",1,2);
  Enemy boss=new BossCreator().createEnemy("b",1,2);
  assertEquals("Skeleton",skeleton.getName()); assertEquals(55,skeleton.getMaxHealth());
  assertEquals("Slime",slime.getName()); assertEquals(35,slime.getMaxHealth());
  assertEquals("Shadow Boss",boss.getName()); assertEquals(300,boss.getMaxHealth());
  assertNotSame(skeleton.getBehavior(),slime.getBehavior());
 }
}
