package com.gamelab.ui.audio;

import com.gamelab.application.arena.ArenaSimulation;
import com.gamelab.pattern.builder.CharacterBuilder;
import com.gamelab.pattern.observer.GameEventBus;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import static org.junit.jupiter.api.Assertions.*;

class CombatAudioTest {
    @Test void jumpDodgeAndLandingSoundOnlyWhenThoseActionsActuallyHappen() {
        List<SoundCue> heard=new ArrayList<>();var events=new GameEventBus();
        try(var audio=new CombatAudio(events,heard::add)) {
            var arena=new ArenaSimulation(CharacterBuilder.warrior("Hero").build(),events);arena.start(1,false);
            arena.jump();arena.jump();arena.dodge();arena.dodge();
            for(int i=0;i<60;i++)arena.update(.02,0,false);
            assertEquals(1,heard.stream().filter(c -> c==SoundCue.JUMP).count());
            assertEquals(1,heard.stream().filter(c -> c==SoundCue.DODGE).count());
            assertEquals(1,heard.stream().filter(c -> c==SoundCue.LAND).count());
        }
    }
    @Test void damageVoiceMatchesRealHealthLossAndNeverPlaysDuringInvulnerability() {
        List<SoundCue> heard=new ArrayList<>();var events=new GameEventBus();
        List<com.gamelab.application.audio.PlayerDamage> hits=new ArrayList<>();
        events.subscribe(e -> {if(e.payload() instanceof com.gamelab.application.audio.PlayerDamage damage)hits.add(damage);});
        try(var audio=new CombatAudio(events,heard::add)) {
            var arena=new ArenaSimulation(CharacterBuilder.warrior("Hero").maxHealth(2000).defense(0).build(),events);arena.start(1,false);
            arena.hero().x=1400;
            for(int i=0;i<300;i++)arena.update(.02,0,false);
            assertFalse(hits.isEmpty());assertTrue(hits.stream().allMatch(h -> h.amount()>0 && !h.fatal()));
            long count=heard.stream().filter(c -> c==SoundCue.PLAYER_HURT).count();assertEquals(hits.size(),count);
            arena.setInvincible(true);for(int i=0;i<150;i++)arena.update(.02,0,false);
            assertEquals(count,heard.stream().filter(c -> c==SoundCue.PLAYER_HURT).count());
            arena.setInvincible(false);arena.player().increaseDefense(10000);
            for(int i=0;i<150;i++)arena.update(.02,0,false);
            assertEquals(count,heard.stream().filter(c -> c==SoundCue.PLAYER_HURT).count(),"Fully absorbed hits must stay silent");
        }
    }
    @Test void lordPhaseRitualAndDefeatHaveDistinctSingleCues() {
        List<SoundCue> heard=new ArrayList<>();var events=new GameEventBus();
        try(var audio=new CombatAudio(events,heard::add)) {
            var arena=new ArenaSimulation(CharacterBuilder.warrior("Hero").damage(10000).build(),events);
            arena.setInvincible(true);arena.start(1,false);
            arena.fighters().forEach(f -> f.enemy.takeDamage(f.enemy.getMaxHealth()));arena.update(.02,0,false);
            arena.start(6,true);var lord=arena.fighters().get(0);
            lord.enemy.takeDamage((int)(lord.enemy.getMaxHealth()*.75));
            for(int i=0;i<750;i++)arena.update(.02,0,false);
            assertEquals(1,heard.stream().filter(c -> c==SoundCue.PHASE_CHANGE).count());
            assertEquals(1,heard.stream().filter(c -> c==SoundCue.SUMMON).count());
            arena.hero().x=lord.body.x-50;arena.hero().y=lord.body.y;arena.attack();
            for(int i=0;i<25;i++)arena.update(.02,0,false);
            assertFalse(lord.enemy.isAlive());assertEquals(1,heard.stream().filter(c -> c==SoundCue.BOSS_DOWN).count());
        }
    }
    @Test void fatalBlowPlaysOneDeathVoiceWithoutOverlappingTheHurtVoice() {
        List<SoundCue> heard=new ArrayList<>();var events=new GameEventBus();
        try(var audio=new CombatAudio(events,heard::add)) {
            var arena=new ArenaSimulation(CharacterBuilder.warrior("Hero").maxHealth(1).defense(0).build(),events);arena.start(1,false);arena.hero().x=1400;
            for(int i=0;i<400;i++)arena.update(.02,0,false);
            assertFalse(arena.player().isAlive());assertEquals(1,heard.stream().filter(c -> c==SoundCue.PLAYER_DEATH).count());
            assertFalse(heard.contains(SoundCue.PLAYER_HURT));
        }
    }
    @Test void clearingCampaignWavesAndGainingLevelsProduceSingleEventCues() {
        var inventory=new com.gamelab.domain.inventory.Inventory(16);
        var weapon=com.gamelab.domain.item.WeaponCatalog.createStarterSword();inventory.addItem(weapon);
        var run=new com.gamelab.application.arena.CampaignRun(CharacterBuilder.warrior("Hero").build(),inventory,
                new com.gamelab.domain.economy.CoinWallet(),new com.gamelab.domain.rune.RuneLoadout(weapon));
        List<SoundCue> heard=new ArrayList<>();
        try(var audio=new CombatAudio(run.events(),heard::add)) {
            run.simulation().setInvincible(true);run.enterNextStage();
            for(int wave=1;wave<=3;wave++) {
                run.simulation().fighters().forEach(f -> f.enemy.takeDamage(f.enemy.getMaxHealth()));
                for(int i=0;i<105;i++)run.simulation().update(.02,0,false);
            }
            run.simulation().hero().x=1880;run.simulation().update(.02,0,false);run.completeStage();run.completeStage();
            assertEquals(3,heard.stream().filter(c -> c==SoundCue.WAVE_START).count());
            assertEquals(2,heard.stream().filter(c -> c==SoundCue.WAVE_CLEAR).count());
            assertEquals(1,heard.stream().filter(c -> c==SoundCue.STAGE_CLEAR).count());
            assertEquals(1,heard.stream().filter(c -> c==SoundCue.LEVEL_UP).count());
        }
    }
    @Test void missHasSwingAndRealHitsUseTheEnemySurface() {
        List<SoundCue> heard=new ArrayList<>();
        GameEventBus events=new GameEventBus();
        try (CombatAudio audio=new CombatAudio(events,heard::add)) {
            ArenaSimulation arena=new ArenaSimulation(CharacterBuilder.warrior("Hero").damage(25).build(),events);
            arena.start(1,false);
            arena.attack();
            assertEquals(List.of(SoundCue.SWING),heard,"A miss must not clang or make a flesh hit");
            arena.restart(); heard.clear();
            var soldier=arena.fighters().get(1);
            arena.hero().x=soldier.body.x-50; arena.attack();
            for(int i=0;i<11;i++) arena.update(.02,0,false);
            assertEquals(List.of(SoundCue.SWING,SoundCue.HARD),heard);
            arena.start(4,false); heard.clear();
            var slime=arena.fighters().get(0);
            arena.hero().x=slime.body.x-50; arena.attack();
            for(int i=0;i<11;i++) arena.update(.02,0,false);
            assertEquals(List.of(SoundCue.SWING,SoundCue.SLIME),heard);
            assertFalse(arena.attack());
            assertEquals(2,heard.size(),"Cooldown must not produce duplicate sound");
            heard.clear(); arena.woundEnemies();
            assertTrue(heard.isEmpty(),"Diagnostic damage is not a weapon impact");
        }
    }

