package com.gamelab.ui.inventory;

import com.gamelab.domain.Item;
import com.gamelab.domain.inventory.Inventory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
import java.util.Objects;

/** Display-only inventory grid; domain decisions stay in Inventory. */
public final class InventoryView extends BorderPane {
    private final Inventory inventory;
    private final GridPane grid = new GridPane();
    private final Label status = new Label();
    public InventoryView(Inventory inventory) {
        this.inventory = Objects.requireNonNull(inventory);
        setPadding(new Insets(24)); setStyle("-fx-background-color: rgba(13,10,19,.97); -fx-border-color:#a47b50; -fx-border-width:2;");
        Label title = new Label("INVENTORY  •  " + inventory.getOwnerClass());
        title.setStyle("-fx-text-fill:#f2d08a;-fx-font-size:24px;-fx-font-weight:bold;"); setTop(title);
        grid.setHgap(10); grid.setVgap(10); grid.setAlignment(Pos.CENTER); setCenter(grid);
        status.setStyle("-fx-text-fill:#d8c8a8;-fx-font-size:14px;");
        Label limits = new Label("Slots: " + inventory.getSize() + " / " + inventory.getCapacity() + "    Weight limit: " + (inventory.getRules().maxWeight() == Double.MAX_VALUE ? "unlimited" : inventory.getRules().maxWeight()));
        limits.setStyle("-fx-text-fill:#c6a96d;-fx-font-size:13px;"); setBottom(new VBox(6, limits, status));
        refresh();
    }
    public Parent getView() { return this; }
    public void showRejection(Item item) { status.setText(inventory.rejectionReason(item)); }
    public void refresh() {
        grid.getChildren().clear(); Item[] slots = inventory.getSlots();
        for (int i=0;i<slots.length;i++) {
            VBox content = new VBox(5); content.setAlignment(Pos.CENTER); content.setPrefSize(112,92);
            Item item=slots[i];
            if(item==null) content.getChildren().add(new Label("Empty"));
            else { Node icon = iconFor(item); Label name=new Label(item.getName()); name.setWrapText(true); content.getChildren().addAll(icon,name); }
            content.setStyle("-fx-text-fill:#d8c8a8;-fx-background-color:#211b2b;-fx-border-color:#67523d;-fx-border-radius:4;-fx-background-radius:4;");
            grid.add(content,i%4,i/4);
        }
    }
    private Node iconFor(Item item) {
        try {
            URL path=getClass().getResource(ItemSpriteResolver.resolve(item));
            if(path!=null){ImageView image=new ImageView(new Image(path.toExternalForm(),40,40,true,false)); image.setFitWidth(40); image.setFitHeight(40); return image;}
        } catch (IllegalArgumentException ignored) { }
        Label fallback=new Label("◆"); fallback.setStyle("-fx-text-fill:#c6a96d;-fx-font-size:20px;"); return fallback;
    }
}
