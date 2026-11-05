package com.gamelab.pattern.factory;
import com.gamelab.domain.Enemy;
import com.gamelab.domain.EnemyElement;
import com.gamelab.domain.progression.Difficulty;
import com.gamelab.domain.progression.EnemyBalanceCatalog;
import com.gamelab.pattern.strategy.*;
/** Convenient registry; each archetype is still instantiated through Factory Method. */
public final class EnemyFactory {
 private EnemyFactory(){}
 public static Enemy create(EnemyType type,String id,double x,double y){return new ThemedEnemyCreator(type).createEnemy(id,x,y);}
 /** Creates a GDD-balanced enemy for the selected campaign stage and difficulty. */
 public static Enemy create(EnemyBalanceCatalog.Archetype archetype,int stage,Difficulty difficulty,String id,double x,double y){
  EnemyBalanceCatalog.Stats stats=EnemyBalanceCatalog.stats(archetype,stage,difficulty);
  EnemyBehavior behavior=switch(archetype){case SLIME,MOUSE,SKELETON_WARRIOR,GUARD_KNIGHT->new MeleeBehavior();case SKELETON_ARCHER->new RangedBehavior(135);case CULTIST_MAGE->new RangedBehavior(110);case LEVEL_GUARD->new BossBehavior();};
  String name=switch(archetype){case SLIME->"Slime";case MOUSE->"Cave Mouse";case SKELETON_WARRIOR->"Skeleton Warrior";case SKELETON_ARCHER->"Skeleton Archer";case CULTIST_MAGE->"Cultist Mage";case GUARD_KNIGHT->"Guard Knight";case LEVEL_GUARD->"Level Guard";};
  boolean boss=archetype==EnemyBalanceCatalog.Archetype.LEVEL_GUARD;
  int xp=EnemyBalanceCatalog.experience(archetype,stage,difficulty),gold=EnemyBalanceCatalog.gold(archetype,stage,difficulty);
  return new Enemy(id,name,stats.health(),stats.damage(),x,y,boss?24:40,gold,xp,boss?EnemyElement.SHADOW:EnemyElement.NORMAL,boss,behavior);
 }
}
