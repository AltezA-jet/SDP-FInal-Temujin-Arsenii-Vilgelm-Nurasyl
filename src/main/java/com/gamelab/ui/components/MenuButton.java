package com.gamelab.ui.components;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;

/** Shared, skinned button used throughout the application. */
public final class MenuButton extends Pane {
    private static final Color PRESSED = Color.rgb(0, 0, 0, 0.35);
    private static final Color DISABLED = Color.rgb(8, 5, 12, 0.60);
    private static final Color SELECTED = Color.rgb(255, 211, 107, 0.85);
    private static final Image SHARED_FRAME = load("/assets/ui/buttons/button_frame.png");
    private static final Image SHARED_GLOW = load("/assets/ui/buttons/button_glow.png");

    private final ImageView glow = new ImageView();
    private final Rectangle shade;
    private final Rectangle selection;
    private Label textLabel;
    private boolean enabled = true;
    private boolean focused;
    private boolean hovered;
    private boolean pressed;
    private boolean selected;
    private boolean keyboardActivationEnabled = true;
    private double level;
    private Runnable onAction;
    private Runnable onHover;

    /** Main-menu constructor for the pre-rendered label artwork. */
    public MenuButton(Image frameImg, Image labelImg, Image glowImg,
                      double labelX, double labelY, double glowX, double glowY) {
        this(frameImg, glowImg, frameImg.getWidth(), frameImg.getHeight());
        ImageView label = new ImageView(labelImg);
        label.setLayoutX(labelX);
        label.setLayoutY(labelY);
        label.setMouseTransparent(true);
        getChildren().add(1, label);
        glow.setFitWidth(0);
        glow.setFitHeight(0);
        glow.setLayoutX(glowX);
        glow.setLayoutY(glowY);
    }

    /** Text button with the shared frame and glow assets. */
    public MenuButton(String text, double width, double height) {
        this(SHARED_FRAME, SHARED_GLOW, text, null, width, height);
    }

    /** Image-label button, matching the bitmap label style used by the main menu. */
    public MenuButton(Image labelImage, double width, double height) {
        this(SHARED_FRAME, SHARED_GLOW, width, height);
        ImageView label = new ImageView(labelImage);
        label.setPreserveRatio(true);
        label.setSmooth(false);
        label.setFitWidth(Math.max(1, width - 48));
        label.setFitHeight(Math.max(1, Math.min(22, height - 20)));
        label.setMouseTransparent(true);
        StackPane centeredLabel = new StackPane(label);
        centeredLabel.setAlignment(Pos.CENTER);
        centeredLabel.setMouseTransparent(true);
        centeredLabel.setPrefSize(width, height);
        centeredLabel.resizeRelocate(0, 0, width, height);
        getChildren().add(getChildren().size() - 2, centeredLabel);
    }

    /** Button with optional graphic and text, used for inventory items and appearance slots. */
    public MenuButton(String text, Node graphic, double width, double height) {
        this(SHARED_FRAME, SHARED_GLOW, text, graphic, width, height);
    }

    private MenuButton(Image frameImage, Image glowImage, String text, Node graphic, double width, double height) {
        this(frameImage, glowImage, width, height);
        Label label = new Label(text);
        label.setTextFill(Color.web("#e2d3f0"));
        label.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 14px; -fx-font-weight: bold;");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        textLabel = label;
        if (graphic == null) {
            label.setMaxWidth(width - 18);
            getChildren().add(getChildren().size() - 2, label);
        } else {
            graphic.setMouseTransparent(true);
            VBox content = new VBox(5, graphic, label);
            content.setAlignment(Pos.CENTER);
            content.setMaxSize(width - 12, height - 12);
            label.setMaxWidth(width - 16);
            content.setMouseTransparent(true);
            StackPane centered = new StackPane(content);
            centered.setPrefSize(width, height);
            centered.resizeRelocate(0, 0, width, height);
            centered.setMouseTransparent(true);
            getChildren().add(getChildren().size() - 2, centered);
        }
        installEvents();
    }

    private MenuButton(Image frameImg, Image glowImg, double width, double height) {
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);
        setFocusTraversable(true);

