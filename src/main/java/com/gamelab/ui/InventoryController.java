package com.gamelab.ui;

import com.gamelab.application.RuneActionResult;
import com.gamelab.application.RuneService;
import com.gamelab.domain.Item;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.Potion;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneLoadout;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.Objects;

public final class InventoryController {

    private static final String PANEL_STYLE =
            "-fx-background-color: #201b2d;"
                    + "-fx-border-color: #8b6f47;"
                    + "-fx-border-width: 2;"
                    + "-fx-background-radius: 8;"
                    + "-fx-border-radius: 8;";

    private final Inventory inventory;
    private final RuneService runeService;

    private RuneLoadout loadout;

    private final BorderPane root;
    private final VBox weaponPanel;
    private final TilePane inventoryGrid;
    private final HBox runeSlots;
    private final Label statusLabel;

    public InventoryController(
            Inventory inventory,
            RuneLoadout loadout,
            RuneService runeService
    ) {
        this.inventory = Objects.requireNonNull(
                inventory,
                "Inventory cannot be null"
        );

        this.loadout = Objects.requireNonNull(
                loadout,
                "Rune loadout cannot be null"
        );

        this.runeService = Objects.requireNonNull(
                runeService,
                "Rune service cannot be null"
        );

        root = new BorderPane();
        weaponPanel = new VBox(10);
        inventoryGrid = new TilePane();
        runeSlots = new HBox(12);
        statusLabel = new Label();

        configureView();
        refresh();
    }

    public Parent getView() {
        return root;
    }

    public void setLoadout(
            RuneLoadout loadout
    ) {
        this.loadout = Objects.requireNonNull(
                loadout,
                "Rune loadout cannot be null"
        );

        refresh();
    }

    public void refresh() {
        renderWeapon();
        renderInventory();
        renderRuneSlots();
    }

    private void configureView() {
        root.setPadding(new Insets(24));
        root.setStyle(
                "-fx-background-color: #100d18;"
        );

        Label title = new Label("INVENTORY");
        title.setStyle(
                "-fx-text-fill: #f2d49b;"
                        + "-fx-font-size: 28px;"
                        + "-fx-font-weight: bold;"
        );

        BorderPane.setMargin(
                title,
                new Insets(0, 0, 18, 0)
        );

        root.setTop(title);

        weaponPanel.setAlignment(Pos.TOP_CENTER);
        weaponPanel.setPadding(new Insets(18));
        weaponPanel.setPrefWidth(260);
        weaponPanel.setStyle(PANEL_STYLE);

        BorderPane.setMargin(
                weaponPanel,
                new Insets(0, 20, 0, 0)
        );

        root.setLeft(weaponPanel);

        inventoryGrid.setPadding(new Insets(16));
        inventoryGrid.setHgap(12);
        inventoryGrid.setVgap(12);
        inventoryGrid.setPrefColumns(4);
        inventoryGrid.setStyle(PANEL_STYLE);

        ScrollPane scrollPane =
                new ScrollPane(inventoryGrid);

        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
                "-fx-background: transparent;"
                        + "-fx-background-color: transparent;"
        );

        root.setCenter(scrollPane);

        runeSlots.setAlignment(Pos.CENTER_LEFT);
        runeSlots.setPadding(
                new Insets(12, 0, 8, 0)
        );

        statusLabel.setStyle(
                "-fx-text-fill: #d8c8a8;"
                        + "-fx-font-size: 14px;"
        );

        VBox bottom = new VBox(
                6,
                createSectionLabel("RUNE SLOTS"),
                runeSlots,
                statusLabel
        );

        bottom.setPadding(
                new Insets(18, 0, 0, 0)
        );

