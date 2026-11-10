package com.gamelab.application.arena;

import com.gamelab.domain.Enemy;
import com.gamelab.pattern.builder.CharacterBuilder;
import com.gamelab.pattern.factory.EnemyFactory;
import com.gamelab.pattern.factory.EnemyType;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.strategy.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static com.gamelab.application.arena.PlatformPhysics.*;

class ArenaSimulationTest {
    private ArenaSimulation arena() {
        ArenaSimulation arena=new ArenaSimulation(CharacterBuilder.warrior("Tester")
                .maxHealth(2000).damage(100).defense(0).build(),new GameEventBus());
        arena.setInvincible(true);
        return arena;
    }
    private void advance(ArenaSimulation arena,double seconds) {
        for (int i=0;i<(int)(seconds/.02);i++) arena.update(.02,0,false);
    }

    @Test void rosterHasTwoRegularEnemiesAndOneBossPerStageAndFinalBoss() {
        for (int stage=1;stage<=5;stage++) {
            assertEquals(2,ArenaCatalog.stage(stage,false).size());
            assertEquals(1,ArenaCatalog.stage(stage,true).size());
        }
        assertEquals(1,ArenaCatalog.stage(6,true).size());
        assertThrows(IllegalArgumentException.class,()->ArenaCatalog.stage(0,false));
    }

    @Test void woundedMeleeEnemyRetreatsAndResumesChaseWhenHealed() {
        Enemy enemy=EnemyFactory.create(EnemyType.RUIN_SOLDIER,"soldier",100,0);
        WoundedRetreatBehavior behavior=new WoundedRetreatBehavior(new MeleeBehavior(),330);
        enemy.setBehavior(behavior);
        enemy.update(0,0,.1);
        assertTrue(enemy.getX()<100);
        enemy.takeDamage(55);
        double before=enemy.getX();
        enemy.update(0,0,.1);
        assertTrue(behavior.isRetreating(enemy));
        assertTrue(enemy.getX()>before);
        enemy.heal(70);
        before=enemy.getX(); enemy.update(0,0,.1);
        assertFalse(behavior.isRetreating(enemy));
        assertTrue(enemy.getX()<before);
    }

    @Test void bossDoesNotRetreatAtLowHealth() {
        Enemy boss=EnemyFactory.create(EnemyType.SHADOW_LORD,"boss",100,0);
        boss.takeDamage(boss.getMaxHealth()-1);
        WoundedRetreatBehavior behavior=new WoundedRetreatBehavior(new MeleeBehavior(),330);
        boss.setBehavior(behavior); boss.update(0,0,.1);
        assertFalse(behavior.isRetreating(boss));
        assertTrue(boss.getX()<100);
    }

    @Test void rangedEnemyRetreatsWhenCloseButAdvancesOutsideShootingDistance() {
        Enemy enemy=EnemyFactory.create(EnemyType.RUIN_ARCHER,"archer",100,0);
        enemy.setBehavior(new RangedBehavior(400));
        enemy.update(0,0,.1); assertTrue(enemy.getX()>100);
        double before=enemy.getX();
        enemy.update(1000,0,.1); assertTrue(enemy.getX()>before);
        enemy.setPosition(400,0); enemy.update(0,0,.1);
        assertEquals(400,enemy.getX(),.001);
    }

    @Test void bodyPassesUpThroughPlatformAndLandsOnItsTop() {
        PlatformPhysics physics=new PlatformPhysics();
        Body body=new Body(350,FLOOR);
        List<Platform> platforms=List.of(new Platform(210,755,310));
        physics.jump(body);
        boolean roseAbove=false;
        for (int i=0;i<60;i++) {
            physics.step(body,0,24,.02,platforms);
            roseAbove|=body.y<755;
        }
        assertTrue(roseAbove); assertEquals(755,body.y,.001); assertTrue(body.grounded);
        assertThrows(IllegalArgumentException.class,()->physics.step(body,0,24,Double.NaN,platforms));
    }

    @Test void regularEnemyAndBossCanFollowHeroOntoPlatforms() {
        ArenaSimulation arena=arena(); arena.start(1,false);
        arena.hero().x=800; arena.hero().y=615; arena.hero().grounded=true;
        boolean reachedUpper=false;
        for (int i=0;i<1500;i++) {
            arena.update(.02,0,false);
            reachedUpper|=arena.fighters().stream().anyMatch(f -> f.body.y<=616);
        }
        assertTrue(reachedUpper,"A regular enemy should navigate the stepping stones");
        arena.start(1,true); arena.hero().x=800; arena.hero().y=615;
        boolean bossClimbed=false;
        for(int i=0;i<1500;i++) { arena.update(.02,0,false); bossClimbed|=arena.fighters().get(0).body.y<754; }
        assertTrue(bossClimbed);
    }

