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
            assertEquals(List.of(SoundCue.SWING,SoundCue.HARD),heard);
            arena.start(4,false); heard.clear();
            var slime=arena.fighters().get(0);
            arena.hero().x=slime.body.x-50; arena.attack();
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
