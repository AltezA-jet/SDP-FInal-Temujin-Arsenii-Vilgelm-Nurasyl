package com.gamelab;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(960, 540);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Background
        gc.setFill(Color.rgb(15, 18, 32));
        gc.fillRect(0, 0, 960, 540);

        // Ground
        gc.setFill(Color.rgb(53, 65, 58));
        gc.fillRect(0, 450, 960, 90);

        // Pixel-style placeholder character
        gc.setFill(Color.rgb(190, 75, 75));
        gc.fillRect(120, 390, 32, 48);

        gc.setFill(Color.rgb(235, 190, 135));
        gc.fillRect(124, 366, 24, 24);

        gc.setFill(Color.rgb(60, 65, 85));
        gc.fillRect(120, 438, 12, 18);
        gc.fillRect(140, 438, 12, 18);

        // Title
        gc.setFill(Color.GOLD);
        gc.fillText("GAME LAB: SHADOWFALL", 30, 40);

        gc.setFill(Color.WHITE);
        gc.fillText("Prototype 0.1 - JavaFX is running", 30, 65);

        StackPane root = new StackPane(canvas);
        root.setStyle("-fx-background-color: #0f1220;");

        Scene scene = new Scene(root, 960, 540);

        stage.setTitle("Game Lab: Shadowfall");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
