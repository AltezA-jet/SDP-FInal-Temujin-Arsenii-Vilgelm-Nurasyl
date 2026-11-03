package com.gamelab.app;

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


/**
 * Game Lab: Shadowfall - application entry point.
 * The menu is designed for 1920x1080 and scaled uniformly to any window size (black bars if the
 * aspect ratio differs). F11 toggles fullscreen.
 */
public class Main extends Application {

    private static final String TITLE = "Game Lab: Shadowfall";
    private Stage stage;
    private MenuScreen menu;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        menu = new MenuScreen();
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
            } else {
                if (menu != null && menu.getScene() == stage.getScene()) menu.handleKey(e.getCode());
            }
        });

        stage.setTitle(TITLE);
        stage.setScene(scene);
        showFixedScreen(menu);
        stage.setMinWidth(960);
        stage.setMinHeight(540);
        stage.setFullScreenExitHint("F11 - toggle fullscreen");
        stage.setOnHidden(e -> menu.stop());
        stage.show();

        
    }
    
    private void onNewGame() {
        menu.stop();
        showFixedScreen(new CharacterCreationScreen(this::onCharacterCreated, this::returnToMenu));
    }

    private void onCharacterCreated(PlayerCharacter player) {
        showFixedScreen(new GameMenuScreen(player, this::returnToMenu));
    }

    private void returnToMenu() {
        menu = new MenuScreen();
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
}
