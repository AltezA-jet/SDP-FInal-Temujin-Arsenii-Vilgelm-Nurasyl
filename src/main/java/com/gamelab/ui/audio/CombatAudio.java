package com.gamelab.ui.audio;

import com.gamelab.application.audio.WeaponImpact;
import com.gamelab.pattern.observer.*;
import java.util.Objects;

/** Observer: sound follows successful combat events, without dependencies in the simulation. */
public final class CombatAudio implements GameObserver, AutoCloseable {
    private final GameEventBus events;
    private final SoundPlayer player;
    private boolean closed;
    public CombatAudio(GameEventBus events,SoundPlayer player) {
        this.events=Objects.requireNonNull(events); this.player=Objects.requireNonNull(player);
        events.subscribe(this);
    }
    @Override public void onGameEvent(GameEvent event) {
        if (closed) return;
        SoundCue eventCue=switch(event.type()) {
            case "PLAYER_HIT" -> event.payload() instanceof com.gamelab.application.audio.PlayerDamage hit
                    ? hit.fatal()?null:SoundCue.PLAYER_HURT
                    : event.payload() instanceof Number damage && damage.intValue()>0 ? SoundCue.PLAYER_HURT:null;
            case "PLAYER_DIED" -> SoundCue.PLAYER_DEATH;
            case "PLAYER_JUMP" -> SoundCue.JUMP;
            case "PLAYER_DODGE" -> SoundCue.DODGE;
            case "PLAYER_LANDED" -> SoundCue.LAND;
            case "PLAYER_STEP" -> SoundCue.STEP;
            case "ENEMY_KILLED" -> SoundCue.ENEMY_DOWN;
            case "BOSS_DEFEATED" -> SoundCue.BOSS_DOWN;
            case "ARENA_WAVE" -> SoundCue.WAVE_START;
            case "ARENA_WAVE_CLEARED" -> SoundCue.WAVE_CLEAR;
            case "ARENA_EXIT_READY" -> SoundCue.STAGE_CLEAR;
            case "PLAYER_LEVEL_UP" -> SoundCue.LEVEL_UP;
            case "POTION_USED" -> SoundCue.HEAL;
            case "SUMMON" -> SoundCue.SUMMON;
            case "BOSS_PHASE_CHANGED" -> SoundCue.PHASE_CHANGE;
            default -> null;
        };
        if(eventCue!=null) player.play(eventCue);
        if (event.type().equals("PLAYER_ATTACK")) player.play(SoundCue.SWING);
        if (event.type().equals("WEAPON_IMPACT") && event.payload() instanceof WeaponImpact impact && impact.damage()>0)
            player.play(switch (impact.surface()) {
                case FLESH -> SoundCue.FLESH;
                case HARD -> SoundCue.HARD;
                case DEMON -> SoundCue.DEMON;
                case SLIME -> SoundCue.SLIME;
                case MYSTIC -> SoundCue.MYSTIC;
                case ETHEREAL -> SoundCue.ETHEREAL;
            });
        if (event.type().equals("ENEMY_ATTACK") && event.payload() instanceof String id) {
            switch (id) {
                case "ghost", "ghost-ranged" -> player.play(SoundCue.ETHEREAL);
                case "demon", "mage", "fire-golem" -> player.play(SoundCue.FIRE);
                case "doctor", "witch", "poison-slime" -> player.play(SoundCue.POISON);
                case "slime" -> player.play(SoundCue.SLIME);
                case "soldier", "skeleton", "shadow-knight", "shadow-lord" -> player.play(SoundCue.SWING);
                case "archer" -> player.play(SoundCue.SHOT);
                case "guardian", "ice-golem" -> player.play(SoundCue.STONE_CAST);
                case "elemental" -> player.play(SoundCue.ICE_CAST);
                default -> { }
            }
        }
    }
    public void setVolume(double volume) { player.setVolume(volume); }
    public void stop() { player.stop(); }
    @Override public void close() {
        if (closed) return;
        closed=true; events.unsubscribe(this); player.close();
    }
}