    @Test void switchingPlayerStrategyChangesRealCombatDamageAndEmitsEvents() {
        List<String> events=new ArrayList<>(); GameEventBus bus=new GameEventBus();
        bus.subscribe(event -> events.add(event.type()));
        ArenaSimulation arena=new ArenaSimulation(CharacterBuilder.warrior("Hero").damage(20).build(),bus);
        arena.setInvincible(true); arena.start(2,false);
        ArenaSimulation.Fighter enemy=arena.fighters().get(0);
        arena.hero().x=enemy.body.x-60;
        int before=enemy.enemy.getHealth();
        assertTrue(arena.attack()); assertEquals(before,enemy.enemy.getHealth());
        advance(arena,.22); assertEquals(20,before-enemy.enemy.getHealth());
        assertFalse(arena.attack(),"Attack cooldown prevents key repeat damage");
        advance(arena,.4); arena.hero().x=enemy.body.x-60;
        arena.setPlayerStrategy(new HeavyAttackStrategy()); before=enemy.enemy.getHealth();
        assertTrue(arena.attack()); advance(arena,.38); assertEquals(30,before-enemy.enemy.getHealth());
        assertTrue(events.contains("ENEMY_HEALTH_CHANGED"));
    }

    @Test void bossAndOrdinaryProjectilesHaveDifferentTerrainRules() {
        ArenaSimulation arena=arena(); arena.start(1,true);
        advance(arena,2);
        assertFalse(arena.projectiles().isEmpty());
        assertTrue(arena.projectiles().stream().allMatch(p -> p.throughPlatforms));
        ArenaSimulation.Projectile bossShot=arena.projectiles().get(0);
        bossShot.x=800; bossShot.y=614.99;
        arena.hero().x=100;
        arena.update(.02,0,false);
        assertTrue(bossShot.y>615);
        assertTrue(arena.projectiles().contains(bossShot),"Boss shot crosses a platform intact");
        arena.start(1,false); arena.hero().x=700;
        advance(arena,2);
        assertFalse(arena.projectiles().isEmpty());
        assertTrue(arena.projectiles().stream().noneMatch(p -> p.throughPlatforms));
        ArenaSimulation.Projectile normalShot=arena.projectiles().get(0);
        normalShot.x=800; normalShot.y=614.99;
        arena.hero().x=100;
        arena.update(.02,0,false);
        assertTrue(normalShot.y>615);
        assertFalse(arena.projectiles().contains(normalShot),"Regular shot is blocked by the platform");
    }

    @Test void finalBossSummonsOnlyPreviouslyKilledRegularTypesWithHalfStats() {
        ArenaSimulation arena=arena(); arena.start(1,false);
        ArenaSimulation.Fighter soldier=arena.fighters().get(1);
        soldier.enemy.takeDamage(soldier.enemy.getMaxHealth()-1);
        arena.hero().x=soldier.body.x-60; arena.attack();
        advance(arena,.22);
        assertEquals(java.util.Set.of("soldier"),arena.defeatedTypes());
        arena.start(6,true);
        var lord=arena.fighters().get(0).enemy;
        lord.takeDamage((int)(lord.getMaxHealth()*.75));
        advance(arena,15);
        List<ArenaSimulation.Fighter> summons=arena.fighters().stream().filter(f -> f.summoned).toList();
        assertFalse(summons.isEmpty());
        assertTrue(summons.stream().allMatch(f -> f.profile.id().equals("soldier") && !f.enemy.isBoss()));
        assertEquals(EnemyType.RUIN_SOLDIER.health/2,summons.get(0).enemy.getMaxHealth());
        assertEquals(EnemyType.RUIN_SOLDIER.damage/2,summons.get(0).enemy.getDamage());
        arena.restart();
        assertEquals(java.util.Set.of("soldier"),arena.defeatedTypes());
        assertTrue(arena.projectiles().isEmpty());
    }

    @Test void finalBossWithNoKillHistoryDoesNotInventSummonsAndRejectsInvalidInput() {
        ArenaSimulation arena=arena(); arena.start(6,true); advance(arena,4);
        assertTrue(arena.fighters().stream().noneMatch(f -> f.summoned));
        assertThrows(IllegalArgumentException.class,()->arena.start(6,false));
        assertThrows(IllegalArgumentException.class,()->arena.update(.1,0,false));
        assertThrows(IllegalArgumentException.class,()->arena.update(.02,Double.NaN,false));
    }

    @Test void witchSummonCountRemainsBoundedOverLongFight() {
        ArenaSimulation arena=arena(); arena.start(4,true); advance(arena,90);
        long count=arena.fighters().stream().filter(f -> f.summoned && f.enemy.isAlive()).count();
        assertTrue(count>=2 && count<=6);
        assertTrue(arena.fighters().stream().filter(f -> f.summoned).allMatch(f -> f.profile.id().equals("poison-slime") && f.enemy.getRewardCoins()==0 && f.enemy.getRewardExperience()==0));
    }

