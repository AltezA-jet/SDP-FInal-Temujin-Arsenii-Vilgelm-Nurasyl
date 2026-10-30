package com.gamelab.pattern.observer;

@FunctionalInterface
public interface GameObserver { void onGameEvent(GameEvent event); }
