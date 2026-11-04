package com.gamelab.ui.components;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** Composites the supplied full-silhouette layers into a correctly ordered 64x96 frame. */
public final class MaleWarriorSprite extends Pane {
    private static final String ROOT = "/assets/male_warrior/";
    private static final int FRAME_WIDTH = 64, FRAME_HEIGHT = 96, FRAME_COUNT = 4;
    private final ImageView sprite = new ImageView();
    private final Map<String, Image> images = new HashMap<>();
    private String hairName = "black", outfitName = "blue", state = "idle";
    private int headwearIndex = -1, frame;
    private double frameTime;
    private boolean attacking, walking;

    public MaleWarriorSprite(double size) {
        setPrefSize(size, size); setMinSize(size, size); setMaxSize(size, size);
        sprite.setPreserveRatio(true); sprite.setSmooth(false);
        sprite.fitWidthProperty().bind(widthProperty()); sprite.fitHeightProperty().bind(heightProperty());
        sprite.setMouseTransparent(true); getChildren().add(sprite);
        setConfiguration(0, 0, -1);
    }

    public void setConfiguration(int hairIndex, int outfitIndex) { setConfiguration(hairIndex, outfitIndex, headwearIndex); }

    public void setConfiguration(int hairIndex, int outfitIndex, int headwearIndex) {
        String[] hairs = {"black", "brown", "red", "blond", "blue", "purple", "pink"};
        String[] outfits = {"blue", "brown", "green", "grey", "purple"};
        hairName = hairs[Math.max(0, Math.min(hairs.length - 1, hairIndex))];
        outfitName = outfits[Math.max(0, Math.min(outfits.length - 1, outfitIndex))];
        this.headwearIndex = Math.max(-1, Math.min(4, headwearIndex));
        state = "idle"; walking = false; attacking = false; frame = 0; frameTime = 0; render();
    }

    public void setWalking(boolean walking, boolean crouching) {
        this.walking = walking;
        setScaleY(crouching ? .72 : 1);
        if (attacking) { render(); return; }
        String next = walking ? "walk" : "idle";
        if (!next.equals(state)) { state = next; frame = 0; frameTime = 0; render(); }
    }

    /** Direction is intentionally not reset when the character stops. */
    public void setFacingLeft(boolean facingLeft) { setScaleX(facingLeft ? -1 : 1); }

    public void setAttacking() {
        attacking = true; state = "attack"; frame = 0; frameTime = 0; render();
    }

    public void update(double dt) {
        double speed = attacking ? .085 : walking ? .12 : Double.POSITIVE_INFINITY;
        if (speed == Double.POSITIVE_INFINITY) return;
        frameTime += dt;
        while (frameTime >= speed) {
            frameTime -= speed; frame++;
            if (frame >= FRAME_COUNT) {
                if (attacking) { attacking = false; state = walking ? "walk" : "idle"; }
                frame = 0;
            }
            render();
        }
    }

    private void render() {
        boolean movementFrame = walking;
        String pose = movementFrame ? "walk" : "idle";
        int poseFrame = movementFrame ? frame : 0;
        Image body = image("body_" + pose + ".png");
        WritableImage result = copy(body, poseFrame, movementFrame);
        Image bodyReference = image("body_" + pose + ".png");

        // All clothing files contain the complete silhouette. Only their changed pixels are applied.
        overlayChanged(result, bodyReference, image("PANTS_" + (movementFrame ? "WALK" : "STAY") + "/pants_" + outfitName + "_" + pose + ".png"), poseFrame, movementFrame);
        overlayChanged(result, bodyReference, image("SHIRT_" + (movementFrame ? "WALK" : "STAY") + "/shirt_" + outfitName + "_" + pose + ".png"), poseFrame, movementFrame);
        overlayChanged(result, bodyReference, image("HAIR_" + (movementFrame ? "WALK" : "STAY") + "/hair_" + hairName + "_" + pose + ".png"), poseFrame, movementFrame);
        // Boots are applied last so they remain above the body and trousers in every frame.
        overlayChanged(result, bodyReference, image("BOOTS_" + (movementFrame ? "WALK" : "STAY") + "/boots_" + outfitName + "_" + pose + ".png"), poseFrame, movementFrame);
        if (headwearIndex >= 0) {
            String[] hats = {"blue", "brown", "green", "grey", "purple"};
            String hat = hats[headwearIndex];
            String folder = movementFrame ? "HEADWAIR_WALK" : "HEADWAIR_STAY";
            String file = movementFrame && hat.equals("grey") ? "headwear_grey_walk.png" : "headwair_" + hat + "_" + (movementFrame ? "walk" : "idle") + ".png";
            overlayChanged(result, bodyReference, image(folder + "/" + file), poseFrame, movementFrame);
        }

        String swordFolder = state.equals("attack") ? "ATTACK" : movementFrame ? "WALK" : "STAY";
        String swordPose = state.equals("attack") ? "attack" : pose;
        overlayAll(result, image("SWORD_" + swordFolder + "/starter_sword_" + swordPose + ".png"), frame, true);
        sprite.setViewport(null); sprite.setImage(result);
    }

    private WritableImage copy(Image source, int index, boolean sheet) {
        WritableImage result = new WritableImage(FRAME_WIDTH, FRAME_HEIGHT);
        PixelWriter writer = result.getPixelWriter(); PixelReader reader = source.getPixelReader();
        int offset = sheet ? index * FRAME_WIDTH : 0;
        for (int y = 0; y < FRAME_HEIGHT; y++) for (int x = 0; x < FRAME_WIDTH; x++) writer.setArgb(x, y, reader.getArgb(offset + x, y));
        return result;
    }

    private void overlayChanged(WritableImage target, Image reference, Image layer, int index, boolean sheet) {
        PixelReader ref = reference.getPixelReader(); PixelReader src = layer.getPixelReader(); PixelWriter out = target.getPixelWriter();
        int offset = sheet ? index * FRAME_WIDTH : 0;
        for (int y = 0; y < FRAME_HEIGHT; y++) for (int x = 0; x < FRAME_WIDTH; x++) {
            int a = src.getArgb(offset + x, y); int b = ref.getArgb(offset + x, y);
            if ((a >>> 24) != 0 && colorDistance(a, b) > 10) out.setArgb(x, y, a);
        }
    }

    private void overlayAll(WritableImage target, Image layer, int index, boolean sheet) {
        PixelReader src = layer.getPixelReader(); PixelWriter out = target.getPixelWriter(); int offset = sheet ? index * FRAME_WIDTH : 0;
        for (int y = 0; y < FRAME_HEIGHT; y++) for (int x = 0; x < FRAME_WIDTH; x++) { int a = src.getArgb(offset + x, y); if ((a >>> 24) != 0) out.setArgb(x, y, a); }
    }

    private int colorDistance(int a, int b) {
        return Math.abs(((a >>> 16) & 255) - ((b >>> 16) & 255)) + Math.abs(((a >>> 8) & 255) - ((b >>> 8) & 255)) + Math.abs((a & 255) - (b & 255));
    }

    private Image image(String path) { return images.computeIfAbsent(path, p -> { URL url = MaleWarriorSprite.class.getResource(ROOT + p); if (url == null) throw new IllegalStateException("Missing warrior sprite " + p); return new Image(url.toExternalForm()); }); }
}
