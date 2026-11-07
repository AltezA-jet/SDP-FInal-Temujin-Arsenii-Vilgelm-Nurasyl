package com.gamelab.ui.audio;

@FunctionalInterface
public interface SoundPlayer extends AutoCloseable {
    void play(SoundCue cue);
    default void setVolume(double volume) { }
    default void stop() { }
    @Override default void close() { }
}
