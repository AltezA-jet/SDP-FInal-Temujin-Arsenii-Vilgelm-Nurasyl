package com.gamelab.pattern.observer;

public final class GameEventTypes {

    public static final String ITEM_PICKED_UP =
            "ITEM_PICKED_UP";

    public static final String ITEM_PURCHASED =
            "ITEM_PURCHASED";

    public static final String COMBAT_HIT = "COMBAT_HIT";

    public static final String ENEMY_KILLED =
            "ENEMY_KILLED";

    public static final String REWARD_RECEIVED =
            "REWARD_RECEIVED";

    public static final String WAVE_COMPLETED =
            "WAVE_COMPLETED";

    public static final String BOSS_DEFEATED =
            "BOSS_DEFEATED";

    public static final String POTION_USED =
            "POTION_USED";

    public static final String RUNE_INSTALLED =
            "RUNE_INSTALLED";

    public static final String RUNE_REMOVED =
            "RUNE_REMOVED";

    public static final String RUNE_REWARDED =
            "RUNE_REWARDED";

    private GameEventTypes() {
    }
}