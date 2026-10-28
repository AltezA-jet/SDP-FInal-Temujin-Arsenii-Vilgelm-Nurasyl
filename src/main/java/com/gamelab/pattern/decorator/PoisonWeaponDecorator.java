package com.gamelab.pattern.decorator;

import com.gamelab.domain.item.Weapon;

public final class PoisonWeaponDecorator extends WeaponDecorator {

    private static final int POISON_DAMAGE_BONUS = 3;
    private static final int POISON_PRICE_BONUS = 40;

    public PoisonWeaponDecorator(Weapon wrappedWeapon) {
        super(wrappedWeapon);
    }

    @Override
    public String getName() {
        return "Poisoned " + wrappedWeapon.getName();
    }

    @Override
    public String getDescription() {
        return wrappedWeapon.getDescription()
                + " Applies a poison effect to enemies.";
    }

    @Override
    public int getPrice() {
        return wrappedWeapon.getPrice()
                + POISON_PRICE_BONUS;
    }

    @Override
    public int getDamageBonus() {
        return wrappedWeapon.getDamageBonus()
                + POISON_DAMAGE_BONUS;
    }
}