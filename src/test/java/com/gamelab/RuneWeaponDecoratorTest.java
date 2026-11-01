package com.gamelab;

import com.gamelab.domain.Enemy;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.item.WeaponCatalog;
import com.gamelab.domain.rune.RuneCatalog;
import com.gamelab.domain.rune.RuneLoadout;
import com.gamelab.domain.rune.RuneType;
import com.gamelab.pattern.factory.EnemyFactory;
import com.gamelab.pattern.factory.EnemyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuneWeaponDecoratorTest {

    @Test
    void fireRuneShouldDealMoreDamageToIceEnemy() {
        RuneLoadout loadout =
                new RuneLoadout(
                        WeaponCatalog.createByLevel(1)
                );

        loadout.installRune(
                RuneCatalog.create(RuneType.FIRE)
        );

        Weapon decoratedWeapon =
                loadout.createDecoratedWeapon();

        Enemy iceEnemy = EnemyFactory.create(
                EnemyType.ICE_CRAWLER,
                "ice-1",
                0,
                0
        );

        Enemy fireEnemy = EnemyFactory.create(
                EnemyType.FIRE_IMP,
                "fire-1",
                0,
                0
        );

        assertEquals(
                18,
                decoratedWeapon
                        .getDamageBonusAgainst(iceEnemy)
        );

        assertEquals(
                11,
                decoratedWeapon
                        .getDamageBonusAgainst(fireEnemy)
        );
    }

    @Test
    void poisonEnemyShouldIgnorePoisonRuneDamage() {
        RuneLoadout loadout =
                new RuneLoadout(
                        WeaponCatalog.createByLevel(1)
                );

        loadout.installRune(
                RuneCatalog.create(RuneType.POISON)
        );

        Weapon decoratedWeapon =
                loadout.createDecoratedWeapon();

        Enemy toxicEnemy = EnemyFactory.create(
                EnemyType.TOXIC_SLIME,
                "poison-1",
                0,
                0
        );

        assertEquals(
                10,
                decoratedWeapon
                        .getDamageBonusAgainst(toxicEnemy)
        );
    }

    @Test
    void ancientRuneShouldDealMoreDamageToBoss() {
        RuneLoadout loadout =
                new RuneLoadout(
                        WeaponCatalog.createByLevel(1)
                );

        loadout.installRune(
                RuneCatalog.create(RuneType.ANCIENT)
        );

        Weapon decoratedWeapon =
                loadout.createDecoratedWeapon();

        Enemy boss = EnemyFactory.create(
                EnemyType.SHADOW_LORD,
                "boss-1",
                0,
                0
        );

        assertEquals(
                20,
                decoratedWeapon
                        .getDamageBonusAgainst(boss)
        );
    }

    @Test
    void multipleRunesShouldBuildDecoratorChain() {
        RuneLoadout loadout =
                new RuneLoadout(
                        WeaponCatalog.createByLevel(2)
                );

        loadout.installRune(
                RuneCatalog.create(RuneType.FIRE)
        );

        loadout.installRune(
                RuneCatalog.create(RuneType.HASTE)
        );

        Weapon decoratedWeapon =
                loadout.createDecoratedWeapon();

        Enemy iceEnemy = EnemyFactory.create(
                EnemyType.FROST_WARRIOR,
                "ice-2",
                0,
                0
        );

        assertEquals(
                20,
                decoratedWeapon
                        .getDamageBonusAgainst(iceEnemy)
        );

        assertEquals(
                1.62,
                decoratedWeapon.getAttackSpeed(),
                0.001
        );

        assertTrue(
                decoratedWeapon.getName()
                        .contains("Fire Rune")
        );

        assertTrue(
                decoratedWeapon.getName()
                        .contains("Haste Rune")
        );
    }
}