package com.gamelab;

import com.gamelab.pattern.strategy.BalancedStrategy;
import com.gamelab.application.CombatService;
import com.gamelab.application.RewardService;
import com.gamelab.application.RuneCombatService;
import com.gamelab.domain.Enemy;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.item.WeaponCatalog;
import com.gamelab.domain.rune.RuneCatalog;
import com.gamelab.domain.rune.RuneLoadout;
import com.gamelab.domain.rune.RuneType;
import com.gamelab.pattern.factory.EnemyFactory;
import com.gamelab.pattern.factory.EnemyType;
import com.gamelab.pattern.observer.GameEventBus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuneCombatServiceTest {

    @Test
    void shouldApplyFireBonusAgainstIceEnemy() {
        GameEventBus eventBus = new GameEventBus();

        RuneCombatService service =
                new RuneCombatService(
                        new CombatService(eventBus),
                        () -> 1.0
                );

        PlayerCharacter player =
                createPlayer();

        Enemy enemy = EnemyFactory.create(
                EnemyType.ICE_CRAWLER,
                "ice-1",
                0,
                0
        );

        RuneLoadout loadout =
                new RuneLoadout(
                        WeaponCatalog.createByLevel(1)
                );

        loadout.installRune(
                RuneCatalog.create(RuneType.FIRE)
        );

        int damage = service.attack(
                player,
                enemy,
                new BalancedStrategy(),
                loadout
        );

        assertEquals(28, damage);
        assertEquals(54, enemy.getHealth());
    }

    @Test
    void shouldApplyCriticalDamageWhenRollSucceeds() {
        GameEventBus eventBus = new GameEventBus();

        RuneCombatService service =
                new RuneCombatService(
                        new CombatService(eventBus),
                        () -> 0.0
                );

        Enemy enemy = EnemyFactory.create(
                EnemyType.RUIN_SOLDIER,
                "normal-1",
                0,
                0
        );

        RuneLoadout loadout =
                new RuneLoadout(
                        WeaponCatalog.createByLevel(1)
                );

        loadout.installRune(
                RuneCatalog.create(RuneType.CRITICAL)
        );

        int damage = service.attack(
                createPlayer(),
                enemy,
                new BalancedStrategy(),
                loadout
        );

        assertEquals(36, damage);
    }

    @Test
    void vampireRuneShouldHealPlayer() {
        GameEventBus eventBus = new GameEventBus();

        RuneCombatService service =
                new RuneCombatService(
                        new CombatService(eventBus),
                        () -> 1.0
                );

        PlayerCharacter player =
                createPlayer();

        player.takeDamage(50);

        Enemy enemy = EnemyFactory.create(
                EnemyType.RUIN_SOLDIER,
                "normal-2",
                0,
                0
        );

        RuneLoadout loadout =
                new RuneLoadout(
                        WeaponCatalog.createByLevel(1)
                );

        loadout.installRune(
                RuneCatalog.create(RuneType.VAMPIRE)
        );

        int damage = service.attack(
                player,
                enemy,
                new BalancedStrategy(),
                loadout
        );

        assertEquals(22, damage);
        assertEquals(54, player.getHealth());
    }

    @Test
    void shouldPreserveEnemyKillRewards() {
        GameEventBus eventBus = new GameEventBus();
        CoinWallet wallet = new CoinWallet();

        RewardService rewardService =
                new RewardService(eventBus);

        CombatService combatService =
                new CombatService(
                        eventBus,
                        rewardService,
                        wallet
                );

        RuneCombatService service =
                new RuneCombatService(
                        combatService,
                        () -> 1.0
                );

        PlayerCharacter player =
                createPlayer();
        player.increaseDamage(90);

        Enemy enemy = EnemyFactory.create(
                EnemyType.RUIN_ARCHER,
                "archer-1",
                0,
                0
        );

        RuneLoadout loadout =
                new RuneLoadout(
                        WeaponCatalog.createByLevel(1)
                );

        int damage = service.attack(
                player,
                enemy,
                new BalancedStrategy(),
                loadout
        );

        assertEquals(48, damage);
        assertEquals(5, wallet.getBalance());
        assertEquals(18, player.getExperience());
    }

    private PlayerCharacter createPlayer() {
        return new PlayerCharacter(
                "Arthur",
                "KNIGHT",
                100,
                10,
                0
        );
    }
}