        ImageView frame = new ImageView(frameImg);
        frame.setFitWidth(width);
        frame.setFitHeight(height);
        frame.setMouseTransparent(true);

        glow.setImage(glowImg);
        glow.setFitWidth(width + 20);
        glow.setFitHeight(height + 18);
        glow.setLayoutX(-10);
        glow.setLayoutY(-9);
        glow.setBlendMode(BlendMode.ADD);
        glow.setOpacity(0);
        glow.setMouseTransparent(true);

        shade = new Rectangle(width, height, PRESSED);
        shade.setVisible(false);
        shade.setMouseTransparent(true);

        selection = new Rectangle(width - 8, height - 8);
        selection.setLayoutX(4);
        selection.setLayoutY(4);
        selection.setFill(Color.TRANSPARENT);
        selection.setStroke(SELECTED);
        selection.setStrokeWidth(1.5);
        selection.setMouseTransparent(true);
        selection.setVisible(false);

        getChildren().addAll(frame, glow, selection, shade);
        setPickOnBounds(true);
        setCursor(Cursor.HAND);
        installEvents();
    }

    private void installEvents() {
        setOnMouseEntered(e -> {
            hovered = true;
            glow.setOpacity(enabled ? 1 : 0);
            if (enabled && onHover != null) onHover.run();
        });
        setOnMouseExited(e -> {
            hovered = false;
            if (!focused) glow.setOpacity(0);
            pressed = false;
            setTranslateY(0);
            shade.setVisible(!enabled);
        });
        setOnMousePressed(e -> {
            if (enabled) {
                pressed = true;
                shade.setFill(PRESSED);
                shade.setVisible(true);
                setTranslateY(2);
                requestFocus();
            }
        });
        setOnMouseReleased(e -> {
            boolean wasPressed = pressed;
            pressed = false;
            setTranslateY(0);
            shade.setVisible(!enabled);
            if (wasPressed && enabled && contains(e.getX(), e.getY())) fire();
        });
        addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (enabled && isActivationKey(e.getCode())) {
                pressed = true;
                shade.setVisible(true);
                setTranslateY(2);
                e.consume();
            }
        });
        addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (enabled && isActivationKey(e.getCode())) {
                boolean wasPressed = pressed;
                pressed = false;
                shade.setVisible(false);
                setTranslateY(0);
                if (wasPressed && keyboardActivationEnabled) fire();
                e.consume();
            }
        });
    }

    private static boolean isActivationKey(KeyCode code) {
        return code == KeyCode.SPACE || code == KeyCode.ENTER;
    }

    private static Image load(String path) {
        URL url = MenuButton.class.getResource(path);
        if (url == null) throw new IllegalStateException("Missing button asset " + path);
        return new Image(url.toExternalForm());
    }

    public void setOnAction(Runnable action) { onAction = action; }
    public void setOnHover(Runnable action) { onHover = action; }
    public void setButtonFocused(boolean value) { focused = value; }
    public void setKeyboardActivationEnabled(boolean value) { keyboardActivationEnabled = value; }

    public void setSelected(boolean value) {
        selected = value;
        selection.setVisible(value);
        if (textLabel != null) textLabel.setTextFill(value ? Color.web("#ffe5a0") : Color.web("#e2d3f0"));
    }

    public void setEnabled(boolean value) {
        enabled = value;
        if (!value) glow.setOpacity(0);
        shade.setFill(DISABLED);
        shade.setVisible(!value);
        setCursor(value ? Cursor.HAND : Cursor.DEFAULT);
    }

    public boolean isButtonEnabled() { return enabled; }

    public void fire() {
        if (enabled && onAction != null) onAction.run();
    }

    /** Call from the owning screen's AnimationTimer to fade and pulse the glow. */
    public void update(double dt, double time) {
        double target = (enabled && (focused || hovered)) ? 1 : 0;
        double step = dt * 8;
        level += Math.max(-step, Math.min(step, target - level));
        glow.setOpacity(level * (0.82 + 0.18 * Math.sin(time * 4)));
    }
}
