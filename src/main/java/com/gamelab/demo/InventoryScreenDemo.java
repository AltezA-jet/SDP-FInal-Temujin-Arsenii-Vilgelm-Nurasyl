package com.gamelab.demo;

import com.gamelab.application.RuneService;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.item.WeaponCatalog;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneCatalog;
import com.gamelab.domain.rune.RuneLoadout;
import com.gamelab.domain.rune.RuneType;
import com.gamelab.domain.shop.ShadowfallShopCatalog;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.ui.InventoryController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class InventoryScreenDemo
        extends Application {

    @Override
    public void start(Stage stage) {
        GameEventBus eventBus =
                new GameEventBus();

        eventBus.subscribe(event ->
                System.out.println(
                        "["
                                + event.type()
                                + "] "
                                + event.message()
                )
        );

        Inventory inventory =
                new Inventory(12);

        Weapon weapon =
                WeaponCatalog.createByLevel(2);

        inventory.addItem(weapon);

        inventory.equipWeapon(
                weapon,
                "KNIGHT"
        );

        RuneLoadout loadout =
                new RuneLoadout(weapon);

        Rune fireRune =
                RuneCatalog.create(
                        RuneType.FIRE
                );

        inventory.addItem(fireRune);

        RuneService runeService =
                new RuneService(eventBus);

        runeService.installRune(
                fireRune,
                loadout,
                inventory
        );

        for (Rune rune
                : RuneCatalog.createShopRunes()) {
            inventory.addItem(rune);
        }

        inventory.addItem(
                ShadowfallShopCatalog
                        .createHealthPotion()
        );

        InventoryController controller =
                new InventoryController(
                        inventory,
                        loadout,
                        runeService
                );

        Scene scene = new Scene(
                controller.getView(),
                1100,
                700
        );

        stage.setTitle(
                "Shadowfall Inventory Preview"
        );

        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}