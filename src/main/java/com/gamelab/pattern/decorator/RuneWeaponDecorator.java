package com.gamelab.pattern.decorator;

import com.gamelab.domain.Enemy;
import com.gamelab.domain.EnemyElement;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneType;

import java.util.Objects;

public final class RuneWeaponDecorator
        extends WeaponDecorator {

    private final Rune rune;

    public RuneWeaponDecorator(
            Weapon wrappedWeapon,
            Rune rune
    ) {
        super(wrappedWeapon);

        this.rune = Objects.requireNonNull(
                rune,
                "Rune cannot be null"
        );
    }

    @Override
    public String getName() {
        return wrappedWeapon.getName()
                + " + " + rune.getName();
    }

    @Override
    public String getDescription() {
        return wrappedWeapon.getDescription()
                + " Installed rune: "
                + rune.getName() + ".";
    }

    @Override
    public int getDamageBonus() {
        return wrappedWeapon.getDamageBonus()
                + getNormalRuneDamage();
    }

    @Override
    public int getDamageBonusAgainst(
            Enemy enemy
    ) {
        Objects.requireNonNull(
                enemy,
                "Enemy cannot be null"
        );

        return wrappedWeapon
                .getDamageBonusAgainst(enemy)
                + getRuneDamageAgainst(enemy);
    }

    @Override
    public double getAttackSpeed() {
        return switch (rune.getRuneType()) {
            case HASTE ->
                    wrappedWeapon.getAttackSpeed() * 1.20;

            case FURY ->
                    wrappedWeapon.getAttackSpeed() * 0.90;

            default ->
                    wrappedWeapon.getAttackSpeed();
        };
    }

    public Rune getRune() {
        return rune;
    }

    private int getNormalRuneDamage() {
        return switch (rune.getRuneType()) {
            case FIRE, ICE -> 4;
            case PURITY -> 3;
            case HOLY -> 4;
            case ANCIENT -> 5;
            case POISON -> 3;
            case VAMPIRE -> 2;
            case LIGHTNING, CRITICAL -> 4;
            case FURY -> 6;
            case HASTE -> 0;
        };
    }

    private int getRuneDamageAgainst(
            Enemy enemy
    ) {
        RuneType type = rune.getRuneType();
        EnemyElement element = enemy.getElement();

        return switch (type) {
            case FIRE -> {
                if (element == EnemyElement.ICE) {
                    yield 8;
                }

                if (element == EnemyElement.FIRE) {
                    yield 1;
                }

                yield 4;
            }

            case ICE -> {
                if (element == EnemyElement.FIRE) {
                    yield 8;
                }

                if (element == EnemyElement.ICE) {
                    yield 1;
                }

                yield 4;
            }

            case POISON ->
                    element == EnemyElement.POISON
                            ? 0 : 3;

            case PURITY ->
                    element == EnemyElement.POISON
                            || element
                            == EnemyElement.CORRUPTED
                            ? 8 : 3;

            case HOLY ->
                    element == EnemyElement.UNDEAD
                            || element
                            == EnemyElement.SHADOW
                            ? 9 : 4;

            case ANCIENT ->
                    enemy.isBoss() ? 10 : 5;

            case VAMPIRE -> 2;
            case LIGHTNING, CRITICAL -> 4;
            case FURY -> 6;
            case HASTE -> 0;
        };
    }
}