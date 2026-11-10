package com.gamelab.ui;

import javafx.scene.Parent;

/** Shared visual language for Shadowfall's JavaFX panels and controls. */
public final class ShadowfallUi {
    private static final String STYLESHEET = "/assets/shadowfall-ui.css";

    private ShadowfallUi() { }

    public static void install(Parent root) {
        String stylesheet = ShadowfallUi.class.getResource(STYLESHEET).toExternalForm();
        if (!root.getStylesheets().contains(stylesheet)) root.getStylesheets().add(stylesheet);
        root.getStyleClass().add("sf-root");
    }
}