        root.setBottom(bottom);
    }

    private void renderWeapon() {
        Weapon weapon =
                loadout.createDecoratedWeapon();

        Label weaponName =
                new Label(weapon.getName());

        weaponName.setWrapText(true);
        weaponName.setStyle(
                "-fx-text-fill: #f2d49b;"
                        + "-fx-font-size: 18px;"
                        + "-fx-font-weight: bold;"
        );

        Label damage = new Label(
                "Damage bonus: +"
                        + weapon.getDamageBonus()
        );

        Label speed = new Label(
                String.format(
                        "Attack speed: %.2f",
                        weapon.getAttackSpeed()
                )
        );

        Label level = new Label(
                "Weapon level: "
                        + weapon.getUpgradeLevel()
                        + "/5"
        );

        damage.setStyle(infoStyle());
        speed.setStyle(infoStyle());
        level.setStyle(infoStyle());

        weaponPanel.getChildren().setAll(
                createSectionLabel("EQUIPPED WEAPON"),
                createImageView(weapon, 96),
                weaponName,
                damage,
                speed,
                level
        );
    }

    private void renderInventory() {
        inventoryGrid.getChildren().clear();

        List<Item> items =
                inventory.getItems();

        if (items.isEmpty()) {
            Label empty =
                    new Label("Inventory is empty");

            empty.setStyle(infoStyle());

            inventoryGrid.getChildren().add(
                    empty
            );

            return;
        }

        for (Item item : items) {
            inventoryGrid.getChildren().add(
                    createItemButton(item)
            );
        }
    }

    private Button createItemButton(
            Item item
    ) {
        Button button = new Button(
                item.getName(),
                createImageView(item, 64)
        );

        button.setContentDisplay(
                ContentDisplay.TOP
        );

        button.setWrapText(true);
        button.setPrefSize(130, 110);

        button.setStyle(
                "-fx-background-color: #30283f;"
                        + "-fx-text-fill: #f4e7cf;"
                        + "-fx-border-color: #655478;"
                        + "-fx-border-radius: 6;"
                        + "-fx-background-radius: 6;"
                        + "-fx-cursor: hand;"
        );

        button.setOnAction(event ->
                handleItemClick(item)
        );

        return button;
    }

    private void handleItemClick(
            Item item
    ) {
        if (item instanceof Rune rune) {
            RuneActionResult result =
                    runeService.installRune(
                            rune,
                            loadout,
                            inventory
                    );

            showResult(result);
            refresh();
            return;
        }

        if (item instanceof Weapon) {
            statusLabel.setText(
                    "This is the equipped weapon"
            );
            return;
        }

        if (item instanceof Potion) {
            statusLabel.setText(
                    "Potion can be used during gameplay"
            );
            return;
        }

        statusLabel.setText(
                item.getName() + " selected"
        );
    }

    private void renderRuneSlots() {
        runeSlots.getChildren().clear();

        List<Rune> installedRunes =
                loadout.getInstalledRunes();

        int slotCount =
                loadout.getWeapon()
                        .getRuneSlotCount();

        for (int index = 0;
             index < slotCount;
             index++) {

            if (index < installedRunes.size()) {
                Rune rune =
                        installedRunes.get(index);

                runeSlots.getChildren().add(
                        createInstalledRuneButton(
                                rune
                        )
                );
            } else {
                runeSlots.getChildren().add(
                        createEmptyRuneSlot()
                );
            }
        }
    }

    private Button createInstalledRuneButton(
            Rune rune
    ) {
        Button button = new Button(
                rune.getName(),
                createImageView(rune, 48)
        );

        button.setContentDisplay(
                ContentDisplay.TOP
        );

        button.setPrefSize(120, 90);

        button.setStyle(
                "-fx-background-color: #49365f;"
                        + "-fx-text-fill: white;"
                        + "-fx-border-color: #b58bdb;"
                        + "-fx-border-radius: 6;"
                        + "-fx-background-radius: 6;"
                        + "-fx-cursor: hand;"
        );

        button.setOnAction(event -> {
            RuneActionResult result =
                    runeService.removeRune(
                            rune.getRuneType(),
                            loadout,
                            inventory
                    );

            showResult(result);
            refresh();
        });

        return button;
    }

    private VBox createEmptyRuneSlot() {
        Label symbol = new Label("+");
        symbol.setStyle(
                "-fx-text-fill: #776b86;"
                        + "-fx-font-size: 28px;"
        );

        Label text = new Label("Empty");
        text.setStyle(
                "-fx-text-fill: #776b86;"
        );

        VBox slot = new VBox(
                4,
                symbol,
                text
        );

        slot.setAlignment(Pos.CENTER);
        slot.setPrefSize(120, 90);

        slot.setStyle(
                "-fx-background-color: #191521;"
                        + "-fx-border-color: #463d52;"
                        + "-fx-border-radius: 6;"
                        + "-fx-background-radius: 6;"
        );

        return slot;
    }

    private ImageView createImageView(
            Item item,
            double size
    ) {
        String path =
                ItemSpriteResolver.resolve(item);

        URL resource =
                getClass().getResource(path);

        if (resource == null) {
            throw new IllegalStateException(
                    "Missing sprite: " + path
            );
        }

        Image image = new Image(
                resource.toExternalForm(),
                size,
                size,
                false,
                false
        );

        ImageView imageView =
                new ImageView(image);

        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setSmooth(false);

        return imageView;
    }

    private Label createSectionLabel(
            String text
    ) {
        Label label = new Label(text);

        label.setStyle(
                "-fx-text-fill: #c6a96d;"
                        + "-fx-font-size: 14px;"
                        + "-fx-font-weight: bold;"
        );

        return label;
    }

    private void showResult(
            RuneActionResult result
    ) {
        statusLabel.setText(
                result.message()
        );

        if (result.success()) {
            statusLabel.setStyle(
                    "-fx-text-fill: #79d28b;"
                            + "-fx-font-size: 14px;"
            );
        } else {
            statusLabel.setStyle(
                    "-fx-text-fill: #e27676;"
                            + "-fx-font-size: 14px;"
            );
        }
    }

    private String infoStyle() {
        return "-fx-text-fill: #d8c8a8;"
                + "-fx-font-size: 14px;";
    }
}