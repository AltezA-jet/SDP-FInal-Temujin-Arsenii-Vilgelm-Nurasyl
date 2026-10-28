package com.gamelab.demo;

import com.gamelab.application.PotionService;
import com.gamelab.application.RewardService;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.BasicWeapon;
import com.gamelab.domain.item.Equipment;
import com.gamelab.domain.item.EquipmentType;
import com.gamelab.domain.item.Potion;
import com.gamelab.domain.item.PotionType;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.shop.PurchaseResult;
import com.gamelab.domain.shop.Shop;
import com.gamelab.pattern.decorator.FireWeaponDecorator;
import com.gamelab.pattern.decorator.PoisonWeaponDecorator;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.observer.GameLogObserver;

public class ItemSystemDemo {

    public static void main(String[] args) {

        System.out.println(
                "=== SHADOWFALL ITEM SYSTEM DEMO ==="
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

        CoinWallet wallet = new CoinWallet(200);
        Inventory inventory = new Inventory(10);

        RewardService rewardService =
                new RewardService(eventBus);

        PotionService potionService =
                new PotionService(eventBus);

        Shop shop = new Shop(eventBus);

        Weapon basicSword = new BasicWeapon(
                "Iron Sword",
                "Basic knight weapon.",
                100,
                "KNIGHT",
                10
        );

        Weapon upgradedSword =
                new PoisonWeaponDecorator(
                        new FireWeaponDecorator(
                                basicSword
                        )
                );

        Potion healthPotion = new Potion(
                "Health Potion",
                "Restores 50 health points.",
                30,
                "ALL",
                PotionType.HEALTH,
                50
        );

        Equipment shield = new Equipment(
                "Iron Shield",
                "Strong tank and knight shield.",
                120,
                "KNIGHT",
                EquipmentType.SHIELD,
                15,
                20
        );

        shop.addItem(upgradedSword);
        shop.addItem(healthPotion);
        shop.addItem(shield);

        System.out.println(
                "\n--- Enemy reward ---"
        );

        rewardService.grantEnemyReward(
                "Skeleton",
                50,
                20,
                wallet,
                player
        );

        System.out.println(
                "Coins: " + wallet.getBalance()
        );

        System.out.println(
                "\n--- Buying upgraded weapon ---"
        );

        PurchaseResult weaponPurchase =
                shop.purchase(
                        upgradedSword,
                        player.getCharacterClass(),
                        wallet,
                        inventory
                );

        System.out.println(
                weaponPurchase.message()
        );

        inventory.equipWeapon(
                upgradedSword,
                player.getCharacterClass()
        );

        System.out.println(
                "Equipped weapon: "
                        + inventory
                        .getEquippedWeapon()
                        .orElseThrow()
                        .getName()
        );

        System.out.println(
                "Weapon damage bonus: +"
                        + inventory
                        .getWeaponDamageBonus()
        );

        System.out.println(
                "\n--- Buying and using potion ---"
        );

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

        player.takeDamage(60);

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
                "\n--- Item pickup ---"
        );

        rewardService.pickupItem(
                shield,
                inventory
        );

        inventory.equipEquipment(
                shield,
                player.getCharacterClass()
        );

        System.out.println(
                "Defense bonus from equipment: +"
                        + inventory
                        .getTotalDefenseBonus()
        );

        System.out.println(
                "Health bonus from equipment: +"
                        + inventory
                        .getTotalHealthBonus()
        );

        System.out.println(
                "\n--- Wave and boss ---"
        );

        rewardService.completeWave(5);

        rewardService.grantBossReward(
                "Shadow Knight",
                200,
                150,
                wallet,
                player
        );

        System.out.println(
                "\n--- Final state ---"
        );

        System.out.println(
                "Coins: " + wallet.getBalance()
        );

        System.out.println(
                "Level: " + player.getLevel()
        );

        System.out.println(
                "Experience: "
                        + player.getExperience()
        );

        System.out.println(
                "Inventory items: "
                        + inventory.getSize()
        );

        System.out.println(
                "Recorded events: "
                        + gameLog.getEntryCount()
        );
    }
}