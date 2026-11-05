package com.gamelab.ui.inventory;

import com.gamelab.domain.Item;
import com.gamelab.domain.inventory.Inventory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.util.Objects;

/** Display-only inventory grid; inventory rules remain in the domain model. */
public final class InventoryView extends BorderPane {
    private final Inventory inventory;
    private final GridPane grid = new GridPane();

    public InventoryView(Inventory inventory) {
        this.inventory = Objects.requireNonNull(inventory);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: rgba(13,10,19,.97); -fx-border-color:#a47b50; -fx-border-width:2;");
        Label title = new Label("INVENTORY");
        title.setStyle("-fx-text-fill:#f2d08a;-fx-font-size:24px;-fx-font-weight:bold;");
        setTop(title);
        grid.setHgap(10); grid.setVgap(10); grid.setAlignment(Pos.CENTER); setCenter(grid);
        refresh();
    }

    public void refresh() {
        grid.getChildren().clear();
        for (int i = 0; i < inventory.getCapacity(); i++) {
            VBox slot = new VBox(5); slot.setAlignment(Pos.CENTER); slot.setPrefSize(112, 72);
            if (i < inventory.getItems().size()) {
                Item item = inventory.getItems().get(i);
                slot.getChildren().add(new Label(item.getName()));
            } else slot.getChildren().add(new Label("Empty"));
            slot.setStyle("-fx-text-fill:#d8c8a8;-fx-background-color:#211b2b;-fx-border-color:#67523d;");
            grid.add(slot, i % 4, i / 4);
        }
    }
}
