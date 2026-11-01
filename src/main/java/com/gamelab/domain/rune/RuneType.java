package com.gamelab.domain.rune;

public enum RuneType {

    FIRE(RuneCategory.SPECIAL),
    ICE(RuneCategory.SPECIAL),
    PURITY(RuneCategory.SPECIAL),
    HOLY(RuneCategory.SPECIAL),
    ANCIENT(RuneCategory.SPECIAL),

    POISON(RuneCategory.COMMON),
    VAMPIRE(RuneCategory.COMMON),
    LIGHTNING(RuneCategory.COMMON),
    FURY(RuneCategory.COMMON),
    HASTE(RuneCategory.COMMON),
    CRITICAL(RuneCategory.COMMON);

    private final RuneCategory category;

    RuneType(RuneCategory category) {
        this.category = category;
    }

    public RuneCategory getCategory() {
        return category;
    }

    public boolean isSpecial() {
        return category == RuneCategory.SPECIAL;
    }
}