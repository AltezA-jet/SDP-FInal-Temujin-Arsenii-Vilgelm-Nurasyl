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

/** Layered player sprite. The legacy class name is kept for compatibility. */
public final class MaleWarriorSprite extends Pane {
    public static final double GAME_SCALE=1.5, GAME_HEIGHT=96*GAME_SCALE;
    private static final int FRAME_WIDTH = 64, FRAME_HEIGHT = 96, FRAME_COUNT = 4;
    private final ImageView sprite = new ImageView();
    private final Map<String, Image> images = new HashMap<>();
    private String hairName = "black", shirtName = "blue", pantsName = "blue", bootsName = "blue", state = "idle";
    private String classId = "warrior";
    private int headwearIndex = -1, frame;
    private double frameTime;
    private boolean attacking, walking;
    private String swordName="starter_sword";
    private double attackFrameDuration=.10;
    private int footPadding;
    public double getFootOffset() {
        double height=prefHeight(-1);
        return SpriteGrounding.footOffset(height,footPadding*height/FRAME_HEIGHT,getScaleY());
    }

    public void setWeapon(String name) { swordName=name.toLowerCase(java.util.Locale.ROOT).replace(' ','_'); render(); }
    public void setClass(String characterClass) {
        String value = characterClass == null ? "" : characterClass.toUpperCase(java.util.Locale.ROOT);
        classId = value.contains("ARCHER") ? "archer" : value.contains("MAGE") ? "mage"
                : value.contains("ASSASSIN") ? "assasin" : "warrior";
        render();
    }
    public void setAttackDuration(double seconds) { attackFrameDuration=seconds/FRAME_COUNT; }
    public void cancelAttack() {
        if(!attacking) return;
        attacking=false; state=walking?"walk":"idle"; frame=0; frameTime=0; render();
    }

    public MaleWarriorSprite(double size) {
        double width = size * FRAME_WIDTH / (double) FRAME_HEIGHT;
        setPrefSize(width, size); setMinSize(width, size); setMaxSize(width, size);
        sprite.setPreserveRatio(true); sprite.setSmooth(false);
        sprite.fitWidthProperty().bind(widthProperty()); sprite.fitHeightProperty().bind(heightProperty());
        sprite.setMouseTransparent(true); getChildren().add(sprite);
        setConfiguration(0, 0, 0, 0, -1);
    }

    public void setConfiguration(int hairIndex, int outfitIndex) { setConfiguration(hairIndex, outfitIndex, headwearIndex); }

    public void setConfiguration(int hairIndex, int outfitIndex, int headwearIndex) {
        setConfiguration(hairIndex, outfitIndex, outfitIndex, outfitIndex, headwearIndex);
    }

