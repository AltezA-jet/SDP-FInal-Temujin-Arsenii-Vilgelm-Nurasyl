package com.gamelab.domain;

import com.gamelab.pattern.strategy.EnemyBehavior;
import java.util.Objects;

/** Domain model for an enemy. Keeps combat data independent from JavaFX. */
public final class Enemy {
    private final String id, name;
    private final int maxHealth, damage, rewardCoins, rewardExperience;
    private final double movementSpeed;
    private final EnemyElement element;
    private final boolean boss;
    private int health;
    private double x, y;
    private EnemyBehavior behavior;
    private boolean rewardClaimed;

    /** Backward-compatible constructor for existing tests and callers. */
    public Enemy(String id, String name, int maxHealth, int damage, double x, double y,
                 double movementSpeed, int rewardCoins, int rewardExperience, EnemyBehavior behavior) {
        this(id,name,maxHealth,damage,x,y,movementSpeed,rewardCoins,rewardExperience,EnemyElement.NORMAL,false,behavior);
    }
    public Enemy(String id, String name, int maxHealth, int damage, double x, double y,
                 double movementSpeed, int rewardCoins, int rewardExperience, EnemyElement element,
                 boolean isBoss, EnemyBehavior behavior) {
        if(id==null||id.isBlank()||name==null||name.isBlank()) throw new IllegalArgumentException("Enemy id and name are required");
        if(maxHealth<=0||damage<0||movementSpeed<0||rewardCoins<0||rewardExperience<0) throw new IllegalArgumentException("Invalid enemy stats");
        if(!Double.isFinite(x)||!Double.isFinite(y)) throw new IllegalArgumentException("Position must be finite");
        this.id=id;this.name=name;this.maxHealth=maxHealth;this.health=maxHealth;this.damage=damage;this.x=x;this.y=y;
        this.movementSpeed=movementSpeed;this.rewardCoins=rewardCoins;this.rewardExperience=rewardExperience;
        this.element=Objects.requireNonNull(element);this.boss=isBoss;this.behavior=Objects.requireNonNull(behavior);
    }
    public void takeDamage(int amount){if(amount<0)throw new IllegalArgumentException("Damage cannot be negative");health=Math.max(0,health-amount);}
    public void heal(int amount){if(amount<0)throw new IllegalArgumentException("Healing cannot be negative");health=Math.min(maxHealth,health+amount);}
    public void update(double px,double py,double dt){if(!Double.isFinite(dt)||dt<0)throw new IllegalArgumentException("Delta must be finite and non-negative");if(isAlive())behavior.update(this,px,py,dt);}
    public void moveTowards(double tx,double ty,double dt){double dx=tx-x,dy=ty-y,d=Math.hypot(dx,dy);if(d==0||dt==0)return;double step=Math.min(d,movementSpeed*dt);x+=dx/d*step;y+=dy/d*step;}
    public void moveAway(double tx,double ty,double dt){double dx=x-tx,dy=y-ty,d=Math.hypot(dx,dy);if(d==0||dt==0)return;double step=movementSpeed*dt;x+=dx/d*step;y+=dy/d*step;}
    public void setBehavior(EnemyBehavior b){behavior=Objects.requireNonNull(b);}
    /** Returns true only once, allowing CombatService to award a kill exactly once. */
    public boolean claimReward(){if(isAlive()||rewardClaimed)return false;rewardClaimed=true;return true;}
    public String getId(){return id;} public String getName(){return name;} public int getHealth(){return health;}
    public int getMaxHealth(){return maxHealth;} public int getDamage(){return damage;} public double getX(){return x;} public double getY(){return y;}
    public double getMovementSpeed(){return movementSpeed;} public int getRewardCoins(){return rewardCoins;} public int getRewardExperience(){return rewardExperience;}
    public EnemyBehavior getBehavior(){return behavior;} public boolean isAlive(){return health>0;}
    public EnemyElement getElement(){return element;} public boolean isBoss(){return boss;}
}
