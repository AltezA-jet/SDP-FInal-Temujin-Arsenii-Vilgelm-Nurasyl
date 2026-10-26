
package com.gamelab;

import com.gamelab.domain.PlayerCharacter;
import com.gamelab.pattern.builder.CharacterBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharacterBuilderTest {

    @Test
    void shouldCreateWarriorWithCorrectStats() {
        PlayerCharacter player = CharacterBuilder
                .warrior("Shadow")
                .build();

        assertEquals("Shadow", player.getName());
        assertEquals("Warrior", player.getCharacterClass());
        assertEquals(140, player.getMaxHealth());
        assertEquals(140, player.getHealth());
        assertEquals(18, player.getDamage());
        assertEquals(8, player.getDefense());
    }

    @Test
    void shouldCreateMageWithCorrectStats() {
        PlayerCharacter player = CharacterBuilder
                .mage("Merlin")
                .build();

        assertEquals("Mage", player.getCharacterClass());
        assertEquals(80, player.getMaxHealth());
        assertEquals(30, player.getDamage());
    }

    @Test
    void shouldReduceHealthWhenTakingDamage() {
        PlayerCharacter player = CharacterBuilder
                .warrior("Shadow")
                .build();

        player.takeDamage(20);

        assertEquals(128, player.getHealth());
    }

    @Test
    void shouldNotReduceHealthBelowZero() {
        PlayerCharacter player = CharacterBuilder
                .warrior("Shadow")
                .build();

        player.takeDamage(1000);

        assertEquals(0, player.getHealth());
        assertFalse(player.isAlive());
    }

    @Test
    void shouldNotHealAboveMaxHealth() {
        PlayerCharacter player = CharacterBuilder
                .warrior("Shadow")
                .build();

        player.takeDamage(40);
        player.heal(1000);

        assertEquals(player.getMaxHealth(), player.getHealth());
    }

    @Test
    void shouldLevelUpWhenEnoughExperienceIsGained() {
        PlayerCharacter player = CharacterBuilder
                .warrior("Shadow")
                .build();

        player.gainExperience(100);

        assertEquals(2, player.getLevel());
        assertEquals(2, player.getStatPoints());
        assertEquals(20, player.getDamage());
        assertEquals(150, player.getMaxHealth());
        assertEquals(150, player.getHealth());
    }

    @Test
    void shouldRejectInvalidCharacterName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PlayerCharacter("", "Warrior", 100, 10, 5)
        );
    }
}
