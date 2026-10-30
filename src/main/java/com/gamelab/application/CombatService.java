package com.gamelab.application;

import com.gamelab.domain.Enemy;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.observer.GameEventTypes;
import com.gamelab.pattern.strategy.PlayerCombatStrategy;
import java.util.Objects;

/** Coordinates player attacks, enemy damage, combat events and optional kill rewards. */
public final class CombatService {
    private final GameEventBus events;
    private final RewardService rewards;
    private final CoinWallet wallet;
    public CombatService(GameEventBus events) { this(events, null, null); }
    public CombatService(GameEventBus events, RewardService rewards, CoinWallet wallet) {
        this.events = Objects.requireNonNull(events, "Event bus cannot be null");
        if ((rewards == null) != (wallet == null)) throw new IllegalArgumentException("Reward service and wallet must be provided together");
        this.rewards = rewards; this.wallet = wallet;
    }
    /** Returns actual damage dealt. Rewards are granted once, on the living-to-dead transition. */
    public int attack(PlayerCharacter player, Enemy enemy, PlayerCombatStrategy strategy) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(enemy, "Enemy cannot be null");
        Objects.requireNonNull(strategy, "Combat strategy cannot be null");
        if (!player.isAlive() || !enemy.isAlive()) return 0;
        int damage = strategy.calculateDamage(player);
        if (damage < 0) throw new IllegalStateException("Combat strategy returned negative damage");
        int before = enemy.getHealth();
        enemy.takeDamage(damage);
        int dealt = before - enemy.getHealth();
        events.publish(GameEventTypes.COMBAT_HIT, player.getName() + " hit " + enemy.getName() + " for " + dealt);
        if (!enemy.isAlive()) {
            if (rewards != null && enemy.getRewardCoins() > 0 && enemy.getRewardExperience() > 0) {
                rewards.grantEnemyReward(enemy.getName(), enemy.getRewardCoins(), enemy.getRewardExperience(), wallet, player);
            } else {
                events.publish(GameEventTypes.ENEMY_KILLED, enemy.getName() + " was defeated");
            }
        }
        return dealt;
    }
    public int damageEnemy(Enemy enemy, int damage) {
        Objects.requireNonNull(enemy, "Enemy cannot be null");
        if (damage < 0) throw new IllegalArgumentException("Damage cannot be negative");
        if (!enemy.isAlive()) return 0;
        int before = enemy.getHealth(); enemy.takeDamage(damage); int dealt = before - enemy.getHealth();
        events.publish(GameEventTypes.COMBAT_HIT, enemy.getName() + " took " + dealt + " damage");
        if (!enemy.isAlive()) events.publish(GameEventTypes.ENEMY_KILLED, enemy.getName() + " was defeated");
        return dealt;
    }
}