    @Test void fireMageCreatesTemporaryGroundHazard() {
        ArenaSimulation arena=arena(); arena.start(3,false);
        arena.hero().x=1500;
        boolean sawZone=false;
        for (int i=0;i<500;i++) { arena.update(.02,0,false); sawZone|=!arena.zones().isEmpty(); }
        assertTrue(sawZone);
    }

    @Test void poisonContinuesAfterLeavingAttackerAndHealingClearsIt() {
        ArenaSimulation arena=arena(); arena.setInvincible(false); arena.start(4,false);
        arena.hero().x=1500;
        for (int i=0;i<500 && !arena.poisoned();i++) arena.update(.02,0,false);
        assertTrue(arena.poisoned());
        arena.hero().x=100;
        int before=arena.player().getHealth(); advance(arena,1.2);
        assertTrue(arena.player().getHealth()<before,"Poison still ticks away from the attacker");
        arena.restorePlayer();
        assertFalse(arena.poisoned()); assertEquals(arena.player().getMaxHealth(),arena.player().getHealth());
    }

    @Test void dodgePreventsMeleeHitAndCannotBeRepeatedWithoutCooldown() {
        ArenaSimulation arena=arena(); arena.setInvincible(false); arena.start(1,false);
        ArenaSimulation.Fighter soldier=arena.fighters().get(1);
        arena.hero().x=soldier.body.x-60;
        for (int i=0;i<80 && soldier.windup==0;i++) arena.update(.02,0,false);
        assertTrue(soldier.windup>0);
        while (soldier.windup>.12) arena.update(.02,0,false);
        int before=arena.player().getHealth();
        arena.dodge(); advance(arena,.14);
        assertEquals(before,arena.player().getHealth());
        advance(arena,.2); arena.dodge();
        assertFalse(arena.dodging(),"Dodge cooldown prevents repeated invulnerability");
    }

    @Test void enemiesFinishClimbingEachPlatformInsteadOfJumpingInALoop() {
        for (Platform target : ArenaSimulation.PLATFORMS) {
            ArenaSimulation arena=arena(); arena.start(1,false);
            arena.hero().x=target.x()+target.width()/2;
            arena.hero().y=target.y(); arena.hero().grounded=true;
            advance(arena,60);
            for (ArenaSimulation.Fighter fighter : arena.fighters()) {
                if (!fighter.profile.ranged() || !arena.canShoot(fighter))
                    assertEquals(target.y(),fighter.body.y,1,"Enemy must settle on target platform: "+target+" / "+fighter.profile.id());
                assertTrue(fighter.body.grounded);
            }
            advance(arena,5);
            for (ArenaSimulation.Fighter fighter : arena.fighters()) {
                if (!fighter.profile.ranged() || !arena.canShoot(fighter))
                    assertEquals(target.y(),fighter.body.y,1,"Distance-keeping must not restart the climb");
                assertTrue(fighter.body.grounded);
            }
        }
    }

    @Test void actualDamageStartsAndRestartsHurtAnimationWithoutBlockingMovement() {
        ArenaSimulation arena=arena(); arena.start(2,false);
        ArenaSimulation.Fighter elemental=arena.fighters().get(1);
        arena.hero().x=elemental.body.x-60;
        arena.setPlayerStrategy(new BalancedStrategy());
        assertTrue(arena.attack());
        assertEquals(0,elemental.hurtTime,.001);
        advance(arena,.22);
        assertTrue(elemental.hurtTime>0);
        double before=elemental.body.x;
        advance(arena,.4);
        assertTrue(elemental.hurtElapsed>0);
        assertNotEquals(before,elemental.body.x,"Hurt visual must not freeze movement");
        advance(arena,.2);
        assertEquals(0,elemental.hurtTime,.001);
        arena.hero().x=elemental.body.x-60;
        arena.attack();
        advance(arena,.22);
        assertTrue(elemental.hurtTime>0);
    }

