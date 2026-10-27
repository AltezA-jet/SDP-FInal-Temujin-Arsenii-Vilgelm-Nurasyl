package com.gamelab.pattern.observer;

import java.util.concurrent.CopyOnWriteArrayList;

/** Observer pattern: UI and other systems subscribe without depending on each other. */
public final class GameEventBus {
    private final CopyOnWriteArrayList<GameObserver> observers = new CopyOnWriteArrayList<>();
    public void subscribe(GameObserver observer) { if (observer != null) observers.addIfAbsent(observer); }
    public void unsubscribe(GameObserver observer) { observers.remove(observer); }
    public void publish(String type, String message) { GameEvent event = new GameEvent(type, message); observers.forEach(o -> o.onGameEvent(event)); }
}
