package com.gamelab.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public final class ItemSpritePreview extends Application {

    private static final String ROOT = "/assets/items/";

    private static final String[] ASSETS = {
            "weapons/starter_sword.png",
            "weapons/swift_blade.png",
            "weapons/greatsword.png",
            "weapons/darksteel_sword.png",
            "weapons/ancient_blade.png",

            "runes/fire_rune.png",
            "runes/ice_rune.png",
            "runes/purity_rune.png",
            "runes/holy_rune.png",
            "runes/ancient_rune.png",
            "runes/poison_rune.png",
            "runes/vampire_rune.png",
            "runes/lightning_rune.png",
            "runes/fury_rune.png",
            "runes/haste_rune.png",
            "runes/critical_rune.png",

            "consumables/health_potion.png",
            "currency/coin.png"
    };

    @Override
    public void start(Stage stage) {
        FlowPane gallery = new FlowPane(16, 16);
        gallery.setPadding(new Insets(24));
        gallery.setStyle("-fx-background-color: #181820;");

        for (String asset : ASSETS) {
            gallery.getChildren().add(createCard(asset));
        }

        ScrollPane scrollPane = new ScrollPane(gallery);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #181820;");

        Scene scene = new Scene(scrollPane, 900, 650);

        stage.setTitle("Shadowfall Item Sprite Preview");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createCard(String assetPath) {
        URL resource = Objects.requireNonNull(
                getClass().getResource(ROOT + assetPath),
                "Missing sprite: " + assetPath
        );

        Image image = new Image(resource.toExternalForm());

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(64);
        imageView.setFitHeight(64);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(false);

        String fileName = assetPath.substring(
                assetPath.lastIndexOf('/') + 1
        );

        Label label = new Label(
                fileName
                        .replace(".png", "")
                        .replace('_', ' ')
        );

        label.setWrapText(true);
        label.setStyle(
                "-fx-text-fill: white;"
                        + "-fx-font-size: 13px;"
        );

        VBox card = new VBox(10, imageView, label);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(140, 120);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: #292936;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #45455a;"
                        + "-fx-border-radius: 8;"
        );

        return card;
    }

    public static void main(String[] args) {
        launch(args);
    }
}