package com.gamelab.application.arena;

import com.gamelab.pattern.factory.EnemyType;
import java.util.List;

/** Playable roster from All for final sdp/Тутор.txt; legacy campaign types remain compatible. */
public final class ArenaCatalog {
    public enum Attack { MELEE, SHOT, FIREBALL, POISON, STONE, ICE_FAN, ROLL, WITCH, CHARGE, FINAL, SUMMON, LEAP }

    public record Profile(String id, String name, int stage, EnemyType type, Attack attack,
                          double range, String spriteFolder, String spriteName, int frameWidth, int frameHeight) {
        public boolean boss() { return type.boss; }
        public boolean ranged() { return attack == Attack.SHOT || attack == Attack.FIREBALL || attack == Attack.POISON || attack == Attack.SUMMON; }
        public com.gamelab.application.audio.WeaponImpact.Surface hitSurface() {
            return switch (id) {
                case "demon" -> com.gamelab.application.audio.WeaponImpact.Surface.DEMON;
                case "slime" -> com.gamelab.application.audio.WeaponImpact.Surface.SLIME;
                case "mage", "doctor", "witch" -> com.gamelab.application.audio.WeaponImpact.Surface.MYSTIC;
                case "ghost", "ghost-ranged" -> com.gamelab.application.audio.WeaponImpact.Surface.ETHEREAL;
                case "soldier", "guardian", "elemental", "ice-golem", "fire-golem", "shadow-knight", "shadow-lord" ->
                        com.gamelab.application.audio.WeaponImpact.Surface.HARD;
                default -> com.gamelab.application.audio.WeaponImpact.Surface.FLESH;
            };
        }
    }

    private static Profile profile(String id, String name, int stage, EnemyType type, Attack attack,
                                   double range, String folder, String sprite, int w, int h) {
        return new Profile(id, name, stage, type, attack, range, folder, sprite, w, h);
    }

    public static final List<Profile> ALL = List.of(
        profile("archer", "Лучница", 1, EnemyType.RUIN_ARCHER, Attack.SHOT, 580, "1 Этап/Archer", "ruin_archer", 64, 96),
        profile("soldier", "Солдат", 1, EnemyType.RUIN_SOLDIER, Attack.MELEE, 90, "1 Этап/Soldier", "swordsman", 64, 96),
        profile("guardian", "Рунный страж", 1, EnemyType.RUNE_GUARDIAN, Attack.STONE, 850, "1 Этап/Rune_Guardian(miniBoss)", "rune_guardian", 96, 128),
        profile("ice-beast", "Ледяной зверь", 2, EnemyType.ICE_CRAWLER, Attack.LEAP, 180, "2 Этап/ice_beast", "ice_beast", 96, 64),
        profile("elemental", "Ледяной элементаль", 2, EnemyType.ICE_ELEMENTAL, Attack.SHOT, 620, "2 Этап/ice_elemental", "ice_elemental", 64, 96),
        profile("ice-golem", "Ледяной голем", 2, EnemyType.FROST_GOLEM, Attack.ICE_FAN, 900, "2 Этап/ice_golem(miniBoss)", "ice_golem", 96, 128),
        profile("demon", "Демон", 3, EnemyType.FIRE_IMP, Attack.LEAP, 180, "3 Этап/fire_demon", "fire_demon", 64, 96),
        profile("mage", "Огненный маг", 3, EnemyType.FIRE_MAGE, Attack.FIREBALL, 650, "3 Этап/fire_mag", "fire_mage", 64, 96),
        profile("fire-golem", "Лавовый голем", 3, EnemyType.INFERNO_GOLEM, Attack.ROLL, 1400, "3 Этап/fire_golem(miniBoss)", "lava_golem", 96, 128),
        profile("slime", "Зелёный слайм", 4, EnemyType.TOXIC_SLIME, Attack.LEAP, 180, "4 Этап/slime", "green_slime", 64, 64),
        profile("doctor", "Чумной маг", 4, EnemyType.POISON_SHAMAN, Attack.POISON, 620, "4 Этап/plague_mage", "plague_mage", 64, 96),
        profile("witch", "Чумная королева", 4, EnemyType.PLAGUE_QUEEN, Attack.WITCH, 850, "4 Этап/Queen(miniBoss)", "queen", 96, 128),
        profile("ghost", "Тёмный призрак", 5, EnemyType.WRAITH, Attack.LEAP, 180, "5 Этап/Dark_ghost", "dark_ghost", 64, 96),
        profile("ghost-ranged", "Тёмный маг", 5, EnemyType.NECROMANCER, Attack.SUMMON, 680, "5 Этап/dark_mage", "dark_mage", 64, 96),
        profile("shadow-knight", "Тёмный король", 5, EnemyType.SHADOW_KNIGHT, Attack.CHARGE, 1200, "5 Этап/dark_king(miniBoss)", "dark_king", 96, 128),
        profile("shadow-lord", "Тёмный Лорд", 6, EnemyType.SHADOW_LORD, Attack.FINAL, 1000, "Final_Boss(Lord)", "lord", 160, 192)
    );

    public static final Profile SKELETON = profile("skeleton", "Скелет-мечник", 5, EnemyType.RUIN_SOLDIER,
            Attack.MELEE, 90, "5 Этап/dark_mage", "skeleton", 64, 96);
    public static final Profile POISON_SLIME = profile("poison-slime", "Ядовитый слайм", 4, EnemyType.TOXIC_SLIME,
            Attack.LEAP, 180, "4 Этап/Queen(miniBoss)", "poison_slime", 64, 64);

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
