package com.gamelab.demo;

import com.gamelab.application.CombatService;
import com.gamelab.application.MinibossRuneRewardService;
import com.gamelab.application.PotionService;
import com.gamelab.application.RewardService;
import com.gamelab.application.RuneCombatService;
import com.gamelab.application.RuneService;
import com.gamelab.application.WeaponUpgradeResult;
import com.gamelab.application.WeaponUpgradeService;
import com.gamelab.domain.Enemy;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.Potion;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.item.WeaponCatalog;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneLoadout;
import com.gamelab.domain.rune.RuneType;
import com.gamelab.domain.shop.PurchaseResult;
import com.gamelab.domain.shop.ShadowfallShopCatalog;
import com.gamelab.domain.shop.Shop;
import com.gamelab.pattern.factory.EnemyFactory;
import com.gamelab.pattern.factory.EnemyType;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.observer.GameLogObserver;
import com.gamelab.pattern.strategy.BalancedStrategy;

public class ItemSystemDemo {

    public static void main(String[] args) {
        System.out.println(
                "=== SHADOWFALL ITEM AND RUNE SYSTEM ==="
        );

        GameEventBus eventBus = new GameEventBus();
        GameLogObserver gameLog =
                new GameLogObserver();

        eventBus.subscribe(gameLog);

        PlayerCharacter player =
                new PlayerCharacter(
                        "Arthur",
                        "KNIGHT",
                        100,
                        20,
                        5
                );

        CoinWallet wallet =
                new CoinWallet(1000);

        Inventory inventory =
                new Inventory(10);

        RewardService rewardService =
                new RewardService(eventBus);

        CombatService combatService =
                new CombatService(
                        eventBus,
                        rewardService,
                        wallet
                );

        RuneCombatService runeCombatService =
                new RuneCombatService(combatService);

        RuneService runeService =
                new RuneService(eventBus);

        MinibossRuneRewardService
                runeRewardService =
                new MinibossRuneRewardService(
                        eventBus
                );

        WeaponUpgradeService upgradeService =
                new WeaponUpgradeService();

        PotionService potionService =
                new PotionService(eventBus);

        Shop shop =
                ShadowfallShopCatalog
                        .createDefaultShop(eventBus);

        Weapon starterSword =
                WeaponCatalog.createByLevel(1);

        inventory.addItem(starterSword);

        inventory.equipWeapon(
                starterSword,
                player.getCharacterClass()
        );

        RuneLoadout loadout =
                new RuneLoadout(starterSword);

        System.out.println(
                "\n--- Starting equipment ---"
        );

        printWeapon(loadout, inventory, wallet);

        System.out.println(
                "\n--- Weapon upgrade ---"
        );

        WeaponUpgradeResult upgradeResult =
                upgradeService.upgrade(
                        loadout,
                        inventory,
                        wallet,
                        player.getCharacterClass()
                );

        System.out.println(
                upgradeResult.message()
        );

        if (upgradeResult.success()) {
            loadout =
                    upgradeResult.runeLoadout();
        }

        printWeapon(loadout, inventory, wallet);

        System.out.println(
                "\n--- Buying Haste Rune ---"
        );

        Rune hasteRune =
                findShopRune(
                        shop,
                        RuneType.HASTE
                );

        PurchaseResult hastePurchase =
                shop.purchase(
                        hasteRune,
                        player.getCharacterClass(),
                        wallet,
                        inventory
                );

        System.out.println(
                hastePurchase.message()
        );

        System.out.println(
                runeService.installRune(
                        hasteRune,
                        loadout,
                        inventory
                ).message()
        );

        System.out.println(
                "\n--- Miniboss reward ---"
        );

        System.out.println(
                runeRewardService.grantStageRune(
                        1,
                        inventory
                ).message()
        );

        Rune fireRune =
                findInventoryRune(
                        inventory,
                        RuneType.FIRE
                );

        System.out.println(
                runeService.installRune(
                        fireRune,
                        loadout,
                        inventory
                ).message()
        );

        printWeapon(loadout, inventory, wallet);

        System.out.println(
                "\n--- Battle against Ice Crawler ---"
        );

        Enemy iceCrawler = EnemyFactory.create(
                EnemyType.ICE_CRAWLER,
                "ice-crawler-1",
                0,
                0
        );

        int firstHit = runeCombatService.attack(
                player,
                iceCrawler,
                new BalancedStrategy(),
                loadout
        );

        int secondHit = runeCombatService.attack(
                player,
                iceCrawler,
                new BalancedStrategy(),
                loadout
        );

        int thirdHit = runeCombatService.attack(
                player,
                iceCrawler,
                new BalancedStrategy(),
                loadout
        );



        System.out.println(
                "First hit: " + firstHit
        );

        System.out.println(
                "Second hit: " + secondHit
        );

        System.out.println(
                "Third hit: " + thirdHit
        );

        System.out.println(
                "Enemy alive: "
                        + iceCrawler.isAlive()
        );

        System.out.println(
                "\n--- Buying and using potion ---"
        );

        Potion healthPotion = shop.getCatalog()
                .stream()
                .filter(Potion.class::isInstance)
                .map(Potion.class::cast)
                .findFirst()
                .orElseThrow();

        PurchaseResult potionPurchase =
                shop.purchase(
                        healthPotion,
                        player.getCharacterClass(),
                        wallet,
                        inventory
                );

        System.out.println(
                potionPurchase.message()
        );

        player.takeDamage(40);

        System.out.println(
                "Health before potion: "
                        + player.getHealth()
        );

        System.out.println(
                potionService.usePotion(
                        healthPotion,
                        player
                ).message()
        );

        inventory.removeItem(healthPotion);

        System.out.println(
                "Health after potion: "
                        + player.getHealth()
        );

        System.out.println(
                "\n--- Final state ---"
        );

        printWeapon(loadout, inventory, wallet);

        System.out.println(
                "Player level: "
                        + player.getLevel()
        );

        System.out.println(
                "Player experience: "
                        + player.getExperience()
        );

        System.out.println(
                "Recorded events: "
                        + gameLog.getEntryCount()
        );
    }

