package com.gamelab.pattern.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class GameLogObserver
        implements GameObserver {

    private final CopyOnWriteArrayList<String> entries =
            new CopyOnWriteArrayList<>();

    @Override
    public void onGameEvent(GameEvent event) {
        String entry =
                "[" + event.type() + "] "
                        + event.message();

        entries.add(entry);
        System.out.println(entry);
    }

    public List<String> getEntries() {
        return List.copyOf(entries);
    }

    public int getEntryCount() {
        return entries.size();
    }

    public void clear() {
        entries.clear();
    }
}