package com.gamelab.app;

import com.gamelab.ui.game.GameScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.ui.character.CharacterCreationScreen;
import com.gamelab.ui.menu.MenuScreen;
import com.gamelab.ui.menu.GameMenuScreen;
import com.gamelab.ui.level.TutorialLevelScreen;
import com.gamelab.ui.inventory.InventoryScreen;
import com.gamelab.ui.shop.ShopScreen;
import com.gamelab.ui.level.EnemyArenaScreen;
import javafx.scene.control.Button;


/**
 * Game Lab: Shadowfall - application entry point.
 * The menu is designed for 1920x1080 and scaled uniformly to any window size (black bars if the
 * aspect ratio differs). F11 toggles fullscreen.
 */
public class Main extends Application {

    private static final String TITLE = "Game Lab: Shadowfall";
    private Stage stage;
    private MenuScreen menu;
    private GameScreen gameScreen;
    private EnemyArenaScreen enemyArena;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        menu = new MenuScreen();
        addArenaButton();
        menu.setOnNewGame(this::onNewGame);
        menu.setOnContinue(this::onContinue);
        menu.setOnSettings(this::onSettings);
        menu.setOnExit(() -> {
            menu.stop();
            Platform.exit();
        });
        menu.setContinueEnabled(true); // TODO: set to "a save exists in the database"

        // 1:1 (1920x1080) when the screen is big enough, otherwise the largest 16:9 window that fits
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double s = Math.min(1.0, Math.min(screen.getWidth() / MenuScreen.W, (screen.getHeight() - 40) / MenuScreen.H));
        Scene scene = new Scene(new Pane(), MenuScreen.W * s, MenuScreen.H * s, Color.BLACK);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
                e.consume();
            } else if (e.getCode() == KeyCode.F6 && menu.getScene() == stage.getScene()) {
                openEnemyArena();
                e.consume();
            } else {
                if (menu != null && menu.getScene() == stage.getScene()) menu.handleKey(e.getCode());
                if (gameScreen != null && gameScreen.getScene() == stage.getScene() && e.getCode() == KeyCode.I) gameScreen.toggleInventory();
            }
        });

        stage.setTitle(TITLE);
        stage.setScene(scene);
        showFixedScreen(menu);
        stage.setMinWidth(960);
        stage.setMinHeight(540);
        stage.setFullScreenExitHint("F11 - toggle fullscreen");
        stage.setOnHidden(e -> { menu.stop(); if (enemyArena != null) enemyArena.stop(); });
        stage.show();

        
    }
    
    private void onNewGame() {
        menu.stop();
        showFixedScreen(new CharacterCreationScreen(this::onCharacterCreated, this::returnToMenu));
    }

    private void onCharacterCreated(PlayerCharacter player) {
        final GameMenuScreen[] gameMenu = new GameMenuScreen[1];
        gameMenu[0] = new GameMenuScreen(player, this::returnToMenu,
                inventory -> showFixedScreen(new InventoryScreen(inventory, () -> showFixedScreen(gameMenu[0]))));
        gameMenu[0].setOnShop(inventory -> showFixedScreen(new ShopScreen(
                inventory,
                gameMenu[0].getWallet(),
                player.getCharacterClass(),
                () -> {
                    gameMenu[0].refreshWallet();
                    showFixedScreen(gameMenu[0]);
                })));
        showFixedScreen(new TutorialLevelScreen(player, () -> showFixedScreen(gameMenu[0])));
    }

    private void returnToMenu() {
        menu = new MenuScreen();
        addArenaButton();
        menu.setOnNewGame(this::onNewGame);
        menu.setOnContinue(this::onContinue);
        menu.setOnSettings(this::onSettings);
        menu.setOnExit(() -> { menu.stop(); Platform.exit(); });
        menu.setContinueEnabled(true);
        showFixedScreen(menu);
    }

    private void showFixedScreen(Pane screen) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color:black;");
        screen.setManaged(false);
        root.getChildren().add(screen);
        Runnable resize = () -> {
            double scale = Math.max(0.01, Math.min(root.getWidth() / MenuScreen.W, root.getHeight() / MenuScreen.H));
            screen.setScaleX(scale);
            screen.setScaleY(scale);
            screen.relocate((root.getWidth() - MenuScreen.W * scale) / 2,
                    (root.getHeight() - MenuScreen.H * scale) / 2);
        };
        root.widthProperty().addListener((obs, oldValue, newValue) -> resize.run());
        root.heightProperty().addListener((obs, oldValue, newValue) -> resize.run());
        resize.run();
        stage.getScene().setRoot(root);
    }

    private void onContinue() {
        // TODO: load the last save and open the game scene
        System.out.println("[Shadowfall] Continue");
    }

    private void onSettings() {
        // TODO: open the settings screen
        System.out.println("[Shadowfall] Settings");
    }

    private void addArenaButton() {
        Button button = new Button("АРЕНА ВРАГОВ / F6");
        button.setStyle("-fx-font-size: 22px; -fx-text-fill: #f2d08a; -fx-background-color: #211729; -fx-border-color: #9b7040; -fx-padding: 14 24;");
        button.relocate(1490, 970);
        button.setOnAction(event -> openEnemyArena());
        menu.getChildren().add(button);
    }

    private void openEnemyArena() {
        menu.stop();
        if (enemyArena != null) enemyArena.stop();
        enemyArena = new EnemyArenaScreen(this::returnToMenu);
        showFixedScreen(enemyArena);
    }
}
