package com.gamelab.ui.inventory;

import com.gamelab.domain.inventory.Inventory;
import com.gamelab.ui.components.MenuButton;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/** Separate full-screen inventory menu. */
public final class InventoryScreen extends Pane {
    public static final double W = 1920, H = 1080;

    public InventoryScreen(Inventory inventory, Runnable onBack) {
        setPrefSize(W, H); setMinSize(W, H); setMaxSize(W, H);
        setStyle("-fx-background-color:#100d18;");

        Label title = new Label("INVENTORY");
        title.setTextFill(Color.web("#f2d08a")); title.setFont(Font.font("Serif", 38)); title.relocate(820, 90);

        InventoryView view = new InventoryView(inventory);
        view.relocate(610, 190); view.setPrefSize(700, 620);

        MenuButton back = new MenuButton("BACK", 220, 58);
        back.relocate(850, 900); back.setOnAction(onBack);
        setOnKeyPressed(event -> { if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) onBack.run(); });
        setFocusTraversable(true);
        getChildren().addAll(title, view, back);
        requestFocus();
    }
}
