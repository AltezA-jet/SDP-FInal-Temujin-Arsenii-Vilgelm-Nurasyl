
package com.gamelab.domain;

public class PlayerCharacter {

    private final String name;
    private final String characterClass;

    private int health;
    private int maxHealth;
    private int damage;
    private final int baseDamage;
    private int defense;

    private int level;
    private int experience;
    private int statPoints;
    private int skillPoints;
    private int healthRank, attackRank, speedRank, resourcesRank;
    private int hairColorIndex;
    private int shirtColorIndex;
    private int pantsColorIndex;
    private int bootsColorIndex;
    private int headwearIndex = -1;
    private int healthBonus, attackBonus;
    private final int baseMana;
    private int mana;
    private final java.util.Map<String, Integer> skillRanks = new java.util.LinkedHashMap<>();
    public static final int MAX_LEVEL = 15;

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
        this.baseDamage = damage;
        this.defense = defense;
        this.baseMana = classIndex() == 2 ? 120 : 0;
        this.mana = baseMana;

        this.level = 1;
        this.experience = 0;
        this.statPoints = 0;
        this.skillPoints = 0;
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

        while (amount > 0 && level < MAX_LEVEL) {
            int needed = experienceToNextLevel();
            int gained = Math.min(amount, needed - experience);
            experience += gained;
            amount -= gained;
            if (experience >= needed) {
                experience = 0;
                level++;
                statPoints += 2;
                skillPoints++;
            }
        }
    }

    public int experienceToNextLevel() { return level >= MAX_LEVEL ? 0 : level * 100; }

    /** Awards stage XP and the separate end-of-stage currencies defined by the GDD. */
    public void completeStage(com.gamelab.domain.progression.CampaignBalance.Stage stage,
                              com.gamelab.domain.progression.Difficulty difficulty) {
        if (stage == null || difficulty == null) throw new IllegalArgumentException("Stage and difficulty are required");
        gainExperience((int)Math.round(stage.experience() * difficulty.experienceMultiplier()));
        statPoints += com.gamelab.domain.progression.CampaignBalance.clearStatReward(difficulty);
        skillPoints += com.gamelab.domain.progression.CampaignBalance.clearSkillReward(difficulty);
    }

    public boolean upgradeStat(String branch) {
        int rank = switch (branch.toLowerCase(java.util.Locale.ROOT)) {
            case "health", "здоровье" -> healthRank;
            case "attack", "сила" -> attackRank;
            case "speed", "ловкость" -> speedRank;
            case "resources", "разум" -> resourcesRank;
            default -> throw new IllegalArgumentException("Unknown stat branch: " + branch);
        };
        int cost = rank < 3 ? 1 : rank < 6 ? 2 : rank < 9 ? 3 : 4;
        if (rank >= 12 || statPoints < cost) return false;
        statPoints -= cost;
        switch (branch.toLowerCase(java.util.Locale.ROOT)) {
            case "health", "здоровье" -> { int amount=healthPerRank(); healthRank++; healthBonus += amount; maxHealth += amount; health = Math.min(maxHealth, health + amount); }
            case "attack", "сила" -> { attackRank++; attackBonus += attackPercentPerRank(); damage += Math.round(baseDamage * attackPercentPerRank() / 100.0f); }
            case "speed", "ловкость" -> speedRank++;
            case "resources", "разум" -> resourcesRank++;
        }
        return true;
    }

    public boolean upgradeSkill(String skillId, int maximumRank, int cost, int requiredLevel) {
        if (skillId == null || maximumRank < 1 || cost < 0) throw new IllegalArgumentException("Invalid skill upgrade");
        if (level < requiredLevel || skillPoints < cost) return false;
        int rank = skillRanks.getOrDefault(skillId, 0);
        if (rank >= maximumRank) return false;
        skillPoints -= cost;
        skillRanks.put(skillId, rank + 1);
        return true;
    }

    public int getSkillRank(String skillId) { return skillRanks.getOrDefault(skillId, 0); }

    /** Sandbox-only helper: makes every node of this hero's own class available for combat testing. */
    public void unlockClassSkillsForDebug() {
        String classId = switch (classIndex()) {
            case 1 -> "archer";
            case 2 -> "mage";
            case 3 -> "assassin";
            default -> "warrior";
        };
        for (int index = 0; index < 5; index++) skillRanks.put(classId + "." + index, 3);
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
    public int getSkillPoints() { return skillPoints; }
    public int getHealthRank() { return healthRank; }
    public int getAttackRank() { return attackRank; }
    public int getSpeedRank() { return speedRank; }
    public int getResourcesRank() { return resourcesRank; }
    public void setAppearance(int hairColorIndex, int outfitIndex, int headwearIndex) {
        setAppearance(hairColorIndex, outfitIndex, outfitIndex, outfitIndex, headwearIndex);
    }
    public void setAppearance(int hairColorIndex, int shirtColorIndex, int pantsColorIndex,
                              int bootsColorIndex, int headwearIndex) {
        this.hairColorIndex = hairColorIndex;
        this.shirtColorIndex = shirtColorIndex;
        this.pantsColorIndex = pantsColorIndex;
        this.bootsColorIndex = bootsColorIndex;
        this.headwearIndex = headwearIndex;
    }
    public int getHairColorIndex() { return hairColorIndex; }
    /** Legacy accessor: older screens treated all three clothing layers as one outfit. */
    public int getOutfitIndex() { return shirtColorIndex; }
    public int getShirtColorIndex() { return shirtColorIndex; }
    public int getPantsColorIndex() { return pantsColorIndex; }
    public int getBootsColorIndex() { return bootsColorIndex; }
    public int getHeadwearIndex() { return headwearIndex; }
    public int getAttackBonusPercent() { return attackBonus; }
    public int getHealthBonus() { return healthBonus; }
    public double getAttackSpeedBonusPercent() { return speedRank * switch (classIndex()) { case 0->1.5; case 1,2->2.5; default->3.0; }; }
    public int getCritChanceBonusPercent() { return speedRank * (classIndex()==1||classIndex()==3 ? 2 : 0); }
    public int getResourceRegenerationBonusPercent() { return resourcesRank * 4; }
    public int getBonusResourceAmount() { return resourcesRank * switch(classIndex()){case 0->8;case 2->6;default->0;}; }
    public int getResourceCooldownReductionPercent() { return classIndex()==1 ? resourcesRank*2 : 0; }
    public int getInvisibilityCostReductionPercent() { return classIndex()==3 ? resourcesRank*1 : 0; }
    public int getMaxMana() { return baseMana + getBonusResourceAmount(); }
    public int getMana() { return mana; }
    public boolean spendMana(int amount) {
        if (amount < 0 || mana < amount) return false;
        mana -= amount;
        return true;
    }
    public void restoreMana(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Mana restoration cannot be negative");
        mana = Math.min(getMaxMana(), mana + amount);
    }
    public void restoreFullMana() { mana = getMaxMana(); }

    private int healthPerRank() { return switch(classIndex()){case 0->15;case 1->10;default->8;}; }
    private int attackPercentPerRank() { return switch(classIndex()){case 0->8;case 1,3->6;default->3;}; }
    private int classIndex() {
        String c=characterClass.toUpperCase(java.util.Locale.ROOT);
        if(c.contains("WARRIOR")||c.contains("Р’Рѕ"))return 0;
        if(c.contains("ARCHER")||c.contains("Р›Сѓ"))return 1;
        if(c.contains("MAGE")||c.contains("РњР°"))return 2;
        if(c.contains("ASSASSIN")||c.contains("РђСЃ"))return 3;
        return 0;
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
