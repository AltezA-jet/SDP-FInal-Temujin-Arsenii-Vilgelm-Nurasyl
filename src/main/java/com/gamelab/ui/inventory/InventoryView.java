package com.gamelab.ui.inventory;

import com.gamelab.domain.Item;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.ui.ShadowfallUi;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.Objects;

/** Display-only inventory grid; inventory rules remain in the domain model. */
public final class InventoryView extends BorderPane {
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private final Inventory inventory;
    private final GridPane grid = new GridPane();
    private Item selected;

    public InventoryView(Inventory inventory) {
        this.inventory = Objects.requireNonNull(inventory);
        ShadowfallUi.install(this);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: rgba(13,10,19,.97); -fx-border-color:#a47b50; -fx-border-width:2;");
        Label title = new Label("INVENTORY");
        title.setStyle("-fx-text-fill:#f2d08a;-fx-font-size:24px;-fx-font-weight:bold;");
        setTop(title);
        grid.setHgap(10); grid.setVgap(10); grid.setAlignment(Pos.TOP_CENTER);
        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        setCenter(scroll);
        refresh();
    }

    public void refresh() {
        grid.getChildren().clear();
        for (int i = 0; i < inventory.getCapacity(); i++) {
            VBox slot = new VBox(5); slot.setAlignment(Pos.CENTER); slot.setPrefSize(142, 136);
            if (i < inventory.getItems().size()) {
                Item item = inventory.getItems().get(i);
                long quantity = inventory.getItems().stream().filter(candidate -> candidate.getClass() == item.getClass()
                        && candidate.getName().equals(item.getName())).count();
                Button icon = new Button(item.getName() + (quantity > 1 ? " ×" + quantity : ""), itemIcon(item));
                icon.setContentDisplay(javafx.scene.control.ContentDisplay.TOP);
                icon.setWrapText(true);
                icon.setPrefSize(128, 74);
                icon.getStyleClass().add("sf-item");
                icon.pseudoClassStateChanged(SELECTED, selected == item);
                icon.setOnAction(event -> { selected = item; refresh(); });
                Label description = new Label(item.getDescription());
                description.getStyleClass().add("sf-muted");
                description.setWrapText(true);
                description.setMaxWidth(128);
                slot.getChildren().addAll(icon, description);
            } else {
                Label empty = new Label("EMPTY SLOT");
                empty.getStyleClass().add("sf-muted");
                slot.getChildren().add(empty);
            }
            slot.getStyleClass().add(i < inventory.getItems().size() ? "sf-panel" : "sf-empty-slot");
            grid.add(slot, i % 4, i / 4);
        }
    }

    private Node itemIcon(Item item) {
        try {
            URL url = getClass().getResource(ItemSpriteResolver.resolve(item));
            if (url != null) {
                Image image = new Image(url.toExternalForm(), 42, 42, true, false, true);
                if (!image.isError()) {
                    ImageView view = new ImageView(image);
                    view.setFitWidth(42); view.setFitHeight(42); view.setPreserveRatio(true); view.setSmooth(false);
                    return view;
                }
            }
        } catch (RuntimeException ignored) { }
        Label fallback = new Label("✦");
        fallback.setTextFill(Color.web("#c6a96d"));
        fallback.setStyle("-fx-font-size: 28px;");
        StackPane placeholder = new StackPane(fallback);
        placeholder.setPrefSize(42, 42);
        placeholder.setStyle("-fx-background-color:#191521; -fx-border-color:#655478;");
        return placeholder;
    }
}
