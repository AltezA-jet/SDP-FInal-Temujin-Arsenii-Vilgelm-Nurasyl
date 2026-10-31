# Enemy and combat system — Participant 2

## Scope and integration boundary

This module owns enemy archetypes, enemy combat data, enemy movement strategies, campaign wave compositions, boss phase state and the combat service entry points. The game/UI layer remains responsible for spawning the returned wave composition, ticking enemies, rendering effects and advancing waves. `PlayerCharacter.takeDamage()` remains the single place where player defense is applied. Weapon elemental multipliers and rune effects remain owned by the item system.

## Enemy data

Every `Enemy` exposes `element`, `health`/`maxHealth`, `damage`, `coinReward`/`rewardCoins`, `experienceReward`/`rewardExperience`, and `isBoss`. The old constructor remains available and defaults to `NORMAL` and non-boss for compatibility.

## Starting balance (tuning baseline)

Values are intentionally moderate starting points for a Knight beginning with 100 health and 10 base weapon damage. They are not claimed to be final play-test balance. Normal enemies generally range from 48–190 health and 10–25 damage. Mini-bosses range from 260–850 health and 24–39 damage. Shadow Lord starts at 1800 health and 48 base damage. Rewards scale with encounter difficulty. These values should be shared with the skill-tree and item teams as the initial balance contract.

## Factory Method

`EnemyCreator` is the Factory Method abstraction. `ThemedEnemyCreator` configures any `EnemyType`, while `EnemyFactory.create(...)` is a small registry entry point. Existing `SkeletonCreator`, `SlimeCreator`, and `BossCreator` remain for backwards compatibility.

## Strategies

- `MeleeBehavior`: closes distance for close-range attacks.
- `RangedBehavior`: maintains a preferred distance.
- `ChargeBehavior`: approaches aggressively.
- `SummonBehavior`: maintains support distance and exposes a consumable summon request.
- `BossBehavior`: maintains boss spacing and exposes ability requests (`AREA_ATTACK`, `SUMMON`, `TELEPORT`, `POISON_CLOUD`, `FIRE_ZONE`, `SHADOW_CLONE`, `ICE_PROJECTILE`, `SHOCKWAVE`). The game layer consumes these requests and renders/applies the corresponding effects.

Movement strategies do not apply damage directly. The game loop should call `CombatService.attackPlayer(enemy, player)` when an enemy attack is in range/cooldown. That method delegates mitigation to `PlayerCharacter.takeDamage()`.

## Campaign waves

`CampaignWaves.all()` describes the 15 regular waves in the scenario. `CampaignWaves.finalBoss()` identifies the separate Shadow Lord encounter. Wave spawning and transitions are intentionally not implemented here because they belong to Participant 4.

## Rewards

When an enemy transitions to zero health, `CombatService` claims its reward once. Ordinary enemies call `RewardService.grantEnemyReward`; bosses call `grantBossReward`. The reward service and wallet are optional together, preserving the lightweight constructor used by existing tests. If not supplied, kill/boss events are still published.

## Shadow Lord phases

`ShadowLordController` tracks `FROZEN_ARMOR`, `BURNING_RAGE`, and `TRUE_SHADOW` and provides the phase's signature ability. Pausing the game and allowing weapon switching between phases remain game/UI integration responsibilities.

## Initial balance sheet

| Enemy | Element | HP | Damage | Speed | Coins | XP |
|---|---:|---:|---:|---:|---:|---:|
| Ruin Soldier | NORMAL | 70 | 12 | 52 | 8 | 22 |
| Ruin Archer | NORMAL | 48 | 10 | 42 | 5 | 18 |
| Rune Guardian | NORMAL | 260 | 24 | 28 | 45 | 110 |
| Ice Crawler | ICE | 82 | 14 | 48 | 10 | 26 |
| Frost Warrior | ICE | 115 | 18 | 42 | 14 | 34 |
| Frost Mage | ICE | 72 | 16 | 34 | 12 | 32 |
| Ice Elemental | ICE | 155 | 20 | 36 | 20 | 48 |
| Frost Golem | ICE | 390 | 28 | 24 | 65 | 155 |
| Fire Imp | FIRE | 92 | 17 | 62 | 13 | 34 |
| Lava Warrior | FIRE | 150 | 22 | 40 | 18 | 42 |
| Fire Mage | FIRE | 105 | 21 | 35 | 17 | 40 |
| Inferno Golem | FIRE | 520 | 34 | 27 | 85 | 200 |
| Toxic Slime | POISON | 115 | 16 | 30 | 14 | 36 |
| Plague Beast | CORRUPTED | 175 | 23 | 38 | 22 | 52 |
| Poison Shaman | CORRUPTED | 125 | 17 | 32 | 20 | 48 |
| Plague Queen | CORRUPTED | 680 | 32 | 25 | 110 | 260 |
| Shadow Soldier | SHADOW | 190 | 25 | 45 | 25 | 58 |
| Wraith | UNDEAD | 135 | 23 | 70 | 22 | 55 |
| Necromancer | UNDEAD | 170 | 20 | 30 | 28 | 65 |
| Shadow Knight | SHADOW | 850 | 39 | 38 | 150 | 350 |
| Shadow Lord | SHADOW | 1800 | 48 | 42 | 300 | 700 |

Speed is a world-units-per-second baseline. Values are provisional shared tuning numbers; changes should be coordinated with the skill-tree and weapon/item owners before they tune progression around them.
