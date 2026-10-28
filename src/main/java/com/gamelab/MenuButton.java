package com.gamelab;

import javafx.scene.Cursor;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * One menu button: shared frame sprite + its own label sprite + an additive glow.
 * All buttons use the same frame image, so they are identical by construction.
 *
 * Mouse events: entering the button asks the menu to focus it (glow fades in),
 * pressing shades it and nudges it down, releasing inside the button fires the action.
 */
final class MenuButton extends Pane {

    private static final Color PRESSED = Color.rgb(0, 0, 0, 0.35);
    private static final Color DISABLED = Color.rgb(8, 5, 12, 0.60);

    private final ImageView glow = new ImageView();
    private final Rectangle shade;

    private boolean enabled = true;
    private boolean focused;
    private boolean pressed;
    private double level; // current glow strength 0..1

    private Runnable onAction;
    private Runnable onHover;

    MenuButton(Image frameImg, Image labelImg, Image glowImg,
               double labelX, double labelY, double glowX, double glowY) {
        double w = frameImg.getWidth();
        double h = frameImg.getHeight();
        setPrefSize(w, h);
        setMinSize(w, h);
        setMaxSize(w, h);

        ImageView frame = new ImageView(frameImg);
        ImageView label = new ImageView(labelImg);
        label.setLayoutX(labelX);
        label.setLayoutY(labelY);

        glow.setImage(glowImg);
        glow.setLayoutX(glowX);
        glow.setLayoutY(glowY);
        glow.setBlendMode(BlendMode.ADD);
        glow.setOpacity(0);

        shade = new Rectangle(w, h, PRESSED);
        shade.setVisible(false);

        frame.setMouseTransparent(true);
        label.setMouseTransparent(true);
        glow.setMouseTransparent(true);
        shade.setMouseTransparent(true);
        getChildren().addAll(frame, label, glow, shade);

        setPickOnBounds(true);
        setCursor(Cursor.HAND);

        setOnMouseEntered(e -> {
            if (enabled && onHover != null) {
                onHover.run();
            }
        });
        setOnMousePressed(e -> {
            if (enabled) {
                pressed = true;
                shade.setFill(PRESSED);
                shade.setVisible(true);
                setTranslateY(2);
            }
        });
        setOnMouseReleased(e -> {
            boolean wasPressed = pressed;
            pressed = false;
            setTranslateY(0);
            shade.setFill(DISABLED);
            shade.setVisible(!enabled);
            if (wasPressed && enabled && contains(e.getX(), e.getY()) && onAction != null) {
                onAction.run();
            }
        });
    }

    void setOnAction(Runnable r) {
        onAction = r;
    }

    void setOnHover(Runnable r) {
        onHover = r;
    }

    void setButtonFocused(boolean value) {
        focused = value;
    }

    void setEnabled(boolean value) {
        enabled = value;
        shade.setFill(DISABLED);
        shade.setVisible(!value);
        setCursor(value ? Cursor.HAND : Cursor.DEFAULT);
    }

    boolean isButtonEnabled() {
        return enabled;
    }

    void fire() {
        if (enabled && onAction != null) {
            onAction.run();
        }
    }

    /** Called every frame: fades the glow in/out and makes it pulse gently. */
    void update(double dt, double time) {
        double target = (enabled && focused) ? 1 : 0;
        double step = dt * 8;
        level += Math.max(-step, Math.min(step, target - level));
        glow.setOpacity(level * (0.82 + 0.18 * Math.sin(time * 4)));
    }
}
