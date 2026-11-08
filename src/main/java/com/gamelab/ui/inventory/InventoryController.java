package com.gamelab.ui.inventory;

import com.gamelab.application.RuneActionResult;
import com.gamelab.application.RuneService;
import com.gamelab.domain.Item;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.Potion;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.rune.RuneLoadout;
import com.gamelab.ui.ShadowfallUi;
import com.gamelab.ui.components.MenuButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.Objects;

/** Interactive campaign inventory: equipment, item grid, rune sockets and item details. */
public final class InventoryController {

    private static final String FRAME_STYLE =
            "-fx-background-color:linear-gradient(to bottom,rgba(14,9,23,.97),rgba(7,5,13,.95));"
                    + "-fx-border-color:#c1843d;"
                    + "-fx-border-width:3;"
                    + "-fx-background-radius:14;"
                    + "-fx-border-radius:14;"
                    + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,.88),32,.35,0,12);";

    private static final String PANEL_STYLE =
            "-fx-background-color:linear-gradient(to bottom,rgba(33,25,45,.97),rgba(17,13,27,.97));"
                    + "-fx-border-color:#9f713d;"
                    + "-fx-border-width:2;"
                    + "-fx-background-radius:10;"
                    + "-fx-border-radius:10;"
                    + "-fx-effect:innershadow(gaussian,rgba(0,0,0,.68),16,.25,0,3);";

    private static final String EMPTY_SLOT_STYLE =
            "-fx-background-color:linear-gradient(to bottom,rgba(20,15,29,.95),rgba(12,9,19,.95));"
                    + "-fx-border-color:#554360;"
                    + "-fx-border-width:2;"
                    + "-fx-background-radius:7;"
                    + "-fx-border-radius:7;"
                    + "-fx-effect:innershadow(gaussian,rgba(0,0,0,.72),10,.20,0,2);";

    private enum Filter {
        ALL,
        WEAPONS,
        RUNES,
        CONSUMABLES
    }

    private final Inventory inventory;
    private final RuneService runeService;
    private RuneLoadout loadout;

    private final BorderPane root = new BorderPane();
    private final VBox weaponPanel = new VBox(10);
    private final TilePane inventoryGrid = new TilePane();
    private final TilePane runeSlots = new TilePane();
    private final VBox detailsPanel = new VBox(12);
    private final Label capacityLabel = new Label();
    private final Label statusLabel = new Label("Выберите предмет");
    private final MenuButton[] filterButtons = new MenuButton[4];

    private Filter filter = Filter.ALL;
    private Item selectedItem;

    public InventoryController(
            Inventory inventory,
            RuneLoadout loadout,
            RuneService runeService
    ) {
        this.inventory = Objects.requireNonNull(inventory, "Inventory cannot be null");
        this.loadout = Objects.requireNonNull(loadout, "Rune loadout cannot be null");
        this.runeService = Objects.requireNonNull(runeService, "Rune service cannot be null");
        configureView();
        refresh();
    }

    public Parent getView() {
        return root;
    }

    public void setLoadout(RuneLoadout loadout) {
        this.loadout = Objects.requireNonNull(loadout, "Rune loadout cannot be null");
        selectedItem = null;
        refresh();
    }

    public void refresh() {
        capacityLabel.setText(inventory.getSize() + " / " + inventory.getCapacity());
        renderWeapon();
        renderRuneSlots();
        renderInventory();
        renderDetails();
        refreshFilterButtons();
    }

    private void configureView() {
        ShadowfallUi.install(root);
        root.setPadding(new Insets(20));
        root.setStyle(FRAME_STYLE);
        resetStatus();

        Label title = new Label("ИНВЕНТАРЬ");
        title.setStyle(
                "-fx-text-fill:#f6cf7a;"
                        + "-fx-font-family:'Monospaced';"
                        + "-fx-font-size:34px;"
                        + "-fx-font-weight:bold;"
        );

        capacityLabel.setStyle(
                "-fx-text-fill:#ffe29b;"
                        + "-fx-font-family:'Monospaced';"
                        + "-fx-font-size:22px;"
                        + "-fx-font-weight:bold;"
                        + "-fx-background-color:rgba(16,11,24,.95);"
                        + "-fx-border-color:#9a6738;"
                        + "-fx-border-width:2;"
                        + "-fx-padding:8 18 8 18;"
                        + "-fx-background-radius:6;"
                        + "-fx-border-radius:6;"
        );

        StackPane header = new StackPane(title, capacityLabel);
        header.setPrefHeight(56);
        StackPane.setAlignment(title, Pos.CENTER);
        StackPane.setAlignment(capacityLabel, Pos.CENTER_RIGHT);

        weaponPanel.setPrefSize(300, 650);
        weaponPanel.setMaxSize(300, 650);
        weaponPanel.setAlignment(Pos.TOP_CENTER);
        weaponPanel.setPadding(new Insets(18));
        weaponPanel.setStyle(PANEL_STYLE);

        VBox inventoryPanel = new VBox(12);
        inventoryPanel.setPrefSize(680, 650);
        inventoryPanel.setMaxSize(680, 650);
        inventoryPanel.setPadding(new Insets(16));
        inventoryPanel.setStyle(PANEL_STYLE);

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER);
        String[] names = {"ВСЕ", "ОРУЖИЕ", "РУНЫ", "РАСХОДНИКИ"};
        Filter[] values = Filter.values();

        for (int index = 0; index < names.length; index++) {
            int currentIndex = index;
            MenuButton button = new MenuButton(names[index], 145, 44);
            button.setOnAction(() -> {
                filter = values[currentIndex];
                renderInventory();
                refreshFilterButtons();
            });
            filterButtons[index] = button;
            filters.getChildren().add(button);
        }

        inventoryGrid.setPrefColumns(4);
        inventoryGrid.setHgap(10);
        inventoryGrid.setVgap(10);
        inventoryGrid.setAlignment(Pos.TOP_CENTER);
        inventoryGrid.setPadding(new Insets(12));
        inventoryGrid.setStyle(
                "-fx-background-color:rgba(11,8,18,.72);"
                        + "-fx-border-color:#5f4935;"
                        + "-fx-border-width:2;"
                        + "-fx-background-radius:7;"
                        + "-fx-border-radius:7;"
        );

        inventoryPanel.getChildren().addAll(filters, inventoryGrid);

        detailsPanel.setPrefSize(320, 650);
        detailsPanel.setMaxSize(320, 650);
        detailsPanel.setAlignment(Pos.TOP_CENTER);
        detailsPanel.setPadding(new Insets(20));
        detailsPanel.setStyle(PANEL_STYLE);

        HBox content = new HBox(18, weaponPanel, inventoryPanel, detailsPanel);
        content.setAlignment(Pos.TOP_CENTER);

        VBox shell = new VBox(14, header, content);
        root.setCenter(shell);
    }

    private void renderWeapon() {
        Weapon baseWeapon = loadout.getWeapon();
        Weapon decoratedWeapon = loadout.createDecoratedWeapon();

        Label weaponName = new Label(displayName(baseWeapon));
        weaponName.setWrapText(true);
        weaponName.setAlignment(Pos.CENTER);
        weaponName.setMaxWidth(250);
        weaponName.setStyle(
                "-fx-text-fill:#ffe2a1;"
                        + "-fx-font-family:'Monospaced';"
                        + "-fx-font-size:19px;"
                        + "-fx-font-weight:bold;"
        );

        Label damage = infoLabel("Урон: +" + decoratedWeapon.getDamageBonus());
        Label speed = infoLabel(String.format("Скорость: %.2f", decoratedWeapon.getAttackSpeed()));
        Label level = infoLabel("Уровень: " + decoratedWeapon.getUpgradeLevel() + "/5");

        runeSlots.setAlignment(Pos.CENTER);
        runeSlots.setPadding(new Insets(6, 0, 0, 0));
        runeSlots.setPrefColumns(2);
        runeSlots.setHgap(12);
        runeSlots.setVgap(12);

        weaponPanel.getChildren().setAll(
                sectionLabel("ЭКИПИРОВАННЫЙ МЕЧ"),
                createImageView(baseWeapon, 128),
                weaponName,
                damage,
                speed,
                level,
                divider(),
                sectionLabel("СЛОТЫ РУН"),
                runeSlots
        );
    }

    private void renderInventory() {
        inventoryGrid.getChildren().clear();

        List<Item> visibleItems = inventory.getItems()
                .stream()
                .filter(this::matchesFilter)
                .toList();

        for (Item item : visibleItems) {
            inventoryGrid.getChildren().add(createItemButton(item));
        }

        int emptyCount = Math.max(0, inventory.getCapacity() - visibleItems.size());
        for (int index = 0; index < emptyCount; index++) {
            inventoryGrid.getChildren().add(createEmptyInventorySlot());
        }
    }

    private MenuButton createItemButton(Item item) {
        MenuButton button = new MenuButton(
                displayName(item),
                createImageView(item, 64),
                138,
                122
        );
        button.setSelected(selectedItem == item);
        button.setOnAction(() -> {
            selectedItem = item;
            resetStatus();
            renderInventory();
            renderDetails();
        });
        return button;
    }

    private VBox createEmptyInventorySlot() {
        Label symbol = new Label("✦");
        symbol.setStyle("-fx-text-fill:#3f3348;-fx-font-size:22px;");
        Label text = new Label("ПУСТО");
        text.setStyle(
                "-fx-text-fill:#665a70;"
                        + "-fx-font-family:'Monospaced';"
                        + "-fx-font-size:12px;"
        );
        VBox slot = new VBox(5, symbol, text);
        slot.setAlignment(Pos.CENTER);
        slot.setPrefSize(138, 122);
        slot.setMaxSize(138, 122);
        slot.setStyle(EMPTY_SLOT_STYLE);
        return slot;
    }

    private void renderRuneSlots() {
        runeSlots.getChildren().clear();
        List<Rune> installedRunes = loadout.getInstalledRunes();
        int slotCount = loadout.getWeapon().getRuneSlotCount();

        for (int index = 0; index < slotCount; index++) {
            if (index < installedRunes.size()) {
                runeSlots.getChildren().add(createInstalledRuneButton(installedRunes.get(index)));
            } else {
                runeSlots.getChildren().add(createEmptyRuneSlot());
            }
        }
    }

    private MenuButton createInstalledRuneButton(Rune rune) {
        MenuButton button = new MenuButton(
                displayName(rune),
                createImageView(rune, 64),
                120,
                104
        );
        button.setSelected(selectedItem == rune);
        button.setOnAction(() -> {
            selectedItem = rune;
            resetStatus();
            renderRuneSlots();
            renderDetails();
        });
        return button;
    }

    private VBox createEmptyRuneSlot() {
        Label symbol = new Label("+");
        symbol.setStyle("-fx-text-fill:#776b86;-fx-font-size:30px;");
        Label text = new Label("ПУСТОЙ СЛОТ");
        text.setStyle(
                "-fx-text-fill:#776b86;"
                        + "-fx-font-family:'Monospaced';"
                        + "-fx-font-size:11px;"
        );
        VBox slot = new VBox(4, symbol, text);
        slot.setAlignment(Pos.CENTER);
        slot.setPrefSize(120, 104);
        slot.setMaxSize(120, 104);
        slot.setStyle(EMPTY_SLOT_STYLE);
        return slot;
    }

    private void renderDetails() {
        detailsPanel.getChildren().clear();

        if (selectedItem == null) {
            Label hint = infoLabel("Выберите предмет в инвентаре или установленную руну.");
            hint.setWrapText(true);
            hint.setAlignment(Pos.CENTER);
            detailsPanel.getChildren().addAll(sectionLabel("СВЕДЕНИЯ О ПРЕДМЕТЕ"), hint, statusLabel);
            return;
        }

        Label title = new Label(displayName(selectedItem));
        title.setWrapText(true);
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(270);
        title.setStyle(
                "-fx-text-fill:#f6cf7a;"
                        + "-fx-font-family:'Monospaced';"
                        + "-fx-font-size:21px;"
                        + "-fx-font-weight:bold;"
        );

        Label description = infoLabel(displayDescription(selectedItem));
        description.setWrapText(true);
        description.setAlignment(Pos.CENTER);
        description.setMaxWidth(270);

        detailsPanel.getChildren().addAll(
                title,
                divider(),
                createImageView(selectedItem, 128),
                description
        );

        if (selectedItem instanceof Rune rune) {
            boolean installed = isInstalled(rune);
            Label state = new Label(installed ? "УСТАНОВЛЕНА В МЕЧ" : "НАХОДИТСЯ В ИНВЕНТАРЕ");
            state.setWrapText(true);
            state.setAlignment(Pos.CENTER);
            state.setMaxWidth(270);
            state.setStyle(
                    "-fx-text-fill:"
                            + (installed ? "#9ee493" : "#f3d69b")
                            + ";-fx-font-family:'Monospaced';"
                            + "-fx-font-size:13px;"
                            + "-fx-font-weight:bold;"
            );

            MenuButton action = new MenuButton(
                    installed ? "СНЯТЬ РУНУ" : "УСТАНОВИТЬ В МЕЧ",
                    250,
                    58
            );
            action.setOnAction(() -> changeRuneState(rune));
            detailsPanel.getChildren().addAll(state, action);
        } else if (selectedItem instanceof Weapon weapon) {
            detailsPanel.getChildren().addAll(
                    infoLabel("Урон: +" + weapon.getDamageBonus()),
                    infoLabel(String.format("Скорость: %.2f", weapon.getAttackSpeed())),
                    infoLabel("Слоты рун: " + weapon.getRuneSlotCount())
            );
        } else if (selectedItem instanceof Potion) {
            Label useHint = infoLabel("Зелье используется во время боя.");
            useHint.setWrapText(true);
            useHint.setAlignment(Pos.CENTER);
            detailsPanel.getChildren().add(useHint);
        }

        detailsPanel.getChildren().add(statusLabel);
    }

    private void changeRuneState(Rune rune) {
        RuneActionResult result;
        if (isInstalled(rune)) {
            result = runeService.removeRune(rune.getRuneType(), loadout, inventory);
        } else {
            result = runeService.installRune(rune, loadout, inventory);
        }
        showResult(result);
        refresh();
    }

    private boolean isInstalled(Rune rune) {
        return loadout.getInstalledRunes()
                .stream()
                .anyMatch(installed -> installed.getRuneType() == rune.getRuneType());
    }

    private boolean matchesFilter(Item item) {
        return switch (filter) {
            case ALL -> true;
            case WEAPONS -> item instanceof Weapon;
            case RUNES -> item instanceof Rune;
            case CONSUMABLES -> item instanceof Potion;
        };
    }

    private void refreshFilterButtons() {
        Filter[] filters = Filter.values();
        for (int index = 0; index < filterButtons.length; index++) {
            filterButtons[index].setSelected(filter == filters[index]);
        }
    }

    private Node createImageView(Item item, double size) {
        try {
            String path = ItemSpriteResolver.resolve(item);
            URL resource = getClass().getResource(path);
            if (resource != null) {
                Image image = new Image(resource.toExternalForm());
                if (!image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(size);
                    imageView.setFitHeight(size);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(false);
                    return imageView;
                }
            }
        } catch (RuntimeException ignored) {
            // Optional item art may be absent while the UI remains usable.
        }

        Label icon = new Label("✦");
        icon.setStyle(
                "-fx-text-fill:#c6a96d;-fx-font-size:"
                        + Math.max(20, size * .55)
                        + "px;"
        );
        StackPane fallback = new StackPane(icon);
        fallback.setPrefSize(size, size);
        fallback.setStyle("-fx-background-color:#191521;-fx-border-color:#655478;");
        return fallback;
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle(
                "-fx-text-fill:#cfa85e;"
                        + "-fx-font-family:'Monospaced';"
                        + "-fx-font-size:14px;"
                        + "-fx-font-weight:bold;"
        );
        return label;
    }

    private Label infoLabel(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-text-fill:#ddd0ba;"
                        + "-fx-font-family:'Monospaced';"
                        + "-fx-font-size:14px;"
        );
        return label;
    }

    private Region divider() {
        Region divider = new Region();
        divider.setPrefHeight(2);
        divider.setMaxWidth(Double.MAX_VALUE);
        divider.setStyle("-fx-background-color:#65472f;");
        VBox.setMargin(divider, new Insets(6, 0, 6, 0));
        return divider;
    }

    private String displayName(Item item) {
        if (item instanceof Weapon weapon) {
            return switch (weapon.getUpgradeLevel()) {
                case 1 -> "СТАРТОВЫЙ МЕЧ";
                case 2 -> "БЫСТРЫЙ КЛИНОК";
                case 3 -> "ДВУРУЧНЫЙ МЕЧ";
                case 4 -> "МЕЧ ИЗ ТЁМНОЙ СТАЛИ";
                case 5 -> "ДРЕВНИЙ КЛИНОК";
                default -> weapon.getName().toUpperCase();
            };
        }

        if (item instanceof Rune rune) {
            return switch (rune.getRuneType()) {
                case FIRE -> "РУНА ОГНЯ";
                case ICE -> "РУНА ЛЬДА";
                case PURITY -> "РУНА ЧИСТОТЫ";
                case HOLY -> "СВЯТАЯ РУНА";
                case ANCIENT -> "ДРЕВНЯЯ РУНА";
                case POISON -> "РУНА ЯДА";
                case VAMPIRE -> "РУНА ВАМПИРИЗМА";
                case LIGHTNING -> "РУНА МОЛНИИ";
                case FURY -> "РУНА ЯРОСТИ";
                case HASTE -> "РУНА СКОРОСТИ";
                case CRITICAL -> "РУНА КРИТИЧЕСКОГО УДАРА";
            };
        }

        if (item instanceof Potion) return "ЗЕЛЬЕ ЗДОРОВЬЯ";
        return item.getName().toUpperCase();
    }

    private String displayDescription(Item item) {
        if (item instanceof Rune rune) {
            return switch (rune.getRuneType()) {
                case FIRE -> "Поджигает врагов и усиливает урон по ледяным существам.";
                case ICE -> "Замедляет врагов и усиливает урон по огненным существам.";
                case PURITY -> "Усиливает урон по отравленным и искажённым врагам.";
                case HOLY -> "Усиливает урон по нежити и теневым существам.";
                case ANCIENT -> "Усиливает урон по боссам и разрушает их защиту.";
                case POISON -> "Накладывает периодический урон ядом.";
                case VAMPIRE -> "Возвращает часть нанесённого урона в виде здоровья.";
                case LIGHTNING -> "Передаёт часть урона ближайшему противнику.";
                case FURY -> "Увеличивает урон оружия.";
                case HASTE -> "Увеличивает скорость атаки.";
                case CRITICAL -> "Добавляет шанс нанести критический урон.";
            };
        }

        if (item instanceof Weapon weapon) {
            return "Оружие " + weapon.getUpgradeLevel() + " уровня с "
                    + weapon.getRuneSlotCount() + " слотами рун.";
        }

        if (item instanceof Potion) return "Восстанавливает здоровье героя во время боя.";
        return item.getDescription();
    }

    private void resetStatus() {
        statusLabel.setText("Предмет выбран");
        statusLabel.setStyle(
                "-fx-text-fill:#d8c8a8;"
                        + "-fx-font-family:'Monospaced';"
                        + "-fx-font-size:13px;"
        );
    }

    private void showResult(RuneActionResult result) {
        statusLabel.setText(result.message());
        statusLabel.setWrapText(true);
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setMaxWidth(270);
        statusLabel.setStyle(
                "-fx-text-fill:"
                        + (result.success() ? "#79d28b" : "#e27676")
                        + ";-fx-font-family:'Monospaced';"
                        + "-fx-font-size:13px;"
        );
    }
}
