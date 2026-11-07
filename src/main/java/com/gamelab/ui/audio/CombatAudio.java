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
                case "doctor", "witch" -> player.play(SoundCue.POISON);
                case "slime" -> player.play(SoundCue.SLIME);
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