    public void setConfiguration(int hairIndex, int shirtIndex, int pantsIndex,
                                 int bootsIndex, int headwearIndex) {
        String[] hairs = {"black", "brown", "red", "blond", "blue", "purple", "pink"};
        String[] outfits = {"blue", "brown", "green", "grey", "purple"};
        hairName = hairs[Math.max(0, Math.min(hairs.length - 1, hairIndex))];
        shirtName = outfits[Math.max(0, Math.min(outfits.length - 1, shirtIndex))];
        pantsName = outfits[Math.max(0, Math.min(outfits.length - 1, pantsIndex))];
        bootsName = outfits[Math.max(0, Math.min(outfits.length - 1, bootsIndex))];
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
        double speed = attacking ? attackFrameDuration : walking ? .12 : Double.POSITIVE_INFINITY;
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
        boolean movementFrame = walking && !attacking;
        String pose = movementFrame ? "walk" : "idle";
        boolean attackSheet = state.equals("attack") && !classId.equals("warrior");
        boolean sheet = movementFrame || attackSheet;
        int poseFrame = sheet ? frame : 0;
        String prefix = classId.equals("warrior") ? "" : classId.equals("assasin") ? "assassin_" : classId + "_";
        String assetPose = attackSheet ? "attack" : pose;
        String bodyName = classId.equals("warrior") ? "body_" + assetPose + ".png" : prefix + "base_" + assetPose + ".png";
        Image body = image(bodyName);
        WritableImage result = copy(body, poseFrame, sheet);
        Image bodyReference = image(bodyName);
        String action = state.equals("attack") && !classId.equals("warrior") ? "Attack" : movementFrame ? "WALK" : "STAY";
        String classPose = state.equals("attack") && !classId.equals("warrior") ? "attack" : pose;

        // All clothing files contain the complete silhouette. Only their changed pixels are applied.
        overlayChanged(result, bodyReference, image("PANTS_" + action + "/" + prefix + "pants_" + pantsName + "_" + classPose + ".png"), poseFrame, sheet);
        overlayChanged(result, bodyReference, image("SHIRT_" + action + "/" + prefix + "shirt_" + shirtName + "_" + classPose + ".png"), poseFrame, sheet);
        overlayChanged(result, bodyReference, image("HAIR_" + action + "/" + prefix + "hair_" + hairName + "_" + classPose + ".png"), poseFrame, sheet);
        if (headwearIndex >= 0) {
            String[] hats = {"blue", "brown", "green", "grey", "purple"};
            String hat = hats[headwearIndex];
            String folder = classId.equals("warrior") ? "HEADWAIR_" + action : action.equals("Attack") ? "HEADWAIR_attack" : "HEADWAIR_" + action;
            String file = classId.equals("warrior") && movementFrame && hat.equals("grey") ? "headwear_grey_walk.png" : prefix + "headwear_" + hat + "_" + classPose + ".png";
            overlayChanged(result, bodyReference, image(folder + "/" + file), poseFrame, sheet);
        }

        String weaponFolder = classId.equals("archer") ? "BOW_" : classId.equals("assasin") ? "DAGGERS_" : classId.equals("mage") ? "STAFF_" : "SWORD_";
        String weaponPrefix = classId.equals("archer") ? "archer_bow_" : classId.equals("assasin") ? "assassin_dagger_" : classId.equals("mage") ? "mage_staff_" : "";
        String weaponName = classId.equals("warrior") ? swordName : weaponVariant();
        String weaponAction = state.equals("attack") ? "ATTACK" : action;
        String weaponPose = state.equals("attack") ? "attack" : classPose;
        overlayAll(result, image(weaponFolder + weaponAction + "/" + weaponPrefix + weaponName + "_" + weaponPose + ".png"), frame, true);
        // Boots are the final composite layer. This prevents the body, trousers,
        // headwear or weapon layer from hiding their darker edge pixels.
        overlayChanged(result, bodyReference, image("BOOTS_" + action + "/" + prefix + "boots_" + bootsName + "_" + classPose + ".png"), poseFrame, sheet);
        // Use the body's feet so a downward sword swing cannot move the character's ground anchor.
        footPadding=SpriteGrounding.bottomPadding(bodyReference.getPixelReader(),movementFrame?poseFrame*FRAME_WIDTH:0,FRAME_WIDTH,FRAME_HEIGHT);
        sprite.setViewport(null); sprite.setImage(result);
    }

    private String weaponVariant() {
        int level = swordName.contains("ancient") ? 4 : swordName.contains("darksteel") ? 3
                : swordName.contains("greatsword") ? 2 : swordName.contains("swift") ? 1 : 0;
        return switch (classId) {
            case "archer" -> new String[]{"start", "light", "hunter", "long", "dark"}[level];
            case "assasin" -> new String[]{"start", "curved", "royal", "shadow", "venom"}[level];
            default -> new String[]{"start", "arcane", "dark", "holy", "nature"}[level];
        };
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
            if ((a >>> 24) != 0 && colorDistance(a, b) > 2) out.setArgb(x, y, a);
        }
    }

    private void overlayAll(WritableImage target, Image layer, int index, boolean sheet) {
        PixelReader src = layer.getPixelReader(); PixelWriter out = target.getPixelWriter(); int offset = sheet ? index * FRAME_WIDTH : 0;
        for (int y = 0; y < FRAME_HEIGHT; y++) for (int x = 0; x < FRAME_WIDTH; x++) { int a = src.getArgb(offset + x, y); if ((a >>> 24) != 0) out.setArgb(x, y, a); }
    }

    private int colorDistance(int a, int b) {
        return Math.abs(((a >>> 16) & 255) - ((b >>> 16) & 255)) + Math.abs(((a >>> 8) & 255) - ((b >>> 8) & 255)) + Math.abs((a & 255) - (b & 255));
    }

    private Image image(String path) { return images.computeIfAbsent(classId + ":" + path, ignored -> {
        String folder = classId.equals("assasin") ? "male_assasin" : "male_" + classId;
        URL url = MaleWarriorSprite.class.getResource("/assets/" + folder + "/" + path);
        if (url == null) throw new IllegalStateException("Missing " + classId + " sprite " + path);
        return new Image(url.toExternalForm());
    }); }
}
