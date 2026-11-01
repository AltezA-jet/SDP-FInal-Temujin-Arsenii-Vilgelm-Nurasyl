package com.gamelab;

import com.gamelab.domain.item.Potion;
import com.gamelab.domain.item.PotionType;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.item.WeaponCatalog;
import com.gamelab.domain.rune.RuneCatalog;
import com.gamelab.domain.rune.RuneType;
import com.gamelab.ui.ItemSpriteResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ItemSpriteResolverTest {

    @Test
    void shouldFindEveryWeaponSprite() {
        for (int level = 1; level <= 5; level++) {
            Weapon weapon =
                    WeaponCatalog.createByLevel(level);

            assertResourceExists(
                    ItemSpriteResolver.resolve(weapon)
            );
        }
    }

    @Test
    void shouldFindEveryRuneSprite() {
        for (RuneType runeType
                : RuneType.values()) {
            assertResourceExists(
                    ItemSpriteResolver.resolve(
                            RuneCatalog.create(runeType)
                    )
            );
        }
    }

    @Test
    void shouldFindPotionAndCoinSprites() {
        Potion potion = new Potion(
                "Health Potion",
                "Restores health.",
                30,
                "ALL",
                PotionType.HEALTH,
                50
        );

        assertResourceExists(
                ItemSpriteResolver.resolve(potion)
        );

        assertResourceExists(
                ItemSpriteResolver.coin()
        );
    }

    private void assertResourceExists(
            String resourcePath
    ) {
        assertNotNull(
                ItemSpriteResolverTest.class
                        .getResource(resourcePath),
                "Missing resource: " + resourcePath
        );
    }
}