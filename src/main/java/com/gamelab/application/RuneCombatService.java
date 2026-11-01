package com.gamelab.application;

import com.gamelab.domain.Enemy;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.rune.RuneLoadout;
import com.gamelab.domain.rune.RuneType;
import com.gamelab.pattern.strategy.PlayerCombatStrategy;

import java.util.Objects;
import java.util.function.DoubleSupplier;

public final class RuneCombatService {

    private static final double CRITICAL_CHANCE = 0.20;
    private static final double CRITICAL_MULTIPLIER = 1.50;
    private static final int VAMPIRE_HEAL_DIVISOR = 5;

    private final CombatService combatService;
    private final DoubleSupplier criticalRoll;

    public RuneCombatService(
            CombatService combatService
    ) {
        this(
                combatService,
                Math::random
        );
    }

    public RuneCombatService(
            CombatService combatService,
            DoubleSupplier criticalRoll
    ) {
        this.combatService = Objects.requireNonNull(
                combatService,
                "Combat service cannot be null"
        );

        this.criticalRoll = Objects.requireNonNull(
                criticalRoll,
                "Critical roll cannot be null"
        );
    }

    public int attack(
            PlayerCharacter player,
            Enemy enemy,
            PlayerCombatStrategy strategy,
            RuneLoadout loadout
    ) {
        Objects.requireNonNull(
                player,
                "Player cannot be null"
        );
        Objects.requireNonNull(
                enemy,
                "Enemy cannot be null"
        );
        Objects.requireNonNull(
                strategy,
                "Combat strategy cannot be null"
        );
        Objects.requireNonNull(
                loadout,
                "Rune loadout cannot be null"
        );

        if (!player.isAlive() || !enemy.isAlive()) {
            return 0;
        }

        int strategyDamage =
                strategy.calculateDamage(player);

        if (strategyDamage < 0) {
            throw new IllegalStateException(
                    "Combat strategy returned negative damage"
            );
        }

        Weapon decoratedWeapon =
                loadout.createDecoratedWeapon();

        int totalDamage =
                strategyDamage
                        + decoratedWeapon
                        .getDamageBonusAgainst(enemy);

        if (hasRune(
                loadout,
                RuneType.CRITICAL
        ) && criticalRoll.getAsDouble()
                < CRITICAL_CHANCE) {
            totalDamage = (int) Math.round(
                    totalDamage
                            * CRITICAL_MULTIPLIER
            );
        }

        int finalDamage = totalDamage;

        PlayerCombatStrategy finalDamageStrategy =
                new PlayerCombatStrategy() {

                    @Override
                    public int calculateDamage(
                            PlayerCharacter ignoredPlayer
                    ) {
                        return finalDamage;
                    }

                    @Override
                    public String getStrategyName() {
                        return "Rune Enhanced";
                    }
                };

        int dealtDamage = combatService.attack(
                player,
                enemy,
                finalDamageStrategy
        );

        if (hasRune(
                loadout,
                RuneType.VAMPIRE
        )) {
            int healing =
                    dealtDamage
                            / VAMPIRE_HEAL_DIVISOR;

            if (healing > 0) {
                player.heal(healing);
            }
        }

        return dealtDamage;
    }

    private boolean hasRune(
            RuneLoadout loadout,
            RuneType runeType
    ) {
        return loadout.getInstalledRunes()
                .stream()
                .anyMatch(rune ->
                        rune.getRuneType()
                                == runeType
                );
    }
}