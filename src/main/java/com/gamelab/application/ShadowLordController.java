package com.gamelab.application;

import com.gamelab.domain.ShadowLordPhase;
import com.gamelab.pattern.strategy.BossBehavior;
import java.util.Objects;

/** Tracks Shadow Lord phase transitions; scene effects are executed by the game/UI layer. */
public final class ShadowLordController {
    private ShadowLordPhase phase = ShadowLordPhase.FROZEN_ARMOR;

    public ShadowLordPhase getPhase() { return phase; }

    public ShadowLordPhase advancePhase() {
        phase = switch (phase) {
            case FROZEN_ARMOR -> ShadowLordPhase.BURNING_RAGE;
            case BURNING_RAGE -> ShadowLordPhase.TRUE_SHADOW;
            case TRUE_SHADOW -> ShadowLordPhase.TRUE_SHADOW;
        };
        return phase;
    }

    public BossBehavior.Ability getSignatureAbility() {
        return switch (phase) {
            case FROZEN_ARMOR -> BossBehavior.Ability.ICE_PROJECTILE;
            case BURNING_RAGE -> BossBehavior.Ability.FIRE_ZONE;
            case TRUE_SHADOW -> BossBehavior.Ability.SHADOW_CLONE;
        };
    }
}
