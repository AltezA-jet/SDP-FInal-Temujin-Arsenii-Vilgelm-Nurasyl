package com.gamelab.application.arena;

/** Shared tuning for playable stages and the sandbox; summoned enemies retain their weaker base stats. */
public final class ArenaBalance {
    public static int health(ArenaCatalog.Profile profile,int base) {
        return (int)Math.round(base*(profile.attack()==ArenaCatalog.Attack.FINAL?1.50:profile.boss()?1.40:1.15));
    }
    public static int damage(ArenaCatalog.Profile profile,int base) {
        return (int)Math.round(base*(profile.boss() && profile.attack()!=ArenaCatalog.Attack.FINAL?1.20:1.25));
    }
    public static double attackCooldown(ArenaCatalog.Profile profile,int phase,boolean heavy) {
        if(profile.attack()==ArenaCatalog.Attack.FINAL) return heavy?1.5:phase>=2?.85:1.1;
        return profile.boss()?1.55:profile.ranged()?1.30:.72;
    }
    public static double lordSummonInterval(java.util.Random random) { return 4.5+random.nextDouble()*2; }
    private ArenaBalance() { }
}
