package com.gamelab.application.arena;

import com.gamelab.pattern.builder.CharacterBuilder;
import com.gamelab.pattern.observer.GameEventBus;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.gamelab.application.arena.PlatformPhysics.FLOOR;

class ArenaDifficultyTest {
    private ArenaSimulation arena(GameEventBus events,Random random) {
        var arena=new ArenaSimulation(CharacterBuilder.warrior("Tester").maxHealth(2000).damage(100).build(),events,random);
        arena.setInvincible(true);return arena;
    }
    @Test void ordinaryEnemiesAndBossesHaveStrongerStatsWithUnchangedRewards() {
        var arena=arena(new GameEventBus(),new Random(1));
        for(int stage=1;stage<=6;stage++) for(boolean boss:List.of(false,true)) {
            if(stage==6 && !boss)continue;
            arena.start(stage,boss);
            for(var fighter:arena.fighters()) {
                var base=fighter.profile.type();
                assertTrue(fighter.enemy.getMaxHealth()>base.health);
                assertTrue(fighter.enemy.getDamage()>base.damage);
                assertEquals(base.coins,fighter.enemy.getRewardCoins());
                assertEquals(base.xp,fighter.enemy.getRewardExperience());
            }
        }
    }

    @Test void lordSummonsRepeatedlyInMeleeAndAtDistanceWithoutProbabilityStarvation() {
        for(boolean close:List.of(true,false)) {
            var bus=new GameEventBus();List<Double> summons=new ArrayList<>();double[] time={0};
            bus.subscribe(e -> { if(e.type().equals("SUMMON"))summons.add(time[0]); });
            // This roll would always reject the old 35% summon check.
            var arena=arena(bus,new Random(17){ @Override public double nextDouble(){return .99;} });
            arena.start(1,false);arena.fighters().forEach(f -> f.enemy.takeDamage(f.enemy.getMaxHealth()));arena.update(.02,0,false);
            arena.start(6,true);var lord=arena.fighters().get(0);
            lord.enemy.takeDamage((int)(lord.enemy.getMaxHealth()*.75));
            for(int i=0;i<1750;i++) {
                time[0]=i*.02;
                // Clear the previous summons so the next ritual has available slots.
                arena.fighters().stream().filter(f -> f.summoned).forEach(f -> f.enemy.takeDamage(f.enemy.getMaxHealth()));
                arena.hero().x=Math.max(24,Math.min(1896,lord.body.x-(close?70:500)));
                arena.hero().y=FLOOR;arena.hero().grounded=true;arena.hero().velocityY=0;
                arena.update(.02,0,false);
                assertTrue(arena.fighters().stream().filter(f -> f.summoned && f.enemy.isAlive()).count()<=3);
            }
            assertTrue(summons.size()>=3,"Summons must recur even in melee: "+close+" / "+summons);
            // Allow the initial cooldown, a heavy swing already in progress, its recovery, and the ritual windup.
            assertTrue(summons.get(0)<5,"Third-phase first summon should not wait for retreat: "+summons);
            for(int i=1;i<summons.size();i++) {
                double interval=summons.get(i)-summons.get(i-1);
                assertTrue(interval>=4.5 && interval<10,"Unexpected summon interval: "+interval);
            }
        }
    }

    @Test void fullMinionLimitDoesNotWasteRitualsAndVacanciesAreRefilled() {
        var bus=new GameEventBus();int[] rituals={0};bus.subscribe(e -> {if(e.type().equals("SUMMON"))rituals[0]++;});
        var arena=arena(bus,new Random(17));arena.start(1,false);
        arena.fighters().forEach(f -> f.enemy.takeDamage(f.enemy.getMaxHealth()));arena.update(.02,0,false);
        arena.start(6,true);var lord=arena.fighters().get(0);lord.enemy.takeDamage((int)(lord.enemy.getMaxHealth()*.75));
        for(int i=0;i<1000;i++)arena.update(.02,0,false);
        assertEquals(1,rituals[0]);assertEquals(3,arena.fighters().stream().filter(f -> f.summoned && f.enemy.isAlive()).count());
        arena.fighters().stream().filter(f -> f.summoned).findFirst().orElseThrow().enemy.takeDamage(10000);
        for(int i=0;i<400;i++)arena.update(.02,0,false);
        assertEquals(2,rituals[0]);assertEquals(3,arena.fighters().stream().filter(f -> f.summoned && f.enemy.isAlive()).count());
    }
}
