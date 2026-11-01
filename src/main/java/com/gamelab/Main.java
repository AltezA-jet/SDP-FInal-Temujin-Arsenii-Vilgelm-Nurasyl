package com.gamelab;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.ui.CharacterCreationScreen;


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
        Scene scene = new Scene(new StackPane(), MenuScreen.W * s, MenuScreen.H * s, Color.BLACK);
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
        System.out.println("[Shadowfall] Character created: " + player.getName() + " (" + player.getCharacterClass() + ")");
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
        StackPane root = new StackPane(new Group(screen));
        root.setStyle("-fx-background-color:black;");
        screen.scaleXProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(0.01, Math.min(root.getWidth() / MenuScreen.W, root.getHeight() / MenuScreen.H)),
                root.widthProperty(), root.heightProperty()));
        screen.scaleYProperty().bind(screen.scaleXProperty());
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
