package com.gamelab.pattern.decorator;

import com.gamelab.domain.item.Weapon;

import java.util.Objects;

public abstract class WeaponDecorator implements Weapon {

    protected final Weapon wrappedWeapon;

    protected WeaponDecorator(Weapon wrappedWeapon) {
        this.wrappedWeapon = Objects.requireNonNull(
                wrappedWeapon,
                "Wrapped weapon cannot be null"
        );
    }

    @Override
    public String getName() {
        return wrappedWeapon.getName();
    }

    @Override
    public String getDescription() {
        return wrappedWeapon.getDescription();
    }

    @Override
    public int getPrice() {
        return wrappedWeapon.getPrice();
    }

    @Override
    public String getRequiredClass() {
        return wrappedWeapon.getRequiredClass();
    }

    @Override
    public int getDamageBonus() {
        return wrappedWeapon.getDamageBonus();
    }
}