    private static Rune findShopRune(
            Shop shop,
            RuneType runeType
    ) {
        return shop.getCatalog()
                .stream()
                .filter(Rune.class::isInstance)
                .map(Rune.class::cast)
                .filter(rune ->
                        rune.getRuneType()
                                == runeType
                )
                .findFirst()
                .orElseThrow();
    }

    private static Rune findInventoryRune(
            Inventory inventory,
            RuneType runeType
    ) {
        return inventory.getItems()
                .stream()
                .filter(Rune.class::isInstance)
                .map(Rune.class::cast)
                .filter(rune ->
                        rune.getRuneType()
                                == runeType
                )
                .findFirst()
                .orElseThrow();
    }

    private static void printWeapon(
            RuneLoadout loadout,
            Inventory inventory,
            CoinWallet wallet
    ) {
        Weapon decoratedWeapon =
                loadout.createDecoratedWeapon();

        System.out.println(
                "Weapon: "
                        + decoratedWeapon.getName()
        );

        System.out.println(
                "Damage bonus: +"
                        + decoratedWeapon
                        .getDamageBonus()
        );

        System.out.printf(
                "Attack speed: %.2f%n",
                decoratedWeapon.getAttackSpeed()
        );

        System.out.println(
                "Rune slots: "
                        + loadout
                        .getInstalledRunes()
                        .size()
                        + "/"
                        + loadout.getWeapon()
                        .getRuneSlotCount()
        );

        System.out.println(
                "Inventory items: "
                        + inventory.getSize()
        );

        System.out.println(
                "Coins: "
                        + wallet.getBalance()
        );
    }
}