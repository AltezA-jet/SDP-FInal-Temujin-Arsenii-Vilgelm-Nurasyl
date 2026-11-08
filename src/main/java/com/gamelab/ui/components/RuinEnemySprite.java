package com.gamelab.ui.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.net.URL;

/** Pixel-art actor used by the first campaign stage. */
public final class RuinEnemySprite extends Pane {

    public enum Kind {
        SOLDIER,
        ARCHER,
        GUARDIAN
    }

    private static final double WALK_FRAME_TIME = .13;
    // Attack frames stay visible long enough to read clearly at 60 FPS.
    private static final double ATTACK_FRAME_TIME = .16;
    private static final double HURT_FRAME_TIME = .10;

    private final Kind kind;
    private final int frameWidth;
    private final int frameHeight;
    private final double actorWidth;
    private final double actorHeight;
    private final Canvas canvas;
    private final Image idle;
    private final Image walk;
    private final Image meleeAttack;
    private final Image rangedAttack;
    private final Image hurt;

    private double animationTime;
    private double attackTime;
    private double hurtTime;
    private boolean walking;
    private boolean rangedAttackActive;

    public RuinEnemySprite(Kind kind) {
        this.kind = kind;
        frameWidth = kind == Kind.GUARDIAN ? 96 : 64;
        frameHeight = kind == Kind.GUARDIAN ? 128 : 96;
        double scale = kind == Kind.GUARDIAN ? 1.72 : 1.75;
        actorWidth = frameWidth * scale;
        actorHeight = frameHeight * scale;

        String root = switch (kind) {
            case SOLDIER -> "/assets/stage1/enemies/swordsman/swordsman";
            case ARCHER -> "/assets/stage1/enemies/archer/ruin_archer";
            case GUARDIAN -> "/assets/stage1/enemies/miniboss/rune_guardian";
        };

        idle = load(root + "_idle.png");
        walk = load(root + "_walk.png");
        meleeAttack = load(root + (kind == Kind.GUARDIAN ? "_attack_melee.png" : "_attack.png"));
        rangedAttack = kind == Kind.GUARDIAN
                ? load(root + "_attack_ranged.png")
                : meleeAttack;
        hurt = kind == Kind.ARCHER ? load(root + "_hurt.png") : null;

        canvas = new Canvas(actorWidth, actorHeight);
        canvas.setMouseTransparent(true);
        setPrefSize(actorWidth, actorHeight);
        setMinSize(actorWidth, actorHeight);
        setMaxSize(actorWidth, actorHeight);
        setMouseTransparent(true);
        getChildren().add(canvas);
        draw();
    }

    public void setWalking(boolean walking) {
        this.walking = walking;
    }

    /** Supplied atlases face left, so only right-facing actors are mirrored. */
    public void setFacingLeft(boolean facingLeft) {
        setScaleX(facingLeft ? 1 : -1);
    }

    public void setAttacking() {
        setAttacking(kind == Kind.ARCHER);
    }

    public void setAttacking(boolean ranged) {
        rangedAttackActive = ranged;
        attackTime = ATTACK_FRAME_TIME * 4;
    }

    public void setHurt() {
        hurtTime = hurt == null ? .18 : HURT_FRAME_TIME * frameCount(hurt);
    }

    public void update(double dt) {
        animationTime += dt;
        attackTime = Math.max(0, attackTime - dt);
        hurtTime = Math.max(0, hurtTime - dt);
        draw();
    }

    public double getActorWidth() {
        return actorWidth;
    }

    public double getActorHeight() {
        return actorHeight;
    }

    private void draw() {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.clearRect(0, 0, actorWidth, actorHeight);
        graphics.setImageSmoothing(false);

        Image sheet;
        int frame;

        if (hurtTime > 0 && hurt != null) {
            sheet = hurt;
            frame = elapsedFrame(hurt, hurtTime, HURT_FRAME_TIME);
        } else if (attackTime > 0) {
            sheet = rangedAttackActive ? rangedAttack : meleeAttack;
            frame = elapsedFrame(sheet, attackTime, ATTACK_FRAME_TIME);
        } else if (walking) {
            sheet = walk;
            frame = (int) (animationTime / WALK_FRAME_TIME) % frameCount(sheet);
        } else {
            sheet = idle;
            frame = 0;
        }

        graphics.drawImage(
                sheet,
                frame * frameWidth,
                0,
                frameWidth,
                frameHeight,
                0,
                0,
                actorWidth,
                actorHeight
        );
    }

    private int elapsedFrame(Image sheet, double remainingTime, double frameTime) {
        int frames = frameCount(sheet);
        double duration = frames * frameTime;
        return Math.min(frames - 1, Math.max(0, (int) ((duration - remainingTime) / frameTime)));
    }

    private int frameCount(Image sheet) {
        return Math.max(1, (int) Math.round(sheet.getWidth() / frameWidth));
    }

    private static Image load(String path) {
        URL resource = RuinEnemySprite.class.getResource(path);
        if (resource == null) throw new IllegalStateException("Missing stage-one sprite: " + path);
        return new Image(resource.toExternalForm());
    }
}
