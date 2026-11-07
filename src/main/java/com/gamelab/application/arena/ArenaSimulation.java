package com.gamelab.application.arena;

import com.gamelab.application.CombatService;
import com.gamelab.application.EnemyHealthState;
import com.gamelab.domain.Enemy;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.pattern.factory.EnemyFactory;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.strategy.*;
import java.util.*;
import static com.gamelab.application.arena.ArenaCatalog.Attack;
import static com.gamelab.application.arena.PlatformPhysics.*;

/** Deterministic combat sandbox. No JavaFX or campaign save mutations. */
public final class ArenaSimulation {
    public enum BehaviorMode { AUTO, MELEE, RANGED }
    public static final List<Platform> PLATFORMS = List.of(
            new Platform(210, 755, 310), new Platform(560, 615, 400),
            new Platform(1120, 755, 300), new Platform(1460, 615, 330));

    public static final class Fighter {
        public final ArenaCatalog.Profile profile;
        public final Enemy enemy;
        public final Body body;
        public final boolean summoned;
        public double cooldown = 1, windup, charging, chargeDirection, jumpCooldown;
        public double hurtTime, hurtElapsed;
        private int observedHealth;
        private Platform jumpTarget;
        private FiringPositionPlanner.Position firingPosition;
        private double firingSearchCooldown;
        public int attacks;
        public String animation = "idle", action = "";
        private Attack pending;
        private boolean chargeHit, deathRecorded;
        private Fighter(ArenaCatalog.Profile profile, Enemy enemy, boolean summoned) {
            this.profile = profile; this.enemy = enemy; this.summoned = summoned;
            body = new Body(enemy.getX(), enemy.getY());
            observedHealth = enemy.getHealth();
        }
        public boolean retreating() {
            return enemy.getBehavior() instanceof WoundedRetreatBehavior behavior && behavior.isRetreating(enemy);
        }
        public double walkingSpeed() { return enemy.getMovementSpeed()*3; }
    }

    public static final class Projectile {
        public double x, y, life = 4;
        public final double vx, vy, radius;
        public final int damage;
        public final boolean throughPlatforms;
        public final Attack kind;
        public final String source;
        private Projectile(double x, double y, double vx, double vy, double radius, int damage,
                           boolean throughPlatforms, Attack kind, String source) {
            this.x=x; this.y=y; this.vx=vx; this.vy=vy; this.radius=radius; this.damage=damage;
            this.throughPlatforms=throughPlatforms; this.kind=kind; this.source=source;
        }
    }
    public static final class Zone {
        public final double x, y, radius;
        public final boolean poison;
        public double life = 3, tick;
        private Zone(double x, double y, boolean poison) { this.x=x; this.y=y; this.poison=poison; radius=95; }
    }

    private final PlayerCharacter player;
    private final GameEventBus events;
    private final CombatService combat;
    private final PlatformPhysics physics = new PlatformPhysics();
    private final List<Fighter> fighters = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Zone> zones = new ArrayList<>();
    private final Set<String> defeatedTypes = new LinkedHashSet<>();
    private final Body hero = new Body(350, FLOOR);
    private PlayerCombatStrategy playerStrategy = new BalancedStrategy();
    private BehaviorMode mode = BehaviorMode.AUTO;
    private boolean invincible, crouching, facingLeft;
    private double attackCooldown, dodgeTime, dodgeCooldown, poisonTime, poisonTick;
    private int sequence;
    private long attackSerial;
    private int stage = 1;
    private boolean bossEncounter;

