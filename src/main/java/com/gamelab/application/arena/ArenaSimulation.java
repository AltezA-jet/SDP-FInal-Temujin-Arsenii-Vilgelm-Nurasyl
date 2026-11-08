package com.gamelab.application.arena;

import com.gamelab.application.CombatService;
import com.gamelab.application.EnemyHealthState;
import com.gamelab.application.RuneCombatService;
import com.gamelab.domain.rune.*;
import com.gamelab.domain.Enemy;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.pattern.factory.EnemyFactory;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.strategy.*;
import java.util.*;
import static com.gamelab.application.arena.ArenaCatalog.Attack;
import static com.gamelab.application.arena.PlatformPhysics.*;

/** Shared arena combat for the sandbox and campaign. No JavaFX or save mutations. */
public final class ArenaSimulation {
    public static final double ROLL_WINDUP=.28*1.5, LORD_QUICK_WINDUP=.48;
    public static final double HERO_HEIGHT=132, HERO_CROUCH_HEIGHT=95, HERO_RADIUS=24;
    public enum BehaviorMode { AUTO, MELEE, RANGED }
    /** Legacy name retained for stage-one physics callers. Active encounters use platforms(). */
    public static final List<Platform> PLATFORMS = ArenaLayouts.stage(1).platforms();

    public static final class Fighter {
        public final ArenaCatalog.Profile profile;
        public final Enemy enemy;
        public final Body body;
        public final boolean summoned;
        public double cooldown = 1, windup, charging, chargeDirection, jumpCooldown;
        public double hurtTime, hurtElapsed, animationTime, recovery, summonCooldown, slowTime, dotTime, dotTick;
        public int dotDamage, phase=1;
        public boolean facingLeft=true, heavy, rollJumped;
        public String owner="";
        private boolean hitReleased;
        private double hitAt;
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
    public static final class Impact {
        public final double x,y;
        public double time;
        private Impact(double x,double y) { this.x=x;this.y=y; }
    }

    private final PlayerCharacter player;
    private final GameEventBus events;
    private final CombatService combat;
    private final RuneCombatService runeCombat;
    private RuneLoadout loadout;
    private final Random random;
    private final PlatformPhysics physics = new PlatformPhysics();
    private final List<Fighter> fighters = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Zone> zones = new ArrayList<>();
    private final List<Impact> impacts=new ArrayList<>();
    private final Set<String> defeatedTypes = new LinkedHashSet<>();
    private final Body hero = new Body(350, FLOOR);
    private PlayerCombatStrategy playerStrategy = new BalancedStrategy();
    private BehaviorMode mode = BehaviorMode.AUTO;
    private boolean invincible, crouching, facingLeft;
    private double attackCooldown, dodgeTime, dodgeCooldown, poisonTime, poisonTick, slowTime;
    private double playerHitDelay, playerAttackDuration, playerAttackTime;
    private boolean playerHitPending;
    private int poisonDamage=10;
    private double hitProtection, attackBuffer;
    private double stepDistance;
    private int sequence;
    private long attackSerial;
    private int stage = 1;
    private boolean bossEncounter;
    private List<Platform> platforms=PLATFORMS;
    private boolean fullStage, exitReady, completed;
    private int wave;
    private double waveDelay;

