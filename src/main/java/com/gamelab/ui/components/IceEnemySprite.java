package com.gamelab.ui.components;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import java.net.URL;

/** Small four-frame ice elemental using the supplied walk and attack atlases. */
public final class IceEnemySprite extends Pane {
    private static final String ROOT = "/assets/2 Этап-Enemy(1,2,3)/";
    private final ImageView sprite = new ImageView();
    private String state = "idle";
    private int frame;
    private double frameTime;

    public IceEnemySprite(double size) {
        setPrefSize(size, size); setMinSize(size, size); setMaxSize(size, size);
        sprite.setPreserveRatio(true); sprite.setSmooth(false);
        sprite.fitWidthProperty().bind(widthProperty()); sprite.fitHeightProperty().bind(heightProperty());
        sprite.setMouseTransparent(true); getChildren().add(sprite); apply();
    }

    public void setWalking(boolean walking) {
        if (state.equals("attack")) return;
        String next = walking ? "walk" : "idle";
        if (!state.equals(next)) { state = next; frame = 0; frameTime = 0; apply(); }
    }

    public void setAttacking() { state = "attack"; frame = 0; frameTime = 0; apply(); }

    public void setFacingLeft(boolean facingLeft) { setScaleX(facingLeft ? -1 : 1); }

    public void update(double dt) {
        if (state.equals("idle")) return;
        frameTime += dt;
        while (frameTime >= .13) {
            frameTime -= .13; frame++;
            if (frame >= 4) { if (state.equals("attack")) state = "idle"; frame = 0; }
            apply();
        }
    }

    private void apply() {
        boolean sheet = !state.equals("idle");
        String file = state.equals("attack") ? "ice_elemental_attack.png" : state.equals("walk") ? "ice_elemental_walk.png" : "ice_elemental.png";
        URL url = IceEnemySprite.class.getResource(ROOT + file);
        if (url == null) throw new IllegalStateException("Missing enemy sprite " + file);
        sprite.setImage(new Image(url.toExternalForm()));
        sprite.setViewport(sheet ? new Rectangle2D(frame * 64, 0, 64, 96) : null);
    }
}
