package com.gamelab.ui.shop;

import com.gamelab.domain.Item;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.Potion;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.rune.Rune;
import com.gamelab.domain.shop.PurchaseResult;
import com.gamelab.domain.shop.ShadowfallShopCatalog;
import com.gamelab.domain.shop.Shop;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.ui.components.MenuButton;
import com.gamelab.ui.inventory.ItemSpriteResolver;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.Objects;

/** Stage-one intermission: the shop and the player's inventory share one market scene. */
public final class StageOneMarketScreen extends Pane {
    public static final double W = 1920;
    public static final double H = 1080;

    private static final String PANEL_STYLE =
            "-fx-background-color: rgba(11,8,18,.93);"
                    + "-fx-border-color: #d79b4f;"
                    + "-fx-border-width: 3;"
                    + "-fx-background-radius: 12;"
                    + "-fx-border-radius: 12;";
    private static final String ITEM_STYLE =
            "-fx-background-color: rgba(38,27,43,.96);"
                    + "-fx-border-color: #80613f;"
                    + "-fx-border-width: 2;"
                    + "-fx-background-radius: 7;"
                    + "-fx-border-radius: 7;";

    private enum ShopFilter {
        ALL,
        WEAPONS,
        RUNES,
        CONSUMABLES
    }

    private final PlayerCharacter player;
    private final Inventory inventory;
    private final CoinWallet wallet;
    private final Shop shop;
    private final int completedStage;
    private final Runnable onBack;
    private final Runnable onContinue;
    private final GridPane shopItems = new GridPane();
    private final GridPane inventoryGrid = new GridPane();
    private final Label coinBalance = text("", 25, "#ffe29b");
    private final Label inventoryCount = text("", 18, "#d7c1a1");
    private final Label status = text("ВЫБЕРИТЕ ТОВАР У КУПЦА", 17, "#f3d69b");
    private final MenuButton[] shopFilterButtons = new MenuButton[4];
    private ShopFilter shopFilter = ShopFilter.ALL;

    public StageOneMarketScreen(
            PlayerCharacter player,
            Inventory inventory,
            CoinWallet wallet,
            Runnable onBack,
            Runnable onContinue
    ) {
        this(player, inventory, wallet, 0, onBack, onContinue);
    }

