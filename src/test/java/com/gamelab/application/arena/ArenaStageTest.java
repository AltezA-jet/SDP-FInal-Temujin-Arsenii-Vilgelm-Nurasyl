package com.gamelab.application.arena;

import com.gamelab.domain.item.WeaponCatalog;
import com.gamelab.domain.rune.*;
import com.gamelab.pattern.builder.CharacterBuilder;
import com.gamelab.pattern.observer.GameEventBus;
import org.junit.jupiter.api.Test;
import java.util.*;
import static com.gamelab.application.arena.PlatformPhysics.*;
import static org.junit.jupiter.api.Assertions.*;

class ArenaStageTest {
    @Test void suppliedEnemyAndTerrainResourcesExistWithTheDeclaredFrameSizes() throws Exception {
        var profiles=new ArrayList<>(ArenaCatalog.ALL); profiles.add(ArenaCatalog.SKELETON); profiles.add(ArenaCatalog.POISON_SLIME);
        for(var profile:profiles) {
            String path="/assets/enemies/"+profile.spriteFolder()+"/"+profile.spriteName()+"_idle.png";
            var resource=getClass().getResource(path);
            if(resource==null && profile.id().equals("elemental")) resource=getClass().getResource(path.replace("_idle.png",".png"));
            assertNotNull(resource,path); var image=javax.imageio.ImageIO.read(resource);
            assertEquals(profile.frameWidth(),image.getWidth(),path); assertEquals(profile.frameHeight(),image.getHeight(),path);
        }
        for(int stage=1;stage<=6;stage++) {
            var layout=ArenaLayouts.stage(stage);
            for(String file:List.of(layout.background(),layout.ground(),layout.medium(),layout.small()))
                assertNotNull(getClass().getResource("/assets/enemies/"+layout.asset(file)),file);
        }
    }
    private ArenaSimulation arena() {
        var arena=new ArenaSimulation(CharacterBuilder.warrior("Tester").damage(10).maxHealth(2000).build(),new GameEventBus(),new Random(17));
        arena.setInvincible(true); return arena;
    }
    private void advance(ArenaSimulation arena,double seconds) {
        for(int i=0;i<Math.round(seconds/.02);i++) arena.update(.02,0,false);
    }
    private void killWave(ArenaSimulation arena) { arena.fighters().forEach(f -> f.enemy.takeDamage(f.enemy.getMaxHealth())); }

    @Test void threeWavesShareGeometryAndExitRequiresVictoryAndWalkingRight() {
        var arena=arena(); arena.startStage(3); var geometry=arena.platforms();
        arena.hero().x=1880; arena.update(.02,0,false); assertFalse(arena.completed());
        for(int wave=1;wave<=3;wave++) {
            assertEquals(wave,arena.wave()); assertSame(geometry,arena.platforms());
            if(wave==3) assertTrue(arena.fighters().stream().anyMatch(f -> f.profile.id().equals("fire-golem")));
            arena.hero().x=100; killWave(arena); advance(arena,2.1);
        }
        assertTrue(arena.exitReady()); assertFalse(arena.completed());
        arena.hero().x=1880; arena.update(.02,0,false); assertTrue(arena.completed());
        arena.restart(); assertEquals(1,arena.wave()); assertFalse(arena.exitReady());
    }

    @Test void completionPublishesOnlyOnce() {
        var bus=new GameEventBus(); List<String> events=new ArrayList<>(); bus.subscribe(e -> events.add(e.type()));
        var arena=new ArenaSimulation(CharacterBuilder.warrior("Tester").build(),bus);
        arena.startStage(6); killWave(arena); arena.update(.02,0,false);
        arena.hero().x=1880; advance(arena,1);
        assertEquals(1,events.stream().filter(e -> e.equals("ARENA_STAGE_COMPLETED")).count());
    }

    @Test void layoutsAreDistinctAndAllPlatformsHaveReachableRoutes() {
        Set<List<Platform>> geometries=new HashSet<>(); var physics=new PlatformPhysics();
        for(int stage=1;stage<=6;stage++) {
            var platforms=ArenaLayouts.stage(stage).platforms(); assertTrue(geometries.add(platforms));
            for(var p:platforms) {
                assertTrue(p.x()>30 && p.x()+p.width()<1890);
                assertNotNull(physics.nextPlatform(new Body(350,FLOOR),p.x()+p.width()/2,p.y(),platforms,72),"Slow bosses need a route to "+p);
            }
        }
    }

    @Test void everyBossCanActuallyReachEachPlatformDuringCombat() {
        for(int stage=1;stage<=6;stage++) for(var platform:ArenaLayouts.stage(stage).platforms()) {
            var arena=arena(); arena.start(stage,true);
            arena.hero().x=platform.x()+platform.width()/2; arena.hero().y=platform.y(); arena.hero().grounded=true;
            var boss=arena.fighters().get(0); boolean reached=false;
            StringBuilder trace=new StringBuilder();
            for(int i=0;i<4000;i++) {
                arena.update(.02,0,false);
                if(i%200==0) trace.append(String.format(" x%.0f/y%.0f/%s",boss.body.x,boss.body.y,boss.animation));
                if(boss.body.grounded && Math.abs(boss.body.y-platform.y())<1 && platform.supports(boss.body.x,0)) { reached=true;break; }
            }
            assertTrue(reached,"Boss cannot reach platform at stage "+stage+": "+platform+trace);
        }
    }

