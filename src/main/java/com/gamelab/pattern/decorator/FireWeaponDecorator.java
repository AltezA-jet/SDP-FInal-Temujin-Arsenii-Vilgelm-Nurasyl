package com.gamelab.pattern.decorator;

import com.gamelab.domain.item.Weapon;

public final class FireWeaponDecorator extends WeaponDecorator {

    private static final int FIRE_DAMAGE_BONUS = 5;
    private static final int FIRE_PRICE_BONUS = 50;

    public FireWeaponDecorator(Weapon wrappedWeapon) {
        super(wrappedWeapon);
    }

    @Override
    public String getName() {
        return "Flaming " + wrappedWeapon.getName();
    }

    @Override
    public String getDescription() {
        return wrappedWeapon.getDescription()
                + " Deals additional fire damage.";
    }

    @Override
    public int getPrice() {
        return wrappedWeapon.getPrice() + FIRE_PRICE_BONUS;
    }

    @Override
    public int getDamageBonus() {
        return wrappedWeapon.getDamageBonus()
                + FIRE_DAMAGE_BONUS;
    }
}