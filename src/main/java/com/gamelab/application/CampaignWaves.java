package com.gamelab.application;
import com.gamelab.domain.WaveDefinition;
import com.gamelab.pattern.factory.EnemyType;
import java.util.*;
/** Campaign roster only. The JavaFX/game-loop owner is responsible for spawning and advancing waves. */
public final class CampaignWaves { private static final List<WaveDefinition> WAVES=List.of(
 wave(1,"Forgotten Ruins",false,EnemyType.RUIN_SOLDIER,5),
 wave(2,"Forgotten Ruins",false,EnemyType.RUIN_SOLDIER,4,EnemyType.RUIN_ARCHER,2),
 wave(3,"Forgotten Ruins",true,EnemyType.RUIN_SOLDIER,2,EnemyType.RUNE_GUARDIAN,1),
 wave(4,"Frozen Caves",false,EnemyType.ICE_CRAWLER,4,EnemyType.FROST_WARRIOR,2),
 wave(5,"Frozen Caves",false,EnemyType.FROST_WARRIOR,3,EnemyType.FROST_MAGE,2,EnemyType.ICE_ELEMENTAL,1),
 wave(6,"Frozen Caves",true,EnemyType.ICE_CRAWLER,2,EnemyType.FROST_GOLEM,1),
 wave(7,"Burning Mines",false,EnemyType.FIRE_IMP,5),
 wave(8,"Burning Mines",false,EnemyType.LAVA_WARRIOR,3,EnemyType.FIRE_MAGE,2),
 wave(9,"Burning Mines",true,EnemyType.FIRE_IMP,2,EnemyType.INFERNO_GOLEM,1),
 wave(10,"Poisoned Swamp",false,EnemyType.TOXIC_SLIME,5),
 wave(11,"Poisoned Swamp",false,EnemyType.PLAGUE_BEAST,3,EnemyType.POISON_SHAMAN,2),
 wave(12,"Poisoned Swamp",true,EnemyType.TOXIC_SLIME,2,EnemyType.PLAGUE_QUEEN,1),
 wave(13,"Shadow Castle",false,EnemyType.SHADOW_SOLDIER,5),
 wave(14,"Shadow Castle",false,EnemyType.SHADOW_SOLDIER,3,EnemyType.WRAITH,2,EnemyType.NECROMANCER,1),
 wave(15,"Shadow Castle",true,EnemyType.WRAITH,2,EnemyType.SHADOW_KNIGHT,1));
 private CampaignWaves(){} private static WaveDefinition wave(int n,String stage,boolean boss,Object... pairs){Map<EnemyType,Integer> m=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)m.put((EnemyType)pairs[i],(Integer)pairs[i+1]);return new WaveDefinition(n,stage,m,boss);} public static List<WaveDefinition> all(){return WAVES;} public static WaveDefinition get(int number){if(number<1||number>WAVES.size())throw new IllegalArgumentException("Wave must be between 1 and 15");return WAVES.get(number-1);} public static EnemyType finalBoss(){return EnemyType.SHADOW_LORD;} }
