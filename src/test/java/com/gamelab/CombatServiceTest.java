package com.gamelab;
import com.gamelab.application.CombatService;
import com.gamelab.domain.*;
import com.gamelab.pattern.observer.*;
import com.gamelab.pattern.strategy.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CombatServiceTest {
 @Test void attackDamagesEnemyAndPublishesHit() {
  GameEventBus bus=new GameEventBus(); int[] hits={0};
  bus.subscribe(event -> { if (GameEventTypes.COMBAT_HIT.equals(event.type())) hits[0]++; });
  PlayerCharacter player=new PlayerCharacter("Hero","Rogue",100,20,0);
  Enemy enemy=new com.gamelab.pattern.factory.SkeletonCreator().createEnemy("s",0,0);
  CombatService combat=new CombatService(bus);
  assertEquals(20,combat.attack(player,enemy,new BalancedStrategy()));
  assertEquals(35,enemy.getHealth()); assertEquals(1,hits[0]);
 }
 @Test void killingEnemySetsHealthToZeroAndAwardsKillEvent() {
  GameEventBus bus=new GameEventBus(); int[] kills={0};
  bus.subscribe(event -> { if (GameEventTypes.ENEMY_KILLED.equals(event.type())) kills[0]++; });
  PlayerCharacter player=new PlayerCharacter("Hero","Warrior",100,100,0);
  Enemy enemy=new com.gamelab.pattern.factory.SkeletonCreator().createEnemy("s",0,0);
  CombatService combat=new CombatService(bus);
  combat.attack(player,enemy,new BalancedStrategy());
  combat.attack(player,enemy,new BalancedStrategy());
  assertFalse(enemy.isAlive()); assertEquals(0,enemy.getHealth()); assertEquals(1,kills[0]);
 }
 @Test void rejectsNegativeDirectDamage() {
  Enemy enemy=new com.gamelab.pattern.factory.SkeletonCreator().createEnemy("s",0,0);
  assertThrows(IllegalArgumentException.class, () -> new CombatService(new GameEventBus()).damageEnemy(enemy,-1));
 }
}