    public ArenaSimulation(PlayerCharacter player, GameEventBus events) {
        this(player,events,new Random());
    }
    public ArenaSimulation(PlayerCharacter player, GameEventBus events, Random random) {
        this.player = Objects.requireNonNull(player);
        this.events = Objects.requireNonNull(events);
        this.random=Objects.requireNonNull(random);
        combat = new CombatService(events);
        runeCombat = new RuneCombatService(combat,random::nextDouble);
        events.subscribe(event -> {
            if (!(event.payload() instanceof EnemyHealthState state)) return;
            for (Fighter fighter : fighters) if (fighter.enemy.getId().equals(state.id())) {
                if (state.health() < fighter.observedHealth) {
                    fighter.hurtTime=.36;
                    fighter.hurtElapsed=0;
                    if (fighter.profile.attack()==Attack.SUMMON && fighter.windup>0) {
                        fighter.windup=0; fighter.recovery=.4; fighter.cooldown=2;
                        fighter.animation="idle"; fighter.action="Призыв прерван";
                    }
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
    public List<Impact> impacts() { return Collections.unmodifiableList(impacts); }
    public Set<String> defeatedTypes() { return Collections.unmodifiableSet(defeatedTypes); }
    public boolean facingLeft() { return facingLeft; }
    public boolean dodging() { return dodgeTime > 0; }
    public boolean crouching() { return crouching; }
    public long attackSerial() { return attackSerial; }
    public boolean poisoned() { return poisonTime > 0; }
    public boolean slowed() { return slowTime>0; }
    public List<Platform> platforms() { return platforms; }
    public int wave() { return wave; }
    public boolean fullStage() { return fullStage; }
    public boolean exitReady() { return exitReady; }
    public boolean completed() { return completed; }
    public double playerAttackDuration() { return playerAttackDuration; }
    public boolean attacking() { return attackCooldown>0; }
    public double dodgeCooldown() { return dodgeCooldown; }
    public boolean damageProtected() { return invincible || dodging() || hitProtection>0; }
    public RuneLoadout loadout() { return loadout; }
    public void setLoadout(RuneLoadout value) { loadout=Objects.requireNonNull(value); }
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
        platforms=ArenaLayouts.stage(stage).platforms(); fullStage=false; wave=0; exitReady=false; completed=false;
        fighters.clear(); projectiles.clear(); zones.clear(); impacts.clear();
        restorePlayer();
        for (int i=0; i<profiles.size(); i++) spawn(profiles.get(i), 1100 + i*350, false);
        events.publish("ARENA", boss ? "Босс: этап " + stage : "Обычные враги: этап " + stage);
    }
    public void startStage(int stage) {
        start(stage,stage==6);
        fullStage=true; wave=1; waveDelay=0;
        spawnWave();
    }
    private void spawnWave() {
        fighters.clear(); projectiles.clear(); zones.clear(); impacts.clear();
        List<String> roster=ArenaLayouts.wave(stage,wave);
        for(int i=0;i<roster.size();i++) {
            double x=780+i*280;
            if(Math.abs(x-hero.x)<220) x=hero.x<960 ? 1650-i*220 : 260+i*220;
            spawn(ArenaCatalog.byId(roster.get(i)),Math.max(80,Math.min(1840,x)),false);
        }
        events.publish("ARENA_WAVE",ArenaLayouts.stage(stage).name()+" — волна "+wave+" / "+(stage==6?1:3));
    }
    public void restart() { if(fullStage) startStage(stage); else start(stage, bossEncounter); }
    public void restorePlayer() {
        player.heal(player.getMaxHealth());
        hero.x=350; hero.y=FLOOR; hero.velocityY=0; hero.grounded=true;
        poisonTime=0; poisonTick=0; slowTime=0; attackCooldown=0; dodgeTime=0; dodgeCooldown=0; hitProtection=0; attackBuffer=0; playerHitPending=false;
        stepDistance=0;
    }

    private Fighter spawn(ArenaCatalog.Profile profile, double x, boolean summoned) {
        return spawn(profile,x,summoned,false);
    }

    private Fighter spawn(ArenaCatalog.Profile profile, double x, boolean summoned, boolean weakened) {
        Enemy enemy = EnemyFactory.create(profile.type(), "arena-" + (++sequence), x, FLOOR);
        if(!summoned) enemy=new Enemy(enemy.getId(),profile.name(),ArenaBalance.health(profile,enemy.getMaxHealth()),
                ArenaBalance.damage(profile,enemy.getDamage()),x,FLOOR,enemy.getMovementSpeed(),enemy.getRewardCoins(),
                enemy.getRewardExperience(),enemy.getElement(),enemy.isBoss(),enemy.getBehavior());
        if (summoned) enemy = new Enemy(enemy.getId(), profile.name(), Math.max(1, enemy.getMaxHealth()/(weakened?2:1)),
                Math.max(1, enemy.getDamage()/(weakened?2:1)), x, FLOOR, enemy.getMovementSpeed(), 0, 0,
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

    public void jump() {
        if(player.isAlive() && hero.grounded) { physics.jump(hero); events.publish("PLAYER_JUMP","Прыжок"); }
    }
    public void dodge() {
        dodge(0);
    }
    public void dodge(double direction) {
        if (player.isAlive() && dodgeCooldown <= 0) {
            if(direction!=0) facingLeft=direction<0;
            dodgeTime=.32; dodgeCooldown=.85;
            playerHitPending=false; attackCooldown=0; attackBuffer=0;
            events.publish("PLAYER_DODGE","Уклонение");
        }
    }

    public boolean attack() {
        if (!player.isAlive()) return false;
        if(attackCooldown>0 || dodging()) {
            if(attackCooldown<=.14 && dodgeTime<=.14) attackBuffer=.16;
            return false;
        }
        attackSerial++;
        double speed=(loadout==null ? 1 : loadout.createDecoratedWeapon().getAttackSpeed())*(1+player.getAttackSpeedBonusPercent()/100);
        playerAttackDuration=(playerStrategy instanceof HeavyAttackStrategy ? .75 : .40)/speed;
        attackCooldown=playerAttackDuration;
        playerAttackTime=0; playerHitDelay=playerAttackDuration*.5; playerHitPending=true;
        events.publish("PLAYER_ATTACK","Удар: " + playerStrategy.getStrategyName());
        return true;
    }

    private void playerStrike() {
        for (Fighter fighter : fighters) {
            double dx = fighter.body.x-hero.x;
            boolean ball=fighter.profile.attack()==Attack.ROLL && (fighter.charging>0 || fighter.animation.equals("transform_back") && fighter.animationTime<.28);
            if (fighter.enemy.isAlive() && Math.abs(dx) <= 155 && Math.abs(fighter.body.y-hero.y) < 105
                    && (!ball || hero.y-55>=fighter.body.y-80)
                    && (facingLeft ? dx <= 30 : dx >= -30)) {
                int dealt;
                PlayerCombatStrategy effective=playerStrategy;
                if(fighter.profile.attack()==Attack.FINAL && fighter.phase==1 && !hasRune(RuneType.ANCIENT)) {
                    effective=new PlayerCombatStrategy() {
                        public int calculateDamage(PlayerCharacter p) { return (int)Math.round(playerStrategy.calculateDamage(p)*.8); }
                        public String getStrategyName() { return playerStrategy.getStrategyName(); }
                    };
                }
                double protection=fighter.profile.attack()==Attack.FINAL && fighter.phase==1 && !hasRune(RuneType.ANCIENT)?.8:1;
                dealt=loadout==null ? combat.attack(player,fighter.enemy,effective) : runeCombat.attack(player,fighter.enemy,playerStrategy,loadout,protection);
                if(dealt>0) {
                    if(hasRune(RuneType.POISON)||hasRune(RuneType.FIRE)) { fighter.dotTime=3; fighter.dotDamage=hasRune(RuneType.FIRE)?4:3; }
                    if(hasRune(RuneType.ICE)) fighter.slowTime=2;
                    if(hasRune(RuneType.LIGHTNING)) {
                        Fighter other=fighters.stream().filter(f -> f!=fighter && f.enemy.isAlive() && Math.hypot(f.body.x-fighter.body.x,f.body.y-fighter.body.y)<260).findFirst().orElse(null);
                        if(other!=null) { combat.damageEnemy(other.enemy,Math.max(1,dealt/3)); recordDeath(other); }
                    }
                }
                if (dealt>0) events.publish("WEAPON_IMPACT","Попадание: "+fighter.profile.name(),
                        new com.gamelab.application.audio.WeaponImpact(fighter.profile.hitSurface(),dealt));
                recordDeath(fighter);
            }
        }
    }
    private boolean hasRune(RuneType type) { return loadout!=null && loadout.getInstalledRunes().stream().anyMatch(r -> r.getRuneType()==type); }

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
        hitProtection=Math.max(0,hitProtection-dt);
        if(attackBuffer>0) {
            attackBuffer=Math.max(0,attackBuffer-dt);
            if(attackCooldown==0 && !dodging()) { attackBuffer=0; attack(); }
        }
        if(playerHitPending) {
            playerAttackTime+=dt;
            if(playerAttackTime>=playerHitDelay) { playerHitPending=false; playerStrike(); }
        }
        dodgeCooldown=Math.max(0,dodgeCooldown-dt);
        dodgeTime=Math.max(0,dodgeTime-dt);
        crouching=crouch && hero.grounded;
        if (movement != 0 && !playerHitPending && !dodging()) facingLeft=movement<0;
        slowTime=Math.max(0,slowTime-dt);
        double speed=dodging() ? (facingLeft ? -640 : 640) : playerHitPending ? 0 :
                movement*(crouching ? 150 : 310)*(attackCooldown>0?.65:1)*(slowed()?.6:1);
        boolean airborne=!hero.grounded;double fallSpeed=hero.velocityY,previousX=hero.x;
        physics.step(hero,speed,24,dt,platforms);
        if(airborne && hero.grounded && fallSpeed>180) events.publish("PLAYER_LANDED","Приземление");
        if(!airborne && hero.grounded && !dodging() && Math.abs(speed)>1) {
            stepDistance+=Math.abs(hero.x-previousX);
            if(stepDistance>=85) { stepDistance-=85; events.publish("PLAYER_STEP","Шаг"); }
        } else stepDistance=0;
        if (poisonTime>0) {
            poisonTime-=dt; poisonTick-=dt;
            if (poisonTick<=0) { hurt(poisonDamage); poisonTick=1; }
        }
        // Spawned minions participate on the next tick, avoiding concurrent modification.
        for (Fighter fighter : List.copyOf(fighters)) if (fighter.enemy.isAlive()) updateFighter(fighter,dt);
        updateProjectiles(dt);
        impacts.forEach(i -> i.time+=dt); impacts.removeIf(i -> i.time>=.4);
        for (Zone zone : zones) {
            zone.life-=dt; zone.tick-=dt;
            if (zone.tick<=0 && Math.abs(hero.x-zone.x)<zone.radius && Math.abs(hero.y-zone.y)<50) {
                hurt(14); if (zone.poison && !invincible && !dodging()) poisonTime=3; zone.tick=.6;
            }
        }
        zones.removeIf(z -> z.life<=0);
        fighters.forEach(this::recordDeath);
        if(fullStage && !completed && fighters.stream().noneMatch(f -> f.enemy.isAlive())) {
            if(waveDelay==0 && wave<(stage==6?1:3)) events.publish("ARENA_WAVE_CLEARED","Волна пройдена");
            waveDelay+=dt;
            if(wave<(stage==6?1:3) && waveDelay>=2) { wave++; waveDelay=0; spawnWave(); }
            else if(wave==(stage==6?1:3) && !exitReady) {
                exitReady=true; events.publish("ARENA_EXIT_READY","Победа! Идите к выходу справа");
            }
        }
        if(exitReady && !completed && hero.x>=1850 && hero.grounded && hero.y==FLOOR) {
            completed=true; events.publish("ARENA_STAGE_COMPLETED","Этап завершён. Переход в магазин",stage);
        }
    }

    private void updateFighter(Fighter fighter, double dt) {
        fighter.animationTime+=dt;
        fighter.summonCooldown=Math.max(0,fighter.summonCooldown-dt);
        fighter.slowTime=Math.max(0,fighter.slowTime-dt);
        if(fighter.dotTime>0) {
            fighter.dotTime-=dt; fighter.dotTick-=dt;
            if(fighter.dotTick<=0) { combat.damageEnemy(fighter.enemy,fighter.dotDamage); fighter.dotTick=1; }
            if(!fighter.enemy.isAlive()) return;
        }
        if(fighter.profile.attack()==Attack.FINAL) {
            double health=(double)fighter.enemy.getHealth()/fighter.enemy.getMaxHealth();
            int previousPhase=fighter.phase;
            fighter.phase=Math.max(fighter.phase,health<=.33?3:health<=.66?2:1);
            if(previousPhase!=fighter.phase) events.publish("BOSS_PHASE_CHANGED","Новая фаза Лорда",fighter.phase);
            if(previousPhase<3 && fighter.phase==3) fighter.summonCooldown=1+random.nextDouble();
        }
        if (fighter.hurtTime>0) {
            fighter.hurtTime=Math.max(0,fighter.hurtTime-dt);
            fighter.hurtElapsed+=dt;
        }
        fighter.jumpCooldown=Math.max(0,fighter.jumpCooldown-dt);
        if (fighter.charging>0) {
            fighter.charging-=dt;
            boolean roll=fighter.profile.attack()==Attack.ROLL;
            if(roll && !fighter.rollJumped && fighter.body.grounded && hero.grounded && hero.y<fighter.body.y-50) {
                Platform target=platforms.stream().filter(p -> Math.abs(p.y()-hero.y)<1 && p.supports(hero.x,0)).findFirst().orElse(null);
                if(target!=null && fighter.body.y-target.y()<=165) {
                    double gap=Math.max(target.x()-fighter.body.x,fighter.body.x-target.x()-target.width());
                    if(gap<220 && (hero.x-fighter.body.x)*fighter.chargeDirection>0) {
                        physics.jump(fighter.body); fighter.rollJumped=true;
                    }
                }
            }
            physics.step(fighter.body,fighter.chargeDirection*(fighter.pending==Attack.LEAP?340:590),roll?32:30,dt,platforms);
            fighter.enemy.setPosition(fighter.body.x,fighter.body.y);
            if (!fighter.chargeHit && Math.abs(hero.x-fighter.body.x)<(roll?76:85)
                    && hero.y>fighter.body.y-(roll?58:100) && hero.y-90<fighter.body.y) {
                boolean hit=!damageProtected();
                hurt(fighter.enemy.getDamage()*(fighter.pending==Attack.LEAP?1:2)); fighter.chargeHit=true;
                if(hit) applyContactEffect(fighter);
                if(fighter.profile.attack()==Attack.CHARGE) fighter.charging=0;
            }
            if (fighter.body.x<=41 || fighter.body.x>=1879) fighter.charging=0;
            if(fighter.charging<=0) {
                fighter.recovery=roll?.42:.7; fighter.animation=roll?"transform_back":"idle"; fighter.animationTime=0;
            } else fighter.animation=roll?"roll":fighter.pending==Attack.LEAP?"attack":"sturm";
            return;
        }
        if(fighter.recovery>0) {
            fighter.recovery=Math.max(0,fighter.recovery-dt);
            physics.step(fighter.body,0,30,dt,platforms); fighter.enemy.setPosition(fighter.body.x,fighter.body.y);
            fighter.action="Восстанавливается";
            if(fighter.recovery==0) { fighter.animation="idle"; fighter.animationTime=0; }
            return;
        }
        if (fighter.windup>0) {
            physics.step(fighter.body,0,30,dt,platforms);
            fighter.enemy.setPosition(fighter.body.x,fighter.body.y);
            fighter.windup-=dt;
            if(!fighter.hitReleased && fighter.animationTime>=fighter.hitAt) { fighter.hitReleased=true; releaseAttack(fighter); }
            if (fighter.windup<=0 && fighter.charging<=0) fighter.recovery=fighter.heavy?.75:fighter.profile.boss()?.4:.18;
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
                fighter.firingPosition=FiringPositionPlanner.find(fighter.body,hero.x,hero.y,fighter.profile.range(),platforms);
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
        if (steppingStone==null && fighter.body.grounded
                && targetY < fighter.body.y-65 && !fighter.retreating() && needsClimb) {
            steppingStone=physics.nextPlatform(fighter.body,targetX,targetY,platforms,fighter.walkingSpeed());
        }
        if (steppingStone!=null) targetX=steppingStone.x()+steppingStone.width()/2;
        boolean dropping=steppingStone==null && fighter.body.grounded && targetY>fighter.body.y+50;
        if (dropping) for (Platform surface : platforms) {
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
        vx*=fighter.slowTime>0?.6:1;
        if(fighter.profile.attack()==Attack.FINAL && fighter.phase>=2) vx*=1.2;
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
            for (Platform surface : platforms) {
                if (Math.abs(surface.y()-fighter.body.y)<1 && surface.supports(fighter.body.x,0)
                        && surface.supports(hero.x,0)) {
                    double nextX=Math.max(surface.x()+30,Math.min(surface.x()+surface.width()-30,fighter.body.x+vx*dt));
                    vx=Math.max(-fighter.walkingSpeed(),Math.min(fighter.walkingSpeed(),(nextX-fighter.body.x)/dt));
                    break;
                }
            }
        }
        physics.step(fighter.body,vx,30,dt,platforms);
        fighter.enemy.setPosition(fighter.body.x,fighter.body.y);
        fighter.animation=Math.abs(vx)>1 ? "walk" : "idle";
        if(Math.abs(hero.x-fighter.body.x)>1) fighter.facingLeft=hero.x<fighter.body.x;
        boolean approaching=fighter.enemy.getBehavior() instanceof WoundedRetreatBehavior wounded && wounded.isApproaching(fighter.enemy);
        fighter.action=fighter.retreating() ? "Отступает" : seekingShot ? "Ищет линию огня" : approaching ? "Подходит для атаки" : mode == BehaviorMode.AUTO
                ? (fighter.profile.ranged() ? "Держит дистанцию" : "Сближается") : "Режим: " + mode;
        fighter.cooldown-=dt;
        boolean summonReady=fighter.profile.attack()==Attack.FINAL && fighter.phase==3
                && fighter.summonCooldown<=0 && !defeatedTypes.isEmpty() && lordSummonSlots()>0;
        if (fighter.cooldown>0 && !summonReady) return;
        if (fighter.retreating()) return;
        // An attack windup stops horizontal movement; never interrupt a platform jump with it.
        if (!fighter.body.grounded) return;
        Attack kind=fighter.profile.attack();
        boolean close=Math.abs(dx)<115 && Math.abs(hero.y-fighter.body.y)<100;
        if (fighter.profile.boss()) {
            if (kind==Attack.FINAL) {
                // A random timer is shared by melee and distant combat. Never retry a probability every idle frame.
                if(summonReady)
                    beginAttack(fighter,Attack.FINAL,1.1,"Призыв павших");
                else if(close) {
                    fighter.heavy=fighter.phase>=2 && !fighter.heavy && random.nextBoolean();
                    beginAttack(fighter,Attack.MELEE,fighter.heavy?.9:LORD_QUICK_WINDUP,fighter.heavy?"Тяжёлый замах":"Быстрый удар");
                }
            } else if(kind==Attack.WITCH) {
                boolean summon=fighter.summonCooldown<=0 && ownedMinions(fighter)==0;
                beginAttack(fighter,summon?Attack.SUMMON:Attack.POISON,.8,summon?"Призыв слаймов":"Ядовитое жало");
            }
            else if (close) beginAttack(fighter,Attack.MELEE,.5,"Удар вблизи");
            else if (Math.abs(dx)<=fighter.profile.range() && (kind!=Attack.CHARGE || Math.abs(hero.y-fighter.body.y)<90)) beginAttack(fighter,kind,.8,
                    kind==Attack.ROLL ? "Голем сворачивается" : kind==Attack.CHARGE ? "Готовит рывок" : "Готовит снаряд");
        } else if(kind==Attack.SUMMON) {
            if(!fighter.summoned && fighter.summonCooldown<=0 && ownedMinions(fighter)<2)
                beginAttack(fighter,Attack.SUMMON,.8,"Призыв скелета");
        } else if (fighter.profile.ranged()) {
            if (canShoot(fighter)) beginAttack(fighter,kind,.5,"Дальний выстрел");
        } else if(kind==Attack.LEAP && Math.abs(dx)<180 && Math.abs(hero.y-fighter.body.y)<100)
            beginAttack(fighter,Attack.LEAP,.4,"Готовит выпад");
        else if (close) beginAttack(fighter,Attack.MELEE,.4,"Замах");
    }

    private void beginAttack(Fighter fighter, Attack kind, double delay, String action) {
        fighter.pending=kind;
        fighter.hitReleased=false; fighter.animationTime=0;
        fighter.hitAt=kind==Attack.ROLL?ROLL_WINDUP:delay;
        fighter.windup=kind==Attack.ROLL?ROLL_WINDUP:delay*2;
        fighter.facingLeft=hero.x<fighter.body.x;
        fighter.animation=switch(kind) {
            case ROLL -> "transform";
            case CHARGE -> "sturm";
            case SUMMON -> fighter.profile.boss()?"attack_summon_poison_slime":"attack_summon";
            case FINAL -> "attack_summon";
            case POISON -> fighter.profile.boss()?"attack_poison":"attack";
            case MELEE -> fighter.profile.attack()==Attack.FINAL?(fighter.heavy?"attack_heavy":"attack_quick"):"attack_melee";
            default -> "attack_ranged";
        };
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
                double dx=hero.x-fighter.body.x;
                if (Math.abs(dx)<145 && Math.abs(hero.y-fighter.body.y)<110 && (fighter.facingLeft?dx<=20:dx>=-20)) {
                    boolean hit=!damageProtected();
                    hurt(damage*(fighter.heavy ? 2 : 1));
                    if(hit && fighter.profile.boss()) knockback(fighter,fighter.heavy?110:45);
                }
            }
            case ROLL, CHARGE, LEAP -> {
                fighter.charging=kind==Attack.LEAP?.38:1.6; fighter.chargeDirection=fighter.facingLeft?-1:1;
                fighter.chargeHit=false; fighter.rollJumped=false; fighter.windup=0; fighter.animationTime=0;
                if(kind==Attack.LEAP && !fighter.profile.id().equals("demon")) { fighter.body.velocityY=-260; fighter.body.grounded=false; }
            }
            case ICE_FAN -> { shoot(fighter,kind,-.20,24,damage); shoot(fighter,kind,.20,24,damage); shoot(fighter,kind,0,13,damage/2); }
            case SUMMON -> {
                if(fighter.profile.boss()) summonSlimes(fighter);
                else if(!fighter.summoned && ownedMinions(fighter)<2) {
                    Fighter minion=spawn(ArenaCatalog.SKELETON,Math.max(60,Math.min(1860,fighter.body.x+80)),true,true);
                    minion.owner=fighter.enemy.getId(); fighter.summonCooldown=5;
                    events.publish("SUMMON","Призван скелет");
                }
            }
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
        fighter.cooldown=ArenaBalance.attackCooldown(fighter.profile,fighter.phase,fighter.heavy);
    }

    private long ownedMinions(Fighter fighter) {
        return fighters.stream().filter(f -> f.summoned && f.enemy.isAlive() && f.owner.equals(fighter.enemy.getId())).count();
    }
    private void knockback(Fighter fighter,double distance) {
        hero.x=Math.max(24,Math.min(1896,hero.x+(fighter.facingLeft?-1:1)*distance));
    }
    private void applyContactEffect(Fighter fighter) {
        if(fighter.profile.stage()==2) slowTime=2;
        if(fighter.profile.id().equals("slime")||fighter.profile.id().equals("poison-slime")) { poisonTime=3; poisonDamage=5; }
        if(fighter.profile.attack()==Attack.CHARGE) knockback(fighter,120);
    }

    private void shoot(Fighter fighter, Attack kind, double offset, double radius, int damage) {
        double y=fighter.body.y-(fighter.profile.boss() ? 90 : 55);
        double angle=Math.atan2(hero.y-48-y,hero.x-fighter.body.x)+offset;
        double speed=kind==Attack.ICE_FAN && radius<20?510:390;
        projectiles.add(new Projectile(fighter.body.x,y,Math.cos(angle)*speed,Math.sin(angle)*speed,radius,damage,
                fighter.profile.boss(),kind,fighter.profile.id()));
    }

    public boolean canShoot(Fighter fighter) {
        return fighter.profile.boss() || FiringPositionPlanner.canShoot(fighter.body.x,fighter.body.y,
                hero.x,hero.y,fighter.profile.range(),platforms);
    }

    private void summonSlimes(Fighter fighter) {
        int count=fighter.enemy.getHealth()<fighter.enemy.getMaxHealth()*.35?3:2;
        if(ownedMinions(fighter)>0) return;
        for (int i=0;i<count;i++) {
            Fighter minion=spawn(ArenaCatalog.POISON_SLIME,Math.max(60,Math.min(1860,fighter.body.x+(i%2==0?-1:1)*(100+i*45))),true);
            minion.owner=fighter.enemy.getId();
        }
        fighter.summonCooldown=12;
        events.publish("SUMMON","Призваны ядовитые слаймы");
    }

    private void summonFallen(Fighter fighter) {
        int available=lordSummonSlots();
        List<String> history=new ArrayList<>(defeatedTypes);
        for (int i=0;i<Math.min(3,available) && !history.isEmpty();i++) {
            String id=history.get(random.nextInt(history.size()));
            spawn(ArenaCatalog.byId(id),Math.max(60,Math.min(1860,fighter.body.x+(i-1)*170)),true,true);
        }
        fighter.summonCooldown=ArenaBalance.lordSummonInterval(random);
        events.publish("SUMMON",history.isEmpty() ? "Пока нет убитых обычных врагов для призыва" : "Призыв ослабленных павших");
    }
    private int lordSummonSlots() {
        return (int)Math.max(0,3-fighters.stream().filter(f -> f.summoned && f.enemy.isAlive()).count());
    }

    private void updateProjectiles(double dt) {
        Iterator<Projectile> iterator=projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile shot=iterator.next();
            double oldX=shot.x, oldY=shot.y;
            shot.x+=shot.vx*dt; shot.y+=shot.vy*dt; shot.life-=dt;
            double heroHeight=crouching ? HERO_CROUCH_HEIGHT : HERO_HEIGHT;
            boolean hit=shot.x+shot.radius>hero.x-24 && shot.x-shot.radius<hero.x+24
                    && shot.y+shot.radius>hero.y-heroHeight && shot.y-shot.radius<hero.y;
            boolean terrain=!shot.throughPlatforms && FiringPositionPlanner.blocked(oldX,oldY,shot.x,shot.y,shot.radius,platforms);
            boolean expired=shot.life<=0 || shot.x<-60 || shot.x>1980 || shot.y<180 || shot.y>FLOOR;
            if (hit || terrain || expired) {
                if (hit && !damageProtected()) {
                    hurt(shot.damage);
                    if (shot.kind==Attack.POISON && !invincible) { poisonTime=4; poisonTick=1; poisonDamage=shot.source.equals("witch")?10:6; }
                    if(!invincible && (shot.source.equals("elemental") || shot.kind==Attack.ICE_FAN && shot.radius<20)) slowTime=2.5;
                    if(!invincible && shot.kind==Attack.ICE_FAN && shot.radius>20) hero.x=Math.max(24,Math.min(1896,hero.x+Math.signum(shot.vx)*65));
                }
                if ((hit || terrain || shot.y>FLOOR) && (shot.kind==Attack.FIREBALL || shot.kind==Attack.WITCH)) {
                    if(shot.kind==Attack.FIREBALL) {
                        impacts.add(new Impact(shot.x,Math.min(FLOOR,shot.y)));
                        if(!hit && Math.hypot(hero.x-shot.x,hero.y-45-shot.y)<100) hurt(shot.damage);
                    }
                    double surface=FLOOR;
                    for (Platform platform : platforms) if (platform.supports(shot.x,0) && platform.y()>=shot.y-20)
                        surface=Math.min(surface,platform.y());
                    double zoneY=surface;
                    if(zones.size()<4 && zones.stream().noneMatch(z -> Math.abs(z.x-shot.x)<150 && Math.abs(z.y-zoneY)<10))
                        zones.add(new Zone(shot.x,surface,shot.kind==Attack.WITCH));
                }
                iterator.remove();
            }
        }
    }

    private void hurt(int damage) {
        if (damageProtected() || !player.isAlive()) return;
        int before=player.getHealth();
        player.takeDamage(damage);
        if(player.getHealth()<before) hitProtection=.30;
        int lost=before-player.getHealth();
        if(lost>0) {
            events.publish("PLAYER_HIT","Герой получил " + lost + " урона",new com.gamelab.application.audio.PlayerDamage(lost,!player.isAlive()));
            if(!player.isAlive()) events.publish("PLAYER_DIED","Герой погиб");
        }
    }
}