    @Test void zeroDamageDoesNotProduceAnImpactAndClosingUnsubscribes() {
        List<SoundCue> heard=new ArrayList<>(); GameEventBus events=new GameEventBus();
        CombatAudio audio=new CombatAudio(events,heard::add);
        ArenaSimulation arena=new ArenaSimulation(CharacterBuilder.warrior("Hero").damage(0).build(),events);
        arena.start(1,false); arena.hero().x=arena.fighters().get(1).body.x-50;
        arena.attack(); assertEquals(List.of(SoundCue.SWING),heard);
        audio.close(); audio.close();
        arena.restart(); arena.attack();
        assertEquals(1,heard.size(),"Detached screens must not keep receiving sounds");
    }

    @Test void suppliedAudioDecodesAsStereoPcmWithoutClipping() throws Exception {
        for (SoundCue cue:SoundCue.values()) for (String file:cue.files()) {
            var stream=getClass().getResourceAsStream(SoundCue.ROOT+file);
            assertNotNull(stream,file);
            try (var input=AudioSystem.getAudioInputStream(new BufferedInputStream(stream))) {
                assertEquals(AudioFormat.Encoding.PCM_SIGNED,input.getFormat().getEncoding());
                assertEquals(44100,input.getFormat().getSampleRate());
                assertEquals(16,input.getFormat().getSampleSizeInBits());
                assertEquals(2,input.getFormat().getChannels());
                byte[] samples=input.readAllBytes(); assertTrue(samples.length>1000,file);
                int peak=0;
                for (int i=0;i<samples.length;i+=2) peak=Math.max(peak,Math.abs((short)((samples[i]&255)|(samples[i+1]<<8))));
                assertTrue(peak>100 && peak<32700,"Silent or clipped sample: "+file);
            }
        }
    }

    @Test void stagesThreeFourAndGhostsHaveDifferentImpactSounds() {
        String[] ids={"demon","mage","fire-golem","slime","doctor","witch","ghost","ghost-ranged"};
        SoundCue[] expected={SoundCue.DEMON,SoundCue.MYSTIC,SoundCue.HARD,SoundCue.SLIME,SoundCue.MYSTIC,
                SoundCue.MYSTIC,SoundCue.ETHEREAL,SoundCue.ETHEREAL};
        for (int i=0;i<ids.length;i++) {
            List<SoundCue> heard=new ArrayList<>(); GameEventBus events=new GameEventBus();
            try (var audio=new CombatAudio(events,heard::add)) {
                var profile=com.gamelab.application.arena.ArenaCatalog.byId(ids[i]);
                events.publish("WEAPON_IMPACT","Hit",new com.gamelab.application.audio.WeaponImpact(profile.hitSurface(),10));
                assertEquals(List.of(expected[i]),heard,ids[i]);
            }
        }
    }

    @Test void enemyAttackAudioWaitsForReleaseAndUsesGhostOrElementalSound() {
        List<SoundCue> heard=new ArrayList<>(); GameEventBus events=new GameEventBus();
        try (var audio=new CombatAudio(events,heard::add)) {
            var arena=new ArenaSimulation(CharacterBuilder.warrior("Hero").maxHealth(2000).build(),events);
            arena.setInvincible(true); arena.start(3,false); arena.hero().x=1500;
            for (int i=0;i<50;i++) arena.update(.02,0,false);
            assertTrue(heard.isEmpty(),"Telegraphs do not release fire audio");
            for (int i=0;i<100;i++) arena.update(.02,0,false);
            assertTrue(heard.contains(SoundCue.FIRE));
            heard.clear(); events.publish("ENEMY_ATTACK","Ghost attack","ghost");
            assertEquals(List.of(SoundCue.ETHEREAL),heard);
            heard.clear(); events.publish("ENEMY_ATTACK","Poison attack","doctor");
            assertEquals(List.of(SoundCue.POISON),heard);
        }
    }
}
