package com.gamelab.application.arena;

import com.gamelab.pattern.factory.EnemyType;
import java.util.List;

/** Playable roster from All for final sdp/Тутор.txt; legacy campaign types remain compatible. */
public final class ArenaCatalog {
    public enum Attack { MELEE, SHOT, FIREBALL, POISON, STONE, ICE_FAN, ROLL, WITCH, CHARGE, FINAL }

    public record Profile(String id, String name, int stage, EnemyType type, Attack attack,
                          double range, String spriteFolder, String spriteName, int frameWidth, int frameHeight) {
        public boolean boss() { return type.boss; }
        public boolean ranged() { return attack == Attack.SHOT || attack == Attack.FIREBALL || attack == Attack.POISON; }
    }

    private static Profile profile(String id, String name, int stage, EnemyType type, Attack attack,
                                   double range, String folder, String sprite, int w, int h) {
        return new Profile(id, name, stage, type, attack, range, folder, sprite, w, h);
    }

    public static final List<Profile> ALL = List.of(
        profile("archer", "Лучница", 1, EnemyType.RUIN_ARCHER, Attack.SHOT, 580, "1 Этап/Archer", "ruin_archer", 64, 96),
        profile("soldier", "Солдат", 1, EnemyType.RUIN_SOLDIER, Attack.MELEE, 90, "1 Этап/Soldier", "swordsman", 64, 96),
        profile("guardian", "Рунный страж", 1, EnemyType.RUNE_GUARDIAN, Attack.STONE, 850, "1 Этап/Rune_Guardian(miniBoss)", "rune_guardian", 96, 128),
        profile("ice-beast", "Ледяная собака", 2, EnemyType.ICE_CRAWLER, Attack.MELEE, 85, "2 Этап/ice_beast", "ice_beast", 96, 64),
        profile("elemental", "Ледяной элементаль", 2, EnemyType.ICE_ELEMENTAL, Attack.SHOT, 620, "2 Этап/ice_elemental", "ice_elemental", 64, 96),
        profile("ice-golem", "Ледяной голем", 2, EnemyType.FROST_GOLEM, Attack.ICE_FAN, 900, "2 Этап/ice_golem(miniBoss)", "ice_golem", 96, 128),
        profile("demon", "Демон", 3, EnemyType.FIRE_IMP, Attack.MELEE, 100, "3 Этап/fire_demon", "fire_demon", 64, 96),
        profile("mage", "Огненный маг", 3, EnemyType.FIRE_MAGE, Attack.FIREBALL, 650, "3 Этап/fire_mag", "fire_mage", 64, 96),
        profile("fire-golem", "Огненный голем", 3, EnemyType.INFERNO_GOLEM, Attack.ROLL, 1000, "", "", 96, 128),
        profile("slime", "Слизень", 4, EnemyType.TOXIC_SLIME, Attack.MELEE, 75, "4 Этап/slime", "green_slime", 64, 64),
        profile("doctor", "Вичдоктор", 4, EnemyType.POISON_SHAMAN, Attack.POISON, 620, "", "", 64, 96),
        profile("witch", "Ведьма", 4, EnemyType.PLAGUE_QUEEN, Attack.WITCH, 850, "", "", 96, 128),
        profile("ghost", "Призрак", 5, EnemyType.WRAITH, Attack.MELEE, 100, "", "", 64, 96),
        profile("ghost-ranged", "Призрак-стрелок", 5, EnemyType.NECROMANCER, Attack.SHOT, 680, "", "", 64, 96),
        profile("shadow-knight", "Теневой рыцарь", 5, EnemyType.SHADOW_KNIGHT, Attack.CHARGE, 1000, "", "", 96, 128),
        profile("shadow-lord", "Повелитель теней", 6, EnemyType.SHADOW_LORD, Attack.FINAL, 1000, "", "", 96, 128)
    );

    private ArenaCatalog() { }
    public static Profile byId(String id) {
        return ALL.stream().filter(p -> p.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown enemy: " + id));
    }
    public static List<Profile> stage(int stage, boolean bosses) {
        if (stage < 1 || stage > 6) throw new IllegalArgumentException("Stage must be 1–6");
        return ALL.stream().filter(p -> p.stage() == stage && p.boss() == bosses).toList();
    }
}
