package com.gamelab.ui.inventory;

import com.gamelab.application.RuneService;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.rune.RuneLoadout;
import com.gamelab.ui.components.MenuButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;

/** Separate full-screen inventory menu. */
public final class InventoryScreen extends Pane {
    public static final double W = 1920, H = 1080;

    public InventoryScreen(
            Inventory inventory,
            RuneLoadout loadout,
            RuneService runeService,
            Runnable onBack
    ) {
        this(inventory,loadout,runeService,onBack,"KNIGHT");
    }
    public InventoryScreen(Inventory inventory,RuneLoadout loadout,RuneService runeService,
                           Runnable onBack,String characterClass) {
        setPrefSize(W, H); setMinSize(W, H); setMaxSize(W, H);
        setStyle("-fx-background-color:#08050e;");

        URL backgroundResource = InventoryScreen.class.getResource(
                "/assets/ui/backgrounds/inventory_armory.png"
        );

        if (backgroundResource != null) {
            ImageView background = new ImageView(
                    new Image(backgroundResource.toExternalForm())
            );
            background.setFitWidth(W);
            background.setFitHeight(H);
            background.setPreserveRatio(false);
            background.setSmooth(false);
            getChildren().add(background);
        }

        Rectangle veil = new Rectangle(
                W,
                H,
                Color.rgb(6, 4, 12, .48)
        );
        getChildren().add(veil);

        InventoryController controller = new InventoryController(
                inventory,
                loadout,
                runeService,
                characterClass
        );
        StackPane view = new StackPane(controller.getView());
        view.relocate(180, 90);
        view.setPrefSize(1560, 820);
        view.setMaxSize(1560, 820);

        MenuButton back = new MenuButton("НАЗАД", 220, 58);
        back.relocate(850, 940); back.setOnAction(onBack);
        setOnKeyPressed(event -> { if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) onBack.run(); });
        setFocusTraversable(true);
        getChildren().addAll(view, back);
        requestFocus();
    }
}
