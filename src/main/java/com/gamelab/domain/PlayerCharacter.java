
package com.gamelab.domain;

public class PlayerCharacter {

    private final String name;
    private final String characterClass;

    private int health;
    private int maxHealth;
    private int damage;
    private int defense;

    private int level;
    private int experience;
    private int statPoints;

    public PlayerCharacter(
            String name,
            String characterClass,
            int maxHealth,
            int damage,
            int defense
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Character name cannot be empty");
        }

        if (characterClass == null || characterClass.isBlank()) {
            throw new IllegalArgumentException("Character class cannot be empty");
        }

        if (maxHealth <= 0 || damage < 0 || defense < 0) {
            throw new IllegalArgumentException("Invalid character stats");
        }

        this.name = name;
        this.characterClass = characterClass;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.damage = damage;
        this.defense = defense;

        this.level = 1;
        this.experience = 0;
        this.statPoints = 0;
    }

    public void takeDamage(int incomingDamage) {
        if (incomingDamage < 0) {
            throw new IllegalArgumentException("Damage cannot be negative");
        }

        int actualDamage = Math.max(0, incomingDamage - defense);
        health = Math.max(0, health - actualDamage);
    }

    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Healing cannot be negative");
        }

        health = Math.min(maxHealth, health + amount);
    }

    public void gainExperience(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Experience cannot be negative");
        }

        experience += amount;

        while (experience >= level * 100) {
            experience -= level * 100;
            level++;
            statPoints += 2;
            damage += 2;
            maxHealthIncrease();
        }
    }

    private void maxHealthIncrease() {
        maxHealth += 10;
        health = Math.min(maxHealth, health + 10);
    }

    public void increaseDefense(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Defense increase cannot be negative");
        }

        defense += amount;
    }

    public void increaseDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage increase cannot be negative");
        }

        damage += amount;
    }

    public String getName() {
        return name;
    }

    public String getCharacterClass() {
        return characterClass;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getDamage() {
        return damage;
    }

    public int getDefense() {
        return defense;
    }

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public int getStatPoints() {
        return statPoints;
    }

    public boolean isAlive() {
        return health > 0;
    }
    
    public void spendStatPointOnDamage() {
        if (statPoints <= 0) {
            throw new IllegalStateException("No stat points available");
        }

        statPoints--;
        damage += 2;
    }

    public void spendStatPointOnDefense() {
        if (statPoints <= 0) {
            throw new IllegalStateException("No stat points available");
        }

        statPoints--;
        defense += 2;
    }
}