    public StageOneMarketScreen(
            PlayerCharacter player,
            Inventory inventory,
            CoinWallet wallet,
            int completedStage,
            Runnable onBack,
            Runnable onContinue
    ) {
        this.player = Objects.requireNonNull(player);
        this.inventory = Objects.requireNonNull(inventory);
        this.wallet = Objects.requireNonNull(wallet);
        if (completedStage < 0 || completedStage > 1) {
            throw new IllegalArgumentException("Completed stage must be 0 or 1");
        }
        this.completedStage = completedStage;
        this.onBack = Objects.requireNonNull(onBack);
        this.onContinue = Objects.requireNonNull(onContinue);
        this.shop = ShadowfallShopCatalog.createDefaultShop(new GameEventBus());

        URL stylesheet = StageOneMarketScreen.class.getResource("/assets/ui/styles/market.css");
        if (stylesheet != null) getStylesheets().add(stylesheet.toExternalForm());

        setPrefSize(W, H);
        setMinSize(W, H);
        setMaxSize(W, H);
        drawBackground();
        buildHeader();
        buildShop();
        buildInventory();
        buildFooter();
        refresh();

        setFocusTraversable(true);
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) onBack.run();
        });
        requestFocus();
    }

    private void drawBackground() {
        URL resource = StageOneMarketScreen.class.getResource(
                "/assets/ui/backgrounds/stage1_evening_market.png"
        );
        if (resource == null) throw new IllegalStateException("Missing stage-one market background");
        ImageView background = new ImageView(new Image(resource.toExternalForm()));
        background.setFitWidth(W);
        background.setFitHeight(H);
        background.setPreserveRatio(false);
        background.setSmooth(false);
        getChildren().add(background);

        Rectangle topShade = new Rectangle(W, 112, Color.rgb(7, 5, 14, .87));
        Rectangle bottomShade = new Rectangle(W, 92, Color.rgb(7, 5, 14, .88));
        bottomShade.setLayoutY(H - 92);
        getChildren().addAll(topShade, bottomShade);
    }

    private void buildHeader() {
        String heading = completedStage == 0
                ? "ТРЕНИРОВКА ЗАВЕРШЕНА  //  ВЕЧЕРНИЙ РЫНОК"
                : "ЭТАП 1 ЗАВЕРШЁН  //  ВЕЧЕРНИЙ РЫНОК";
        Label title = text(heading, 31, "#f6cf7a");
        title.relocate(58, 30);
        title.setPrefSize(1050, 52);

        ImageView coin = image(ItemSpriteResolver.coin(), 42);
        coin.relocate(1640, 31);
        coinBalance.relocate(1692, 33);
        coinBalance.setPrefSize(180, 45);
        getChildren().addAll(title, coin, coinBalance);
    }

    private void buildShop() {
        VBox panel = panel(48, 142, 650, 800);
        Label title = text("МАГАЗИН КУПЦА", 28, "#f6cf7a");
        Label subtitle = text("Выберите категорию и товар", 14, "#cbb99f");

        HBox filters = new HBox(8);
        filters.setAlignment(Pos.CENTER);
        String[] names = {"ВСЕ", "ОРУЖИЕ", "РУНЫ", "РАСХОДНИКИ"};
        ShopFilter[] values = ShopFilter.values();

        for (int index = 0; index < values.length; index++) {
            int currentIndex = index;
            MenuButton filterButton = new MenuButton(names[index], 138, 44);
            filterButton.setOnAction(() -> {
                shopFilter = values[currentIndex];
                refreshShopFilters();
                renderShop();
            });
            shopFilterButtons[index] = filterButton;
            filters.getChildren().add(filterButton);
        }

        shopItems.setHgap(14);
        shopItems.setVgap(14);
        shopItems.setPadding(new Insets(12));
        shopItems.setAlignment(Pos.TOP_CENTER);
        shopItems.setMinWidth(570);
        shopItems.setPrefWidth(570);
        shopItems.setMaxWidth(570);

        ScrollPane scroll = new ScrollPane(shopItems);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(560);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.getStyleClass().add("market-scroll");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setMargin(scroll, new Insets(8, 0, 0, 0));
        panel.getChildren().addAll(title, subtitle, filters, scroll);
        refreshShopFilters();
    }

    private void buildInventory() {
        VBox panel = panel(1222, 142, 650, 800);
        Label title = text("ИНВЕНТАРЬ", 28, "#f6cf7a");
        inventoryCount.setAlignment(Pos.CENTER_RIGHT);
        inventoryCount.setPrefWidth(560);

        inventoryGrid.setHgap(12);
        inventoryGrid.setVgap(12);
        inventoryGrid.setAlignment(Pos.TOP_CENTER);
        inventoryGrid.setPadding(new Insets(18, 0, 0, 0));
        panel.getChildren().addAll(title, inventoryCount, inventoryGrid);
    }

    private void buildFooter() {
        status.relocate(500, 996);
        status.setPrefSize(880, 62);
        status.setAlignment(Pos.CENTER);
        status.setWrapText(true);

        MenuButton back = new MenuButton("НАЗАД", 240, 60);
        back.relocate(60, 997);
        back.setOnAction(onBack);

        MenuButton next = new MenuButton(
                completedStage == 0 ? "ПЕРЕЙТИ К ЭТАПУ 1" : "ПЕРЕЙТИ К ЭТАПУ 2",
                420,
                60
        );
        next.relocate(1430, 997);
        next.setOnAction(onContinue);
        getChildren().addAll(status, back, next);
    }

    private void refresh() {
        coinBalance.setText(wallet.getBalance() + " МОНЕТ");
        inventoryCount.setText(inventory.getSize() + " / " + inventory.getCapacity() + " ЯЧЕЕК");
        renderShop();
        renderInventory();
    }

    private void renderShop() {
        shopItems.getChildren().clear();
        int visibleIndex = 0;
        for (Item item : shop.getCatalog()) {
            if (!matchesShopFilter(item)) continue;
            boolean alreadyOwned =
                    (item instanceof Rune rune
                            && inventory.hasAcquiredRuneType(rune.getRuneType()))
                            || (item instanceof Weapon weapon
                            && inventory.hasAcquiredWeaponLevel(weapon.getUpgradeLevel()));
            ImageView icon = image(ItemSpriteResolver.resolve(item), 54);
            Label name = text(item.getName(), 16, "#fff0c9");
            name.setWrapText(true);
            name.setAlignment(Pos.CENTER);
            name.setPrefWidth(245);
            Label description = text(item.getDescription(), 12, "#c9b9c8");
            description.setWrapText(true);
            description.setAlignment(Pos.CENTER);
            description.setPrefSize(245, 44);
            Label price = text(
                    alreadyOwned ? "УЖЕ КУПЛЕНО" : item.getPrice() + " монет",
                    15,
                    alreadyOwned ? "#9ee493" : "#f6c96b"
            );

            VBox card = new VBox(5, icon, name, description, price);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(10));
            card.setPrefSize(260, 176);
            card.setMinSize(260, 176);
            card.setMaxSize(260, 176);
            card.setStyle(ITEM_STYLE);
            card.setOpacity(alreadyOwned ? .72 : 1);
            card.setCursor(alreadyOwned ? javafx.scene.Cursor.DEFAULT : javafx.scene.Cursor.HAND);
            if (!alreadyOwned) {
                card.setOnMouseEntered(event -> card.setStyle(ITEM_STYLE + "-fx-border-color:#f0bb63;"));
                card.setOnMouseExited(event -> card.setStyle(ITEM_STYLE));
                card.setOnMouseClicked(event -> purchase(item));
            }
            shopItems.add(card, visibleIndex % 2, visibleIndex / 2);
            visibleIndex++;
        }
    }

    private boolean matchesShopFilter(Item item) {
        return switch (shopFilter) {
            case ALL -> true;
            case WEAPONS -> item instanceof Weapon;
            case RUNES -> item instanceof Rune;
            case CONSUMABLES -> item instanceof Potion;
        };
    }

    private void refreshShopFilters() {
        ShopFilter[] values = ShopFilter.values();
        for (int index = 0; index < shopFilterButtons.length; index++) {
            shopFilterButtons[index].setSelected(shopFilter == values[index]);
        }
    }

    private void renderInventory() {
        inventoryGrid.getChildren().clear();
        for (int index = 0; index < inventory.getCapacity(); index++) {
            VBox slot = new VBox(5);
            slot.setAlignment(Pos.CENTER);
            slot.setPrefSize(132, 112);
            slot.setPadding(new Insets(7));
            slot.setStyle(ITEM_STYLE);

            if (index < inventory.getItems().size()) {
                Item item = inventory.getItems().get(index);
                Label name = text(item.getName(), 12, "#f5e5c2");
                name.setWrapText(true);
                name.setAlignment(Pos.CENTER);
                name.setPrefWidth(116);
                slot.getChildren().addAll(image(ItemSpriteResolver.resolve(item), 48), name);
            } else {
                Label empty = text("ПУСТО", 12, "#71677b");
                slot.getChildren().add(empty);
            }
            inventoryGrid.add(slot, index % 4, index / 4);
        }
    }

    private void purchase(Item item) {
        PurchaseResult result = shop.purchase(
                item,
                player.getCharacterClass(),
                wallet,
                inventory
        );
        status.setText(result.success() ? "КУПЛЕНО: " + item.getName() : result.message());
        status.setTextFill(Color.web(result.success() ? "#9ee493" : "#f09a8f"));
        refresh();
    }

    private VBox panel(double x, double y, double width, double height) {
        VBox panel = new VBox(8);
        panel.relocate(x, y);
        panel.setPrefSize(width, height);
        panel.setMaxSize(width, height);
        panel.setPadding(new Insets(24));
        panel.setStyle(PANEL_STYLE);
        panel.setEffect(new DropShadow(28, Color.rgb(0, 0, 0, .72)));
        getChildren().add(panel);
        return panel;
    }

    private static Label text(String value, double size, String color) {
        Label label = new Label(value);
        label.setTextFill(Color.web(color));
        label.setStyle("-fx-font-family:'Monospaced';-fx-font-size:" + size + "px;-fx-font-weight:bold;");
        return label;
    }

    private static ImageView image(String resourcePath, double size) {
        URL resource = StageOneMarketScreen.class.getResource(resourcePath);
        if (resource == null) throw new IllegalStateException("Missing item asset " + resourcePath);
        ImageView image = new ImageView(new Image(resource.toExternalForm()));
        image.setFitWidth(size);
        image.setFitHeight(size);
        image.setPreserveRatio(true);
        image.setSmooth(false);
        return image;
    }
}
