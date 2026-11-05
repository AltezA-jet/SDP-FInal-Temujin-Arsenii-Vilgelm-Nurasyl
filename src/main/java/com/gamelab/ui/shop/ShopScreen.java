package com.gamelab.ui.shop;

import com.gamelab.domain.Item;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.shop.PurchaseResult;
import com.gamelab.domain.shop.Shop;
import com.gamelab.domain.shop.ShadowfallShopCatalog;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.ui.inventory.InventoryView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Objects;

/** Full-screen shop with the current inventory shown beside the product list. */
public final class ShopScreen extends Pane {
    public static final double W = 1920;
    public static final double H = 1080;

    private final Inventory inventory;
    private final CoinWallet wallet;
    private final String characterClass;
    private final Runnable onBack;
    private final Shop shop;
    private final InventoryView inventoryView;
    private final Label balanceLabel = new Label();
    private final Label statusLabel = new Label("Choose an item to purchase.");

    public ShopScreen(Inventory inventory, CoinWallet wallet, String characterClass, Runnable onBack) {
        this.inventory = Objects.requireNonNull(inventory, "Inventory cannot be null");
        this.wallet = Objects.requireNonNull(wallet, "Wallet cannot be null");
        this.characterClass = Objects.requireNonNull(characterClass, "Character class cannot be null");
        this.onBack = Objects.requireNonNull(onBack, "Back action cannot be null");
        this.shop = ShadowfallShopCatalog.createDefaultShop(new GameEventBus());
        this.inventoryView = new InventoryView(inventory);

        setPrefSize(W, H);
        setMinSize(W, H);
        setMaxSize(W, H);
        setStyle("-fx-background-color: #100d18;");

        Label title = new Label("TRADER'S HALL");
        title.setTextFill(Color.web("#f2d08a"));
        title.setFont(Font.font("Serif", 42));
        title.relocate(70, 42);

        balanceLabel.setTextFill(Color.web("#ffd66e"));
        balanceLabel.setFont(Font.font("Monospaced", 24));
        balanceLabel.relocate(1510, 52);

        Label goodsTitle = sectionTitle("GOODS", 70, 135);
        Label inventoryTitle = sectionTitle("YOUR INVENTORY", 1190, 135);

        TilePane goods = new TilePane();
        goods.setPadding(new Insets(12));
        goods.setHgap(18);
        goods.setVgap(18);
        goods.setPrefColumns(3);
        goods.setTileAlignment(Pos.TOP_LEFT);
        for (Item item : shop.getCatalog()) {
            goods.getChildren().add(createItemCard(item));
        }

        ScrollPane goodsScroll = new ScrollPane(goods);
        goodsScroll.setLayoutX(55);
        goodsScroll.setLayoutY(180);
        goodsScroll.setPrefSize(1060, 700);
        goodsScroll.setFitToWidth(true);
        goodsScroll.setStyle("-fx-background: #100d18; -fx-background-color: #100d18;");

        inventoryView.setLayoutX(1170);
        inventoryView.setLayoutY(180);
        inventoryView.setPrefSize(690, 700);

        statusLabel.setLayoutX(70);
        statusLabel.setLayoutY(915);
        statusLabel.setPrefWidth(1100);
        statusLabel.setStyle("-fx-text-fill: #d8c8a8; -fx-font-size: 18px;");

        Button back = new Button("BACK TO GAME");
        back.setLayoutX(1570);
        back.setLayoutY(920);
        back.setPrefSize(250, 62);
        back.setStyle("-fx-background-color: #392b3b; -fx-text-fill: #f2d08a; -fx-font-size: 16px; -fx-border-color: #a47b50;");
        back.setOnAction(event -> onBack.run());

        setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) onBack.run();
        });
        setFocusTraversable(true);
        getChildren().addAll(title, balanceLabel, goodsTitle, inventoryTitle, goodsScroll,
                inventoryView, statusLabel, back);
        refresh();
        requestFocus();
    }

    private VBox createItemCard(Item item) {
        Label name = new Label(item.getName());
        name.setWrapText(true);
        name.setStyle("-fx-text-fill: #f2d08a; -fx-font-size: 19px; -fx-font-weight: bold;");

        Label description = new Label(item.getDescription());
        description.setWrapText(true);
        description.setMinHeight(66);
        description.setStyle("-fx-text-fill: #d8c8a8; -fx-font-size: 14px;");

        Label price = new Label(item.getPrice() + " coins  ·  " + item.getRequiredClass());
        price.setStyle("-fx-text-fill: #ffd66e; -fx-font-size: 14px;");

        Button buy = new Button("BUY");
        buy.setMaxWidth(Double.MAX_VALUE);
        buy.setStyle("-fx-background-color: #6b452b; -fx-text-fill: #fff0c2; -fx-font-weight: bold;");
        buy.setOnAction(event -> purchase(item));

        VBox card = new VBox(12, name, description, price, buy);
        card.setPadding(new Insets(18));
        card.setPrefSize(318, 230);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: #201b2d; -fx-border-color: #8b6f47; -fx-border-width: 2; -fx-background-radius: 8; -fx-border-radius: 8;");
        return card;
    }

    private void purchase(Item item) {
        PurchaseResult result = shop.purchase(item, characterClass, wallet, inventory);
        statusLabel.setText(result.message());
        statusLabel.setStyle((result.success() ? "-fx-text-fill: #79d28b;" : "-fx-text-fill: #e27676;")
                + " -fx-font-size: 18px;");
        refresh();
    }

    private void refresh() {
        balanceLabel.setText("COINS  " + wallet.getBalance());
        inventoryView.refresh();
    }

    private Label sectionTitle(String text, double x, double y) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#c6a96d"));
        label.setFont(Font.font("Monospaced", 20));
        label.relocate(x, y);
        return label;
    }
}
