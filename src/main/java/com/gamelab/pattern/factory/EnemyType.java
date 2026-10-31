package com.gamelab.pattern.factory;
import com.gamelab.domain.EnemyElement;
/** Balanced starting combat values. Values are tuning baselines, not final play-test results. */
public enum EnemyType {
 RUIN_SOLDIER("Ruin Soldier",EnemyElement.NORMAL,70,12,52,8,22,Behavior.MELEE,false),
 RUIN_ARCHER("Ruin Archer",EnemyElement.NORMAL,48,10,42,5,18,Behavior.RANGED,false),
 RUNE_GUARDIAN("Rune Guardian",EnemyElement.NORMAL,260,24,28,45,110,Behavior.BOSS,true),
 ICE_CRAWLER("Ice Crawler",EnemyElement.ICE,82,14,48,10,26,Behavior.MELEE,false),
 FROST_WARRIOR("Frost Warrior",EnemyElement.ICE,115,18,42,14,34,Behavior.MELEE,false),
 FROST_MAGE("Frost Mage",EnemyElement.ICE,72,16,34,12,32,Behavior.RANGED,false),
 ICE_ELEMENTAL("Ice Elemental",EnemyElement.ICE,155,20,36,20,48,Behavior.RANGED,false),
 FROST_GOLEM("Frost Golem",EnemyElement.ICE,390,28,24,65,155,Behavior.BOSS,true),
 FIRE_IMP("Fire Imp",EnemyElement.FIRE,92,17,62,13,34,Behavior.CHARGE,false),
 LAVA_WARRIOR("Lava Warrior",EnemyElement.FIRE,150,22,40,18,42,Behavior.MELEE,false),
 FIRE_MAGE("Fire Mage",EnemyElement.FIRE,105,21,35,17,40,Behavior.RANGED,false),
 INFERNO_GOLEM("Inferno Golem",EnemyElement.FIRE,520,34,27,85,200,Behavior.BOSS,true),
 TOXIC_SLIME("Toxic Slime",EnemyElement.POISON,115,16,30,14,36,Behavior.MELEE,false),
 PLAGUE_BEAST("Plague Beast",EnemyElement.CORRUPTED,175,23,38,22,52,Behavior.CHARGE,false),
 POISON_SHAMAN("Poison Shaman",EnemyElement.CORRUPTED,125,17,32,20,48,Behavior.SUMMON,false),
 PLAGUE_QUEEN("Plague Queen",EnemyElement.CORRUPTED,680,32,25,110,260,Behavior.BOSS,true),
 SHADOW_SOLDIER("Shadow Soldier",EnemyElement.SHADOW,190,25,45,25,58,Behavior.MELEE,false),
 WRAITH("Wraith",EnemyElement.UNDEAD,135,23,70,22,55,Behavior.CHARGE,false),
 NECROMANCER("Necromancer",EnemyElement.UNDEAD,170,20,30,28,65,Behavior.SUMMON,false),
 SHADOW_KNIGHT("Shadow Knight",EnemyElement.SHADOW,850,39,38,150,350,Behavior.BOSS,true),
 SHADOW_LORD("Shadow Lord",EnemyElement.SHADOW,1800,48,42,300,700,Behavior.BOSS,true);
 public enum Behavior { MELEE,RANGED,CHARGE,SUMMON,BOSS }
 public final String displayName; public final EnemyElement element; public final int health,damage,coins,xp; public final double speed; public final Behavior behavior; public final boolean boss;
 EnemyType(String n,EnemyElement e,int h,int d,double s,int c,int xp,Behavior b,boolean boss){displayName=n;element=e;health=h;damage=d;speed=s;coins=c;this.xp=xp;behavior=b;this.boss=boss;}
}
