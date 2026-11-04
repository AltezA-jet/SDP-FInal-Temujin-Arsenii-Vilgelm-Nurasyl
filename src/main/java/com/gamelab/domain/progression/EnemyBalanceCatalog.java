package com.gamelab.domain.progression;

import java.util.Map;

/** Normal-difficulty enemy table; Easy/Hard are derived with the GDD multipliers. */
public final class EnemyBalanceCatalog {
    public record Stats(int health, int damage) { }
    public enum Archetype { SLIME, MOUSE, SKELETON_WARRIOR, SKELETON_ARCHER, CULTIST_MAGE, GUARD_KNIGHT, LEVEL_GUARD }
    private static final Map<Archetype,int[][]> TABLE=Map.of(
        Archetype.SLIME, rows(20,5,26,6,32,6,38,7,44,8,50,9),
        Archetype.MOUSE, rows(14,4,18,5,22,5,27,6,31,6,35,7),
        Archetype.SKELETON_WARRIOR, rows(40,9,52,10,64,12,76,13,88,14,100,16),
        Archetype.SKELETON_ARCHER, rows(28,7,36,8,45,9,53,10,62,11,70,12),
        Archetype.CULTIST_MAGE, rows(35,12,46,14,56,16,66,17,77,19,88,21),
        Archetype.GUARD_KNIGHT, rows(120,18,156,21,192,23,228,26,264,29,300,32),
        Archetype.LEVEL_GUARD, rows(250,20,325,23,400,26,475,29,550,32,625,35));
    private EnemyBalanceCatalog(){}
    private static int[][] rows(int... values){int[][] out=new int[6][2];for(int i=0;i<6;i++){out[i][0]=values[i*2];out[i][1]=values[i*2+1];}return out;}
    public static Stats stats(Archetype archetype,int stage,Difficulty difficulty){
        if(stage<1||stage>6)throw new IllegalArgumentException("Stage must be 1–6");
        int[] base=TABLE.get(archetype)[stage-1];
        return new Stats((int)Math.round(base[0]*difficulty.healthMultiplier()),(int)Math.round(base[1]*difficulty.damageMultiplier()));
    }
    public static Stats shadowBoss(Difficulty difficulty){return new Stats(switch(difficulty){case EASY->2000;case NORMAL->2500;case HARD->3000;},switch(difficulty){case EASY->42;case NORMAL->60;case HARD->90;});}
    public static int experience(Archetype type,int stage,Difficulty difficulty){int base=switch(type){case SLIME,MOUSE->8;case SKELETON_WARRIOR,SKELETON_ARCHER->15;case CULTIST_MAGE->22;case GUARD_KNIGHT->50;case LEVEL_GUARD->120;};return (int)Math.round((base*(1+(stage-1)/5.0))*difficulty.experienceMultiplier());}
    public static int gold(Archetype type,int stage,Difficulty difficulty){int base=switch(type){case SLIME->3;case MOUSE->2;case SKELETON_WARRIOR,SKELETON_ARCHER->5;case CULTIST_MAGE->8;case GUARD_KNIGHT->20;case LEVEL_GUARD->40;};double top=switch(type){case SLIME->5;case MOUSE->4;case SKELETON_WARRIOR,SKELETON_ARCHER->9;case CULTIST_MAGE->14;case GUARD_KNIGHT,LEVEL_GUARD->35;};return (int)Math.round((base+(top-base)*(stage-1)/5.0)*difficulty.goldMultiplier());}
}
