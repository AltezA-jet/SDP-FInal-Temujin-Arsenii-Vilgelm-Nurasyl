package com.gamelab.domain.progression;

/** Campaign difficulty multipliers from the progression GDD. */
public enum Difficulty {
    EASY(0.7, 0.8, 1.0, 1.0, 0.8, 1.25, true),
    NORMAL(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, false),
    HARD(1.5, 1.2, 1.25, 1.25, 1.15, 1.0, true);

    private final double damage, health, experience, gold, shopPrice, regeneration;
    private final boolean specialCheckpointRule;
    Difficulty(double damage, double health, double experience, double gold, double shopPrice,
               double regeneration, boolean specialCheckpointRule) {
        this.damage=damage; this.health=health; this.experience=experience; this.gold=gold;
        this.shopPrice=shopPrice; this.regeneration=regeneration; this.specialCheckpointRule=specialCheckpointRule;
    }
    public double damageMultiplier(){return damage;}
    public double healthMultiplier(){return health;}
    public double experienceMultiplier(){return experience;}
    public double goldMultiplier(){return gold;}
    public double shopPriceMultiplier(){return shopPrice;}
    public double regenerationMultiplier(){return regeneration;}
    public boolean hasMovingCheckpoints(){return this==EASY;}
    public boolean hasNoCheckpoints(){return this==HARD;}
    public int shopPrice(int base){return (int)Math.round(base*shopPrice);}
    public int reward(int base){return (int)Math.round(base*(this==HARD?gold:1.0));}
}
