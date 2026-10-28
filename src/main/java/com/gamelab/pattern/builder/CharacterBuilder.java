
package com.gamelab.pattern.builder;

import com.gamelab.domain.PlayerCharacter;

public class CharacterBuilder {

    private String name = "Hero";
    private String characterClass = "Warrior";
    private int maxHealth = 100;
    private int damage = 15;
    private int defense = 5;

    public CharacterBuilder name(String name) {
        this.name = name;
        return this;
    }

    public CharacterBuilder characterClass(String characterClass) {
        this.characterClass = characterClass;
        return this;
    }

    public CharacterBuilder maxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        return this;
    }

    public CharacterBuilder damage(int damage) {
        this.damage = damage;
        return this;
    }

    public CharacterBuilder defense(int defense) {
        this.defense = defense;
        return this;
    }

    public PlayerCharacter build() {
        return new PlayerCharacter(
                name,
                characterClass,
                maxHealth,
                damage,
                defense
        );
    }

    public static CharacterBuilder warrior(String name) {
        return new CharacterBuilder()
                .name(name)
                .characterClass("Warrior")
                .maxHealth(140)
                .damage(18)
                .defense(8);
    }

    public static CharacterBuilder rogue(String name) {
        return new CharacterBuilder()
                .name(name)
                .characterClass("Rogue")
                .maxHealth(90)
                .damage(24)
                .defense(3);
    }

    public static CharacterBuilder mage(String name) {
        return new CharacterBuilder()
                .name(name)
                .characterClass("Mage")
                .maxHealth(80)
                .damage(30)
                .defense(2);
    }
}