    public ArenaSimulation(PlayerCharacter player, GameEventBus events) {
        this.player = Objects.requireNonNull(player);
        this.events = Objects.requireNonNull(events);
        combat = new CombatService(events);
        events.subscribe(event -> {
            if (!(event.payload() instanceof EnemyHealthState state)) return;
            for (Fighter fighter : fighters) if (fighter.enemy.getId().equals(state.id())) {
                if (state.health() < fighter.observedHealth) {
                    fighter.hurtTime=.36;
                    fighter.hurtElapsed=0;
                }
                fighter.observedHealth=state.health();
                break;
            }
        });
    }
    public PlayerCharacter player() { return player; }
    public Body hero() { return hero; }
    public List<Fighter> fighters() { return Collections.unmodifiableList(fighters); }
    public List<Projectile> projectiles() { return Collections.unmodifiableList(projectiles); }
    public List<Zone> zones() { return Collections.unmodifiableList(zones); }
    public Set<String> defeatedTypes() { return Collections.unmodifiableSet(defeatedTypes); }
    public boolean facingLeft() { return facingLeft; }
    public boolean dodging() { return dodgeTime > 0; }
    public boolean crouching() { return crouching; }
    public long attackSerial() { return attackSerial; }
    public boolean poisoned() { return poisonTime > 0; }
    public void setInvincible(boolean value) { invincible = value; }
    public void setPlayerStrategy(PlayerCombatStrategy strategy) { playerStrategy = Objects.requireNonNull(strategy); }
    public void setBehaviorMode(BehaviorMode value) {
        mode = Objects.requireNonNull(value);
        fighters.forEach(this::configureBehavior);
    }

    public void start(int stage, boolean boss) {
        List<ArenaCatalog.Profile> profiles = ArenaCatalog.stage(stage, boss);
        if (profiles.isEmpty()) throw new IllegalArgumentException("No enemies for this encounter");
        this.stage=stage; bossEncounter=boss;
        fighters.clear(); projectiles.clear(); zones.clear();
        restorePlayer();
        for (int i=0; i<profiles.size(); i++) spawn(profiles.get(i), 1100 + i*350, false);
        events.publish("ARENA", boss ? "Босс: этап " + stage : "Обычные враги: этап " + stage);
    }
    public void restart() { start(stage, bossEncounter); }
    public void restorePlayer() {
        player.heal(player.getMaxHealth());
        hero.x=350; hero.y=FLOOR; hero.velocityY=0; hero.grounded=true;
        poisonTime=0; poisonTick=0; attackCooldown=0; dodgeTime=0; dodgeCooldown=0;
    }

    private Fighter spawn(ArenaCatalog.Profile profile, double x, boolean summoned) {
        return spawn(profile,x,summoned,false);
    }

    private Fighter spawn(ArenaCatalog.Profile profile, double x, boolean summoned, boolean weakened) {
        Enemy enemy = EnemyFactory.create(profile.type(), "arena-" + (++sequence), x, FLOOR);
        if (weakened) enemy = new Enemy(enemy.getId(), profile.name(), Math.max(1, enemy.getMaxHealth()/2),
                Math.max(1, enemy.getDamage()/2), x, FLOOR, enemy.getMovementSpeed(), 0, 0,
                enemy.getElement(), false, enemy.getBehavior());
        Fighter fighter = new Fighter(profile, enemy, summoned);
        configureBehavior(fighter);
        fighters.add(fighter);
        return fighter;
    }

    private void configureBehavior(Fighter fighter) {
        if (fighter.profile.boss()) { fighter.enemy.setBehavior(new MeleeBehavior()); return; }
        boolean ranged = mode == BehaviorMode.RANGED || mode == BehaviorMode.AUTO && fighter.profile.ranged();
        EnemyBehavior normal = ranged ? new RangedBehavior(fighter.profile.range() * .72) : new MeleeBehavior();
        EnemyBehavior approach=fighter.profile.ranged() ? new RangedBehavior(Math.min(260,fighter.profile.range()*.45)) : new MeleeBehavior();
        fighter.enemy.setBehavior(new WoundedRetreatBehavior(normal, Math.max(330, fighter.profile.range() * .8),approach));
    }

    public void jump() { if (player.isAlive()) physics.jump(hero); }
    public void dodge() {
        if (player.isAlive() && dodgeCooldown <= 0) { dodgeTime=.28; dodgeCooldown=1.15; }
    }

