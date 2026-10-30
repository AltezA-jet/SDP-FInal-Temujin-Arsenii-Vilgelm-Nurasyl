package com.gamelab;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;


/**
 * Game Lab: Shadowfall - application entry point.
 * The menu is designed for 1920x1080 and scaled uniformly to any window size (black bars if the
 * aspect ratio differs). F11 toggles fullscreen.
 */
public class Main extends Application {

    private static final String TITLE = "Game Lab: Shadowfall";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        MenuScreen menu = new MenuScreen();
        menu.setOnNewGame(this::onNewGame);
        menu.setOnContinue(this::onContinue);
        menu.setOnSettings(this::onSettings);
        menu.setOnExit(() -> {
            menu.stop();
            Platform.exit();
        });
        menu.setContinueEnabled(true); // TODO: set to "a save exists in the database"

        StackPane root = new StackPane(new Group(menu));
        root.setStyle("-fx-background-color: black;");
        menu.scaleXProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(0.01, Math.min(root.getWidth() / MenuScreen.W, root.getHeight() / MenuScreen.H)),
                root.widthProperty(), root.heightProperty()));
        menu.scaleYProperty().bind(menu.scaleXProperty());

        // 1:1 (1920x1080) when the screen is big enough, otherwise the largest 16:9 window that fits
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double s = Math.min(1.0, Math.min(screen.getWidth() / MenuScreen.W, (screen.getHeight() - 40) / MenuScreen.H));
        Scene scene = new Scene(root, MenuScreen.W * s, MenuScreen.H * s, Color.BLACK);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
                e.consume();
            } else {
                menu.handleKey(e.getCode());
            }
        });

        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.setMinWidth(960);
        stage.setMinHeight(540);
        stage.setFullScreenExitHint("F11 - toggle fullscreen");
        stage.setOnHidden(e -> menu.stop());
        stage.show();

        
    }
    
    private void onNewGame() {
        // TODO: character creation screen / start a new session
        System.out.println("[Shadowfall] New Game");
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
