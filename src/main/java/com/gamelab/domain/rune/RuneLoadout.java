package com.gamelab.domain.rune;

import com.gamelab.domain.item.Weapon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class RuneLoadout {

    private final Weapon weapon;
    private final List<Rune> installedRunes;

    public RuneLoadout(Weapon weapon) {
        this.weapon = Objects.requireNonNull(
                weapon,
                "Weapon cannot be null"
        );

        this.installedRunes = new ArrayList<>();
    }

    public void installRune(Rune rune) {
        Objects.requireNonNull(
                rune,
                "Rune cannot be null"
        );

        if (installedRunes.size()
                >= weapon.getRuneSlotCount()) {
            throw new IllegalStateException(
                    "All rune slots are occupied"
            );
        }

        boolean duplicateExists =
                installedRunes.stream()
                        .anyMatch(installed ->
                                installed.getRuneType()
                                        == rune.getRuneType()
                        );

        if (duplicateExists) {
            throw new IllegalStateException(
                    "The same rune cannot be installed twice"
            );
        }

        if (rune.isSpecial()
                && getSpecialRune().isPresent()) {
            throw new IllegalStateException(
                    "Only one special rune can be installed"
            );
        }

        for (Rune installed : installedRunes) {
            if (areIncompatible(
                    installed.getRuneType(),
                    rune.getRuneType()
            )) {
                throw new IllegalStateException(
                        installed.getName()
                                + " is incompatible with "
                                + rune.getName()
                );
            }
        }

        installedRunes.add(rune);
    }

    public Rune removeRune(RuneType runeType) {
        Objects.requireNonNull(
                runeType,
                "Rune type cannot be null"
        );

        Rune rune = installedRunes.stream()
                .filter(installed ->
                        installed.getRuneType() == runeType
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Rune is not installed: "
                                        + runeType
                        )
                );

        installedRunes.remove(rune);
        return rune;
    }

    public List<Rune> removeAllRunes() {
        List<Rune> removedRunes =
                List.copyOf(installedRunes);

        installedRunes.clear();
        return removedRunes;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public List<Rune> getInstalledRunes() {
        return List.copyOf(installedRunes);
    }

    public Optional<Rune> getSpecialRune() {
        return installedRunes.stream()
                .filter(Rune::isSpecial)
                .findFirst();
    }

    public int getAvailableSlotCount() {
        return weapon.getRuneSlotCount()
                - installedRunes.size();
    }

    private boolean areIncompatible(
            RuneType first,
            RuneType second
    ) {
        return matches(
                first,
                second,
                RuneType.PURITY,
                RuneType.POISON
        ) || matches(
                first,
                second,
                RuneType.HOLY,
                RuneType.POISON
        ) || matches(
                first,
                second,
                RuneType.HOLY,
                RuneType.VAMPIRE
        );
    }

    private boolean matches(
            RuneType first,
            RuneType second,
            RuneType expectedFirst,
            RuneType expectedSecond
    ) {
        return first == expectedFirst
                && second == expectedSecond
                || first == expectedSecond
                && second == expectedFirst;
    }
}