    public boolean attack() {
        if (!player.isAlive() || attackCooldown > 0) return false;
        attackSerial++;
        attackCooldown = playerStrategy instanceof HeavyAttackStrategy ? .75 : .32;
        events.publish("PLAYER_ATTACK","Удар: " + playerStrategy.getStrategyName());
        for (Fighter fighter : fighters) {
            double dx = fighter.body.x-hero.x;
            if (fighter.enemy.isAlive() && Math.abs(dx) <= 155 && Math.abs(fighter.body.y-hero.y) < 105
                    && (facingLeft ? dx <= 30 : dx >= -30)) {
                int dealt=combat.attack(player, fighter.enemy, playerStrategy);
                if (dealt>0) events.publish("WEAPON_IMPACT","Попадание: "+fighter.profile.name(),
                        new com.gamelab.application.audio.WeaponImpact(fighter.profile.hitSurface(),dealt));
                recordDeath(fighter);
            }
        }
        return true;
    }

    private void recordDeath(Fighter fighter) {
        if (fighter.enemy.isAlive() || fighter.deathRecorded) return;
        fighter.deathRecorded=true;
        if (!fighter.profile.boss() && !fighter.summoned) defeatedTypes.add(fighter.profile.id());
    }

    public void woundEnemies() {
        for (Fighter fighter : fighters) if (!fighter.profile.boss() && fighter.enemy.isAlive()) {
            int target=Math.max(1, fighter.enemy.getMaxHealth()/5);
            combat.damageEnemy(fighter.enemy, Math.max(0, fighter.enemy.getHealth()-target));
        }
    }

    public void update(double dt, double movement, boolean crouch) {
        if (!Double.isFinite(dt) || dt <= 0 || dt > .05 || !Double.isFinite(movement) || Math.abs(movement)>1)
            throw new IllegalArgumentException("Invalid simulation step");
        if (!player.isAlive()) return;
        attackCooldown=Math.max(0,attackCooldown-dt);
        dodgeCooldown=Math.max(0,dodgeCooldown-dt);
        dodgeTime=Math.max(0,dodgeTime-dt);
        crouching=crouch && hero.grounded;
        if (movement != 0) facingLeft=movement<0;
        double speed=dodging() ? (facingLeft ? -640 : 640) : movement*(crouching ? 150 : 310);
        physics.step(hero,speed,24,dt,PLATFORMS);
        if (poisonTime>0) {
            poisonTime-=dt; poisonTick-=dt;
            if (poisonTick<=0) { hurt(10); poisonTick=1; }
        }
        // Spawned minions participate on the next tick, avoiding concurrent modification.
        for (Fighter fighter : List.copyOf(fighters)) if (fighter.enemy.isAlive()) updateFighter(fighter,dt);
        updateProjectiles(dt);
        for (Zone zone : zones) {
            zone.life-=dt; zone.tick-=dt;
            if (zone.tick<=0 && Math.abs(hero.x-zone.x)<zone.radius && Math.abs(hero.y-zone.y)<50) {
                hurt(14); if (zone.poison && !invincible && !dodging()) poisonTime=3; zone.tick=.6;
            }
        }
        zones.removeIf(z -> z.life<=0);
    }

