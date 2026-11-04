package com.gamelab.ui.game;

import com.gamelab.domain.PlayerCharacter;
import com.gamelab.ui.components.HealthHud;
import com.gamelab.ui.menu.MenuScreen;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/** Initial gameplay view with the player's live health HUD. */
public final class GameScreen extends Pane {
    private final PlayerCharacter player;
    private final HealthHud healthHud = new HealthHud();

    public GameScreen(PlayerCharacter player, Runnable onReturnToMenu) {
        this.player = player;
        setPrefSize(MenuScreen.W, MenuScreen.H);
        setMinSize(MenuScreen.W, MenuScreen.H);
        setMaxSize(MenuScreen.W, MenuScreen.H);
        setStyle("-fx-background-image: url('/assets/ui/backgrounds/menu_bg.png');"
                + "-fx-background-size: cover; -fx-background-color: #100d18;");

        Label title = new Label("SHADOWFALL");
        title.setTextFill(Color.web("#f2d08a"));
        title.setFont(Font.font("Serif", 36));
        title.setLayoutX(800);
        title.setLayoutY(80);

        Label hero = new Label(player.getName() + "  ·  " + player.getCharacterClass());
        hero.setTextFill(Color.web("#dfd4c1"));
        hero.setFont(Font.font("Serif", 22));
        hero.setLayoutX(40);
        hero.setLayoutY(120);

        healthHud.setLayoutX(32);
        healthHud.setLayoutY(32);
        refreshHealth();

        Label note = new Label("Путешествие начинается");
        note.setTextFill(Color.web("#c5a5ef"));
        note.setFont(Font.font("Serif", 28));
        note.setLayoutX(760);
        note.setLayoutY(510);

        Label back = new Label("ВЕРНУТЬСЯ В МЕНЮ");
        back.setTextFill(Color.web("#f2d08a"));
        back.setFont(Font.font("Serif", 18));
        back.setStyle("-fx-padding: 12 20; -fx-border-color: #9b7040; -fx-background-color: #100e1b;");
        back.setLayoutX(830);
        back.setLayoutY(950);
        back.setOnMouseClicked(event -> onReturnToMenu.run());
        back.setOnMouseEntered(event -> back.setOpacity(.75));
        back.setOnMouseExited(event -> back.setOpacity(1));

        getChildren().addAll(title, hero, healthHud, note, back);
    }

    /** Call after combat, healing, or a level-up changes the player's health. */
    public void refreshHealth() {
        healthHud.setHealth(player.getHealth(), player.getMaxHealth());
    }

    public HealthHud getHealthHud() {
        return healthHud;
    }
}
