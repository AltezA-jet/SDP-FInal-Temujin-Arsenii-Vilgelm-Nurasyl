package com.gamelab.application;

import com.gamelab.domain.rune.RuneLoadout;

import java.util.Objects;

public record WeaponUpgradeResult(
        boolean success,
        String message,
        RuneLoadout runeLoadout
) {

    public WeaponUpgradeResult {
        Objects.requireNonNull(
                message,
                "Message cannot be null"
        );

        Objects.requireNonNull(
                runeLoadout,
                "Rune loadout cannot be null"
        );
    }

    public static WeaponUpgradeResult successful(
            String message,
            RuneLoadout runeLoadout
    ) {
        return new WeaponUpgradeResult(
                true,
                message,
                runeLoadout
        );
    }

    public static WeaponUpgradeResult failed(
            String message,
            RuneLoadout runeLoadout
    ) {
        return new WeaponUpgradeResult(
                false,
                message,
                runeLoadout
        );
    }
}