    @Test void equippedSwordAndHasteChangeActualDamageAndAttackDuration() {
        var arena=arena(); var loadout=new RuneLoadout(WeaponCatalog.createSwiftBlade());
        loadout.installRune(RuneCatalog.create(RuneType.HASTE)); arena.setLoadout(loadout); arena.start(1,false);
        var target=arena.fighters().get(1); arena.hero().x=target.body.x-60;
        int before=target.enemy.getHealth(); assertTrue(arena.attack());
        assertEquals(.4/(1.35*1.2),arena.playerAttackDuration(),.0001);
        assertEquals(before,target.enemy.getHealth()); advance(arena,.14);
        assertEquals(22,before-target.enemy.getHealth());
        int after=target.enemy.getHealth(); advance(arena,.08); assertEquals(after,target.enemy.getHealth());
    }

    @Test void iceGolemFiresTwoBigShotsAndOneFasterSmallShot() {
        var arena=arena(); arena.start(2,true); arena.hero().x=150;
        for(int i=0;i<500 && arena.projectiles().size()<3;i++) arena.update(.02,0,false);
        assertEquals(3,arena.projectiles().size());
        assertEquals(2,arena.projectiles().stream().filter(p -> p.radius>20).count());
        var small=arena.projectiles().stream().filter(p -> p.radius<20).findFirst().orElseThrow();
        assertEquals(510,Math.hypot(small.vx,small.vy),.001);
    }

    @Test void darkMageSummonsAtMostTwoWeakerSkeletonsWithoutRewardsOrChains() {
        var arena=arena(); arena.start(5,false); advance(arena,60);
        var summons=arena.fighters().stream().filter(f -> f.summoned).toList();
        assertEquals(2,summons.size());
        for(var f:summons) {
            assertEquals("skeleton",f.profile.id()); assertEquals(35,f.enemy.getMaxHealth());
            assertEquals(0,f.enemy.getRewardCoins()); assertEquals(0,f.enemy.getRewardExperience());
        }
    }

    @Test void mageRitualCanBeInterruptedByThePlayerHitFrame() {
        var arena=arena(); arena.start(5,false);
        var mage=arena.fighters().get(1);
        for(int i=0;i<200 && mage.windup==0;i++) arena.update(.02,0,false);
        assertTrue(mage.windup>0);
        arena.hero().x=mage.body.x-50; arena.hero().y=mage.body.y; arena.attack(); advance(arena,.22);
        assertEquals(0,mage.windup); assertTrue(arena.fighters().stream().noneMatch(f -> f.summoned));
    }

    @Test void lordPhasesFollowHealthAndSummonsOnlyInThirdPhase() {
        var arena=arena(); arena.start(1,false); killWave(arena); arena.update(.02,0,false);
        arena.start(6,true); var lord=arena.fighters().get(0); advance(arena,10);
        assertEquals(1,lord.phase); assertEquals(1,arena.fighters().size());
        lord.enemy.takeDamage((int)(lord.enemy.getMaxHealth()*.4)); advance(arena,.1); assertEquals(2,lord.phase);
        lord.enemy.takeDamage((int)(lord.enemy.getMaxHealth()*.35)); advance(arena,30); assertEquals(3,lord.phase);
        assertTrue(arena.fighters().stream().anyMatch(f -> f.summoned));
        assertTrue(arena.fighters().stream().filter(f -> f.summoned && f.enemy.isAlive()).count()<=3);
        assertTrue(arena.fighters().stream().filter(f -> f.summoned).allMatch(f -> !f.profile.boss() && f.enemy.getRewardCoins()==0));
    }

    private ArenaSimulation rollingGolem() {
        var arena=arena(); arena.start(3,true); arena.hero().x=200;
        var golem=arena.fighters().get(0);
        for(int i=0;i<400 && golem.charging==0;i++) arena.update(.02,0,false);
        assertTrue(golem.charging>0); return arena;
    }

    @Test void rollFallsOffPlatformsAndDoesNotTurnToChaseHero() {
        var arena=rollingGolem(); var golem=arena.fighters().get(0);
        golem.body.x=270; golem.body.y=760; golem.body.velocityY=0; golem.body.grounded=true;
        double direction=golem.chargeDirection; arena.hero().x=1500;
        advance(arena,.35); assertTrue(golem.body.y>760); assertEquals(direction,golem.chargeDirection);
    }

    @Test void rollJumpsOnceForPlayerStandingOnHigherPlatformButNotForAirbornePlayer() {
        for(boolean grounded:List.of(false,true)) {
            var arena=rollingGolem(); var golem=arena.fighters().get(0);
            golem.body.x=700; golem.body.y=FLOOR; golem.body.velocityY=0; golem.body.grounded=true; golem.chargeDirection=-1;
            arena.hero().x=440; arena.hero().y=760; arena.hero().velocityY=grounded?0:-100; arena.hero().grounded=grounded;
            arena.update(.02,0,false); assertEquals(grounded,golem.rollJumped);
            if(grounded) { assertTrue(golem.body.velocityY<0); advance(arena,.15); assertTrue(golem.rollJumped); }
        }
    }
}
