package com.gamelab.ui;

import com.gamelab.application.GameFacade;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenuController {

    private final Stage stage;
    private final GameFacade facade;

    public MainMenuController(Stage stage, GameFacade facade) {
        this.stage = stage;
        this.facade = facade;
    }

    public Parent createView() {
        VBox menu = new VBox(15);

        // Здесь создаются кнопки меню.
        // NEW GAME -> открыть GameController
        // CONTINUE -> загрузить SaveData через facade
        // SETTINGS -> показать управление
        // QUIT -> закрыть окно

        return menu;
    }
}