    @Test void approachingAndJumpingNeverExceedNormalWalkingSpeed() {
        for (int stage=1;stage<=5;stage++) {
            ArenaSimulation arena=arena(); arena.start(stage,false);
            Platform target=arena.platforms().get(1);
            arena.hero().x=target.x()+target.width()/2;
            arena.hero().y=target.y(); arena.hero().grounded=true;
            boolean jumped=false;
            java.util.Set<String> landed=new java.util.HashSet<>();
            for (int tick=0;tick<3000;tick++) {
                double[] before=arena.fighters().stream().mapToDouble(f -> f.body.x).toArray();
                arena.update(.02,0,false);
                for (int i=0;i<before.length;i++) {
                    ArenaSimulation.Fighter fighter=arena.fighters().get(i);
                    if(fighter.body.grounded && Math.abs(fighter.body.y-target.y())<1 && target.supports(fighter.body.x,0)) landed.add(fighter.enemy.getId());
                    assertTrue(Math.abs(fighter.body.x-before[i])<=Math.max(340,fighter.walkingSpeed())*.02+.001,
                            "Approach/jump accelerated "+fighter.profile.id());
                    jumped|=fighter.body.velocityY<0;
                }
            }
            assertTrue(jumped,"Enemies should still be able to jump at stage "+stage);
            for (ArenaSimulation.Fighter fighter : arena.fighters())
                if (!fighter.profile.ranged() || !arena.canShoot(fighter))
                    assertTrue(landed.contains(fighter.enemy.getId()),"Enemy must land on the platform without a speed boost: "+fighter.profile.id());
        }
    }
    @Test void blockedArcherFindsAFiringPosition() {
        ArenaSimulation arena=arena(); arena.start(1,false);
        var archer=arena.fighters().get(0);
        archer.body.x=350; archer.enemy.setPosition(350,FLOOR);
        arena.hero().x=350; arena.hero().y=755; arena.hero().grounded=true;
        assertFalse(arena.canShoot(archer));
        for (int i=0;i<1500;i++) {
            int attacks=archer.attacks; boolean clear=arena.canShoot(archer);
            arena.update(.02,0,false);
            if (archer.attacks>attacks) assertTrue(clear && arena.canShoot(archer));
        }
        assertTrue(archer.attacks>0);
    }

    @Test void coverDuringWindupCancelsShot() {
        ArenaSimulation arena=arena(); arena.start(1,false); arena.hero().x=1000;
        var archer=arena.fighters().get(0);
        for (int i=0;i<100 && archer.windup==0;i++) arena.update(.02,0,false);
        assertTrue(archer.windup>0);
        arena.hero().x=archer.body.x; arena.hero().y=755; arena.hero().grounded=true;
        assertFalse(arena.canShoot(archer)); advance(arena,.52);
        assertEquals(0,archer.attacks);
        assertTrue(arena.projectiles().stream().noneMatch(p -> p.source.equals("archer")));
    }

    @Test void woundedMeleeRepeatsRetreatAndAttack() {
        ArenaSimulation arena=arena(); arena.start(1,false); arena.hero().x=1400;
        var soldier=arena.fighters().get(1); arena.woundEnemies();
        advance(arena,.8); assertTrue(soldier.retreating()); assertEquals(0,soldier.attacks);
        boolean retreatedAfterAttack=false;
        for (int i=0;i<2500;i++) {
            arena.update(.02,0,false);
            retreatedAfterAttack|=soldier.attacks>0 && soldier.retreating();
        }
        assertTrue(retreatedAfterAttack); assertTrue(soldier.attacks>=2);
    }

    @Test void woundedRangedReturnsToAttack() {
        ArenaSimulation arena=arena(); arena.start(2,false); arena.hero().x=1500;
        var elemental=arena.fighters().get(1); arena.woundEnemies();
        advance(arena,.8); assertEquals(0,elemental.attacks);
        advance(arena,35); assertTrue(elemental.attacks>=2);
    }

    @Test void firingGeometryMatchesPlatformObstacles() {
        assertTrue(FiringPositionPlanner.blocked(350,845,350,707,12,ArenaSimulation.PLATFORMS));
        assertFalse(FiringPositionPlanner.blocked(350,845,1000,852,12,ArenaSimulation.PLATFORMS));
        assertTrue(FiringPositionPlanner.blocked(300,755,600,755,12,ArenaSimulation.PLATFORMS));
        assertFalse(FiringPositionPlanner.canShoot(100,FLOOR,1800,FLOOR,580,ArenaSimulation.PLATFORMS));
    }

    @Test void retreatDurationIsRandomAndWithinOneToThreeSeconds() {
        Enemy enemy=EnemyFactory.create(EnemyType.RUIN_SOLDIER,"wounded",100,0);
        enemy.takeDamage(enemy.getMaxHealth()-1);
        var behavior=new WoundedRetreatBehavior(new MeleeBehavior(),330,new MeleeBehavior(),new java.util.Random(42));
        java.util.Set<Integer> durations=new java.util.HashSet<>();
        for (int cycle=0;cycle<10;cycle++) {
            int ticks=0;
            while (!behavior.isApproaching(enemy) && ticks<310) { behavior.update(enemy,0,0,.01); ticks++; }
            assertTrue(ticks>=100 && ticks<=301); assertTrue(behavior.isApproaching(enemy));
            durations.add(ticks); behavior.attackCompleted(enemy); assertTrue(behavior.isRetreating(enemy));
        }
        assertTrue(durations.size()>1);
    }
}