    private void updateFighter(Fighter fighter, double dt) {
        if (fighter.hurtTime>0) {
            fighter.hurtTime=Math.max(0,fighter.hurtTime-dt);
            fighter.hurtElapsed+=dt;
        }
        fighter.jumpCooldown=Math.max(0,fighter.jumpCooldown-dt);
        if (fighter.charging>0) {
            fighter.charging-=dt;
            physics.step(fighter.body,fighter.chargeDirection*590,40,dt,List.of());
            fighter.enemy.setPosition(fighter.body.x,fighter.body.y);
            if (!fighter.chargeHit && Math.abs(hero.x-fighter.body.x)<95 && Math.abs(hero.y-fighter.body.y)<95) {
                hurt(fighter.enemy.getDamage()*2); fighter.chargeHit=true;
            }
            if (fighter.body.x<=41 || fighter.body.x>=1879) fighter.charging=0;
            fighter.animation="walk";
            return;
        }
        if (fighter.windup>0) {
            physics.step(fighter.body,0,30,dt,fighter.profile.boss() ? List.of() : PLATFORMS);
            fighter.enemy.setPosition(fighter.body.x,fighter.body.y);
            fighter.windup-=dt;
            if (fighter.windup<=0) releaseAttack(fighter);
            return;
        }
        double dx=hero.x-fighter.body.x;
        double targetX=hero.x;
        double targetY=hero.y;
        boolean clearShot=canShoot(fighter);
        boolean seekingShot=fighter.profile.ranged() && !fighter.profile.boss() && mode!=BehaviorMode.MELEE
                && !fighter.retreating() && !clearShot;
        fighter.firingSearchCooldown-=dt;
        if (seekingShot) {
            if (fighter.firingSearchCooldown<=0 || fighter.firingPosition==null) {
                fighter.firingPosition=FiringPositionPlanner.find(fighter.body,hero.x,hero.y,fighter.profile.range(),PLATFORMS);
                fighter.firingSearchCooldown=.5;
            }
            if (fighter.firingPosition!=null) {
                targetX=fighter.firingPosition.x(); targetY=fighter.firingPosition.y();
            }
        } else fighter.firingPosition=null;
        boolean needsClimb=!fighter.profile.ranged() || mode==BehaviorMode.MELEE || seekingShot;
        // Keep the selected landing surface throughout flight. Replanning mid-jump can
        // reverse horizontal steering or cancel it as soon as the enemy rises above the target.
        if (fighter.body.grounded) fighter.jumpTarget=null;
        Platform steppingStone = fighter.jumpTarget;
        if (steppingStone==null && fighter.body.grounded && !fighter.profile.boss()
                && targetY < fighter.body.y-65 && !fighter.retreating() && needsClimb) {
            steppingStone=physics.nextPlatform(fighter.body,targetX,targetY,PLATFORMS,fighter.walkingSpeed());
        }
        if (steppingStone!=null) targetX=steppingStone.x()+steppingStone.width()/2;
        boolean dropping=seekingShot && steppingStone==null && fighter.body.grounded && targetY>fighter.body.y+50;
        if (dropping) for (Platform surface : PLATFORMS) {
            if (Math.abs(surface.y()-fighter.body.y)<1 && surface.supports(fighter.body.x,30)) {
                targetX=Math.abs(targetX-surface.x())<Math.abs(targetX-(surface.x()+surface.width()))
                        ? surface.x()-50 : surface.x()+surface.width()+50;
                break;
            }
        }
        double beforeX=fighter.body.x;
        // Strategies choose horizontal intent. Gravity alone controls vertical movement.
        fighter.enemy.update(targetX, fighter.body.y, dt);
        double vx=(fighter.enemy.getX()-beforeX)/dt*3;
        if (seekingShot && steppingStone==null)
            vx=Math.signum(targetX-beforeX)*Math.min(fighter.walkingSpeed(),Math.abs(targetX-beforeX)/dt);
        boolean meleeStop = !fighter.retreating() && !fighter.profile.ranged() && Math.abs(dx)<80;
        if (meleeStop && steppingStone==null) vx=0;
        if (steppingStone!=null) {
            vx=Math.signum(targetX-beforeX)*Math.min(fighter.walkingSpeed(),Math.abs(targetX-beforeX)/dt);
            double gap=Math.max(steppingStone.x()-fighter.body.x,
                    fighter.body.x-(steppingStone.x()+steppingStone.width()));
            double jumpReach=fighter.walkingSpeed()*PlatformPhysics.jumpFlightTime(fighter.body.y,steppingStone.y())-15;
            if (gap<=jumpReach && fighter.body.grounded) {
                if (fighter.jumpCooldown<=0) {
                    fighter.jumpTarget=steppingStone;
                    physics.jump(fighter.body); fighter.jumpCooldown=.9;
                } else {
                    // Do not walk off the takeoff ledge while the next jump is cooling down.
                    vx=0;
                }
            }
        }
        // Once sharing a platform, distance-keeping must not walk off its edge and
        // immediately start the same climb again. Keep room to retreat on the surface.
        if (steppingStone==null && !dropping && fighter.body.grounded && Math.abs(hero.y-fighter.body.y)<50) {
            for (Platform surface : PLATFORMS) {
                if (Math.abs(surface.y()-fighter.body.y)<1 && surface.supports(fighter.body.x,0)
                        && surface.supports(hero.x,0)) {
                    double nextX=Math.max(surface.x()+30,Math.min(surface.x()+surface.width()-30,fighter.body.x+vx*dt));
                    vx=Math.max(-fighter.walkingSpeed(),Math.min(fighter.walkingSpeed(),(nextX-fighter.body.x)/dt));
                    break;
                }
            }
        }
        physics.step(fighter.body,vx,30,dt,fighter.profile.boss() ? List.of() : PLATFORMS);
        fighter.enemy.setPosition(fighter.body.x,fighter.body.y);
        fighter.animation=Math.abs(vx)>1 ? "walk" : "idle";
        boolean approaching=fighter.enemy.getBehavior() instanceof WoundedRetreatBehavior wounded && wounded.isApproaching(fighter.enemy);
        fighter.action=fighter.retreating() ? "Отступает" : seekingShot ? "Ищет линию огня" : approaching ? "Подходит для атаки" : mode == BehaviorMode.AUTO
                ? (fighter.profile.ranged() ? "Держит дистанцию" : "Сближается") : "Режим: " + mode;
        fighter.cooldown-=dt;
        if (fighter.cooldown>0) return;
        if (fighter.retreating()) return;
        // An attack windup stops horizontal movement; never interrupt a platform jump with it.
        if (!fighter.body.grounded && !fighter.profile.boss()) return;
        Attack kind=fighter.profile.attack();
        boolean close=Math.abs(dx)<115 && Math.abs(hero.y-fighter.body.y)<100;
        if (fighter.profile.boss()) {
            if (kind==Attack.FINAL) {
                if (fighter.attacks%3==2 || !close) beginAttack(fighter,Attack.FINAL,1.1,"Призыв павших");
                else if (close) beginAttack(fighter,Attack.MELEE,fighter.attacks%3==1 ? .95 : .3,
                        fighter.attacks%3==1 ? "Тяжёлый замах" : "Быстрый удар");
            } else if (kind==Attack.WITCH) beginAttack(fighter,kind,.9,fighter.attacks%2==0 ? "Призыв слизней" : "Бросок колбы");
            else if (close) beginAttack(fighter,Attack.MELEE,.5,"Удар вблизи");
            else if (Math.abs(dx)<=fighter.profile.range()) beginAttack(fighter,kind,.8,
                    kind==Attack.ROLL ? "Голем сворачивается" : kind==Attack.CHARGE ? "Готовит рывок" : "Готовит снаряд");
        } else if (fighter.profile.ranged()) {
            if (canShoot(fighter)) beginAttack(fighter,kind,.5,"Дальний выстрел");
        } else if (close) beginAttack(fighter,Attack.MELEE,.4,"Замах");
    }

