# Enemy AI and combat implementation

## Enemy AI (Strategy)
`Enemy` stores an `EnemyBehavior`, and `setBehavior` can replace it at runtime. `ChaseBehavior` pursues the player, `DefensiveBehavior` holds position, and `RangedBehavior` maintains a configurable preferred distance. Movement is expressed in world units per second and receives the frame delta in seconds; the strategies do not depend on JavaFX.

## Enemy creation (Factory Method)
`EnemyCreator.createEnemy(id, x, y)` is the factory method. `SkeletonCreator`, `SlimeCreator`, and `BossCreator` provide distinct stats, rewards, speeds, and initial AI strategies.

## Combat
`CombatService.attack` uses the existing `PlayerCombatStrategy` to calculate player damage, applies it to an enemy, and publishes combat events through `GameEventBus`. An optional `RewardService` + `CoinWallet` pair grants configured kill rewards. `damageEnemy` supports direct damage from other combat sources. The UI/game loop should own the enemy collection and call `enemy.update(playerX, playerY, deltaSeconds)` once per tick; this implementation deliberately does not add a second game loop or JavaFX physics layer.

## Integration notes
- Create enemies through the concrete creators and keep them in the game/session owner.
- Call `update` once per frame with elapsed seconds.
- Remove enemies for which `isAlive()` is false from the active collection.
- Construct `CombatService` with `GameFacade.events()` (or the same shared event bus) so observers receive combat notifications.
- To enable kill rewards, pass a `RewardService` backed by that same event bus and the player's `CoinWallet` to the three-argument constructor.
