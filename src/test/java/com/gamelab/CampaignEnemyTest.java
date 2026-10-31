package com.gamelab;

import com.gamelab.application.CampaignWaves;
import com.gamelab.application.CombatService;
import com.gamelab.domain.*;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.application.RewardService;
import com.gamelab.pattern.factory.*;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.strategy.BalancedStrategy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CampaignEnemyTest {
    @Test void allCampaignArchetypesHaveBalancedStatsAndElements() {
        for (EnemyType type : EnemyType.values()) {
            Enemy enemy = EnemyFactory.create(type, type.name(), 0, 0);
            assertEquals(type.displayName, enemy.getName());
            assertEquals(type.health, enemy.getMaxHealth());
            assertEquals(type.element, enemy.getElement());
            assertEquals(type.boss, enemy.isBoss());
            assertTrue(enemy.getDamage() > 0 && enemy.getRewardCoins() > 0 && enemy.getRewardExperience() > 0);
        }
    }

    @Test void campaignContainsFifteenWavesAndFinalBoss() {
        assertEquals(15, CampaignWaves.all().size());
        assertEquals(5, CampaignWaves.get(1).getTotalEnemies());
        assertEquals(3, CampaignWaves.get(3).getTotalEnemies());
        assertTrue(CampaignWaves.get(15).isBossWave());
        assertEquals(EnemyType.SHADOW_LORD, CampaignWaves.finalBoss());
    }

    @Test void enemyAttackUsesPlayerDefense() {
        PlayerCharacter player = new PlayerCharacter("Knight", "Knight", 100, 10, 5);
        Enemy enemy = EnemyFactory.create(EnemyType.RUIN_SOLDIER, "soldier", 0, 0);
        int lost = new CombatService(new GameEventBus()).attackPlayer(enemy, player);
        assertEquals(7, lost);
        assertEquals(93, player.getHealth());
    }

    @Test void bossRewardIsGrantedOnce() {
        GameEventBus bus = new GameEventBus();
        PlayerCharacter player = new PlayerCharacter("Knight", "Knight", 100, 1000, 0);
        CoinWallet wallet = new CoinWallet();
        CombatService combat = new CombatService(bus, new RewardService(bus), wallet);
        Enemy boss = EnemyFactory.create(EnemyType.RUNE_GUARDIAN, "guardian", 0, 0);
        combat.attack(player, boss, new BalancedStrategy());
        combat.attack(player, boss, new BalancedStrategy());
        assertEquals(45, wallet.getBalance());
        assertEquals(2, player.getLevel());
        assertEquals(10, player.getExperience());
    }
}
