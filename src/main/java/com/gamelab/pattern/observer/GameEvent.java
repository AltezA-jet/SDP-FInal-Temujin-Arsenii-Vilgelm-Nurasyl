package com.gamelab.pattern.observer;

public record GameEvent(String type, String message, Object payload) {
    public GameEvent(String type, String message) { this(type, message, null); }
}