    private void beginAttack(Fighter fighter, Attack kind, double delay, String action) {
        fighter.pending=kind; fighter.windup=delay; fighter.animation=kind==Attack.MELEE ? "attack_melee" : "attack_ranged";
        fighter.action=action;
        events.publish("ENEMY_ACTION",fighter.profile.name()+": "+action);
    }

    private void releaseAttack(Fighter fighter) {
        Attack kind=fighter.pending;
        if (!fighter.profile.boss() && fighter.profile.ranged() && !canShoot(fighter)) {
            fighter.cooldown=.15; fighter.animation="idle"; fighter.action="Выстрел перекрыт";
            fighter.firingSearchCooldown=0;
            return;
        }
        int damage=fighter.enemy.getDamage();
        switch (kind) {
            case MELEE -> {
                boolean heavy=fighter.profile.attack()==Attack.FINAL && fighter.attacks%3==1;
                if (Math.abs(hero.x-fighter.body.x)<145 && Math.abs(hero.y-fighter.body.y)<110) hurt(damage*(heavy ? 2 : 1));
            }
            case ROLL, CHARGE -> {
                fighter.charging=1.6; fighter.chargeDirection=Math.signum(hero.x-fighter.body.x);
                fighter.chargeHit=false;
            }
            case ICE_FAN -> { shoot(fighter,kind,0,24,damage); shoot(fighter,kind,-.19,13,damage/2); shoot(fighter,kind,.19,13,damage/2); }
            case WITCH -> {
                if (fighter.attacks%2==0) summonSlimes(fighter);
                else shoot(fighter,kind,0,18,damage);
            }
            case FINAL -> summonFallen(fighter);
            default -> shoot(fighter,kind,0,kind==Attack.STONE ? 18 : 12,damage);
        }
        fighter.attacks++;
        events.publish("ENEMY_ATTACK",fighter.profile.name()+": атака",fighter.profile.id());
        if (fighter.enemy.getBehavior() instanceof WoundedRetreatBehavior wounded) wounded.attackCompleted(fighter.enemy);
        fighter.cooldown=fighter.profile.boss() ? 2.5 : fighter.profile.ranged() ? 1.65 : .9;
        fighter.animation="idle";
    }

