package com.gamelab.ui.shop;

import com.gamelab.domain.Item;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.shop.PurchaseResult;
import com.gamelab.domain.shop.Shop;
import com.gamelab.domain.shop.ShadowfallShopCatalog;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.ui.inventory.InventoryView;
import com.gamelab.ui.inventory.ItemSpriteResolver;
import com.gamelab.ui.ShadowfallUi;
import com.gamelab.ui.components.MenuButton;
import com.gamelab.domain.item.Potion;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.rune.Rune;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Objects;
import java.net.URL;

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
    private final TilePane goods = new TilePane();
    private final MenuButton[] filterButtons = new MenuButton[4];
    private ShopFilter filter = ShopFilter.ALL;

    private enum ShopFilter { ALL, WEAPONS, RUNES, CONSUMABLES }

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
        ShadowfallUi.install(this);
        setStyle("-fx-background-color: #100d18;");

        Label title = new Label("TRADER'S HALL");
        title.setTextFill(Color.web("#f2d08a"));
        title.setFont(Font.font("Serif", 42));
        title.relocate(70, 42);

        balanceLabel.setTextFill(Color.web("#ffd66e"));
        balanceLabel.setFont(Font.font("Monospaced", 24));
        balanceLabel.setGraphic(coinIcon());
        balanceLabel.relocate(1510, 52);

        Label goodsTitle = sectionTitle("GOODS", 70, 170);
        Label inventoryTitle = sectionTitle("YOUR INVENTORY", 1190, 135);

        goods.setPadding(new Insets(12));
        goods.setHgap(18);
        goods.setVgap(18);
        goods.setPrefColumns(3);
        goods.setTileAlignment(Pos.TOP_LEFT);
        HBox filters = new HBox(10);
        filters.setLayoutX(70);
        filters.setLayoutY(118);
        String[] filterNames = {"ALL", "WEAPONS", "RUNES", "CONSUMABLES"};
        ShopFilter[] filterValues = ShopFilter.values();
        for (int i=0;i<filterButtons.length;i++) {
            final int selected=i;
            MenuButton button=new MenuButton(filterNames[i],150,48);
            button.setOnAction(() -> { filter=filterValues[selected]; refreshFilters(); renderGoods(); });
            filterButtons[i]=button;
            filters.getChildren().add(button);
        }
        renderGoods();

        ScrollPane goodsScroll = new ScrollPane(goods);
        goodsScroll.setLayoutX(55);
        goodsScroll.setLayoutY(202);
        goodsScroll.setPrefSize(1060, 678);
        goodsScroll.setFitToWidth(true);
        goodsScroll.setStyle("-fx-background: #100d18; -fx-background-color: #100d18;");

        inventoryView.setLayoutX(1170);
        inventoryView.setLayoutY(180);
        inventoryView.setPrefSize(690, 700);

        statusLabel.setLayoutX(70);
        statusLabel.setLayoutY(915);
        statusLabel.setPrefWidth(1100);
        statusLabel.setStyle("-fx-text-fill: #d8c8a8; -fx-font-size: 18px;");

        MenuButton back = new MenuButton("BACK TO GAME",250,62);
        back.setLayoutX(1570); back.setLayoutY(920); back.setOnAction(onBack);

        setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) onBack.run();
        });
        setFocusTraversable(true);
        getChildren().addAll(title, balanceLabel, goodsTitle, inventoryTitle, filters, goodsScroll,
                inventoryView, statusLabel, back);
        refreshFilters();
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

        MenuButton buy = new MenuButton("BUY",260,46);
        buy.setOnAction(() -> purchase(item));

        VBox card = new VBox(9, itemIcon(item), name, description, price, buy);
        card.setPadding(new Insets(18));
        card.setPrefSize(318, 300);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: #201b2d; -fx-border-color: #8b6f47; -fx-border-width: 2; -fx-background-radius: 8; -fx-border-radius: 8;");
        return card;
    }

    private Node itemIcon(Item item) {
        try {
            URL url = getClass().getResource(ItemSpriteResolver.resolve(item));
            if (url != null) {
                Image image = new Image(url.toExternalForm(), 64, 64, true, false, true);
                if (!image.isError()) {
                    ImageView view = new ImageView(image);
                    view.setFitWidth(64);
                    view.setFitHeight(64);
                    view.setPreserveRatio(true);
                    view.setSmooth(false);
                    return view;
                }
            }
        } catch (RuntimeException ignored) { }
        Label placeholder = new Label("✦");
        placeholder.setTextFill(Color.web("#c6a96d"));
        placeholder.setStyle("-fx-font-size: 34px; -fx-background-color: #191521; -fx-border-color: #655478;");
        placeholder.setPrefSize(64, 64);
        placeholder.setAlignment(Pos.CENTER);
        return placeholder;
    }

    private Node coinIcon() {
        try {
            URL url = getClass().getResource(ItemSpriteResolver.coin());
            if (url != null) {
                Image image = new Image(url.toExternalForm(), 30, 30, true, false, true);
                if (!image.isError()) {
                    ImageView view = new ImageView(image);
                    view.setFitWidth(30);
                    view.setFitHeight(30);
                    view.setPreserveRatio(true);
                    view.setSmooth(false);
                    return view;
                }
            }
        } catch (RuntimeException ignored) { }
        Label fallback = new Label("◆");
        fallback.setTextFill(Color.web("#ffd66e"));
        return fallback;
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

    private void renderGoods() {
        goods.getChildren().clear();
        for (Item item : shop.getCatalog()) {
            boolean visible=switch(filter) {
                case ALL -> true;
                case WEAPONS -> item instanceof Weapon;
                case RUNES -> item instanceof Rune;
                case CONSUMABLES -> item instanceof Potion;
            };
            if(visible) goods.getChildren().add(createItemCard(item));
        }
    }

    private void refreshFilters() {
        ShopFilter[] values=ShopFilter.values();
        for(int i=0;i<filterButtons.length;i++) filterButtons[i].setSelected(filter==values[i]);
    }

    private Label sectionTitle(String text, double x, double y) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#c6a96d"));
        label.setFont(Font.font("Monospaced", 20));
        label.relocate(x, y);
        return label;
    }
}