    private void shoot(Fighter fighter, Attack kind, double offset, double radius, int damage) {
        double y=fighter.body.y-(fighter.profile.boss() ? 90 : 55);
        double angle=Math.atan2(hero.y-48-y,hero.x-fighter.body.x)+offset;
        projectiles.add(new Projectile(fighter.body.x,y,Math.cos(angle)*390,Math.sin(angle)*390,radius,damage,
                fighter.profile.boss(),kind,fighter.profile.id()));
    }

    public boolean canShoot(Fighter fighter) {
        return fighter.profile.boss() || FiringPositionPlanner.canShoot(fighter.body.x,fighter.body.y,
                hero.x,hero.y,fighter.profile.range(),PLATFORMS);
    }

    private void summonSlimes(Fighter fighter) {
        int count=fighter.attacks%4==0 ? 2 : 3;
        int available=(int)Math.max(0,6-fighters.stream().filter(f -> f.summoned && f.enemy.isAlive()).count());
        for (int i=0;i<Math.min(count,available);i++) spawn(ArenaCatalog.byId("slime"),
                Math.max(60,Math.min(1860,fighter.body.x+(i%2==0 ? -1 : 1)*(100+i*45))),true);
    }

    private void summonFallen(Fighter fighter) {
        int available=(int)Math.max(0,6-fighters.stream().filter(f -> f.summoned && f.enemy.isAlive()).count());
        List<String> history=new ArrayList<>(defeatedTypes);
        for (int i=0;i<Math.min(3,Math.min(available,history.size()));i++) {
            String id=history.get((fighter.attacks/3+i)%history.size());
            spawn(ArenaCatalog.byId(id),Math.max(60,Math.min(1860,fighter.body.x+(i-1)*170)),true,true);
        }
        events.publish("SUMMON",history.isEmpty() ? "Пока нет убитых обычных врагов для призыва" : "Призыв ослабленных павших");
    }

    private void updateProjectiles(double dt) {
        Iterator<Projectile> iterator=projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile shot=iterator.next();
            double oldX=shot.x, oldY=shot.y;
            shot.x+=shot.vx*dt; shot.y+=shot.vy*dt; shot.life-=dt;
            double heroHeight=crouching ? 38 : 90;
            boolean hit=shot.x+shot.radius>hero.x-24 && shot.x-shot.radius<hero.x+24
                    && shot.y+shot.radius>hero.y-heroHeight && shot.y-shot.radius<hero.y;
            boolean terrain=!shot.throughPlatforms && FiringPositionPlanner.blocked(oldX,oldY,shot.x,shot.y,shot.radius,PLATFORMS);
            boolean expired=shot.life<=0 || shot.x<-60 || shot.x>1980 || shot.y<180 || shot.y>FLOOR;
            if (hit || terrain || expired) {
                if (hit && !dodging()) {
                    hurt(shot.damage);
                    if (shot.kind==Attack.POISON && !invincible) { poisonTime=4; poisonTick=1; }
                }
                if ((hit || terrain || shot.y>FLOOR) && (shot.kind==Attack.FIREBALL || shot.kind==Attack.WITCH)) {
                    double surface=FLOOR;
                    for (Platform platform : PLATFORMS) if (platform.supports(shot.x,0) && platform.y()>=shot.y-20)
                        surface=Math.min(surface,platform.y());
                    zones.add(new Zone(shot.x,surface,shot.kind==Attack.WITCH));
                }
                iterator.remove();
            }
        }
    }

    private void hurt(int damage) {
        if (invincible || dodging() || !player.isAlive()) return;
        int before=player.getHealth();
        player.takeDamage(damage);
        events.publish("PLAYER_HIT","Герой получил " + (before-player.getHealth()) + " урона");
    }
}
