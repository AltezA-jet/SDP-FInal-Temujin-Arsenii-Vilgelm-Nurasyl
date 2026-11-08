package com.gamelab.ui.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/** Temporary pixel-art actors for stage one. Replace their drawing with sprite sheets later. */
public final class RuinEnemySprite extends Pane {

    public enum Kind {
        SOLDIER,
        ARCHER,
        GUARDIAN
    }

    private final Kind kind;
    private final Canvas canvas;
    private double time;
    private double attackTime;
    private boolean walking;

    public RuinEnemySprite(Kind kind) {
        this.kind = kind;
        double width = kind == Kind.GUARDIAN ? 190 : 120;
        double height = kind == Kind.GUARDIAN ? 220 : 150;
        canvas = new Canvas(width, height);
        canvas.setMouseTransparent(true);
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);
        getChildren().add(canvas);
        draw();
    }

    public void setWalking(boolean walking) {
        this.walking = walking;
    }

    public void setFacingLeft(boolean facingLeft) {
        setScaleX(facingLeft ? -1 : 1);
    }

    public void setAttacking() {
        attackTime = .24;
    }

    public void update(double dt) {
        time += dt;
        attackTime = Math.max(0, attackTime - dt);
        draw();
    }

    public double getActorWidth() {
        return canvas.getWidth();
    }

    public double getActorHeight() {
        return canvas.getHeight();
    }

    private void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setImageSmoothing(false);
        int step = walking && ((int) (time / .14) & 1) == 1 ? 6 : 0;
        double flash = attackTime > 0 ? 1 : 0;

        switch (kind) {
            case SOLDIER -> drawSoldier(g, step, flash);
            case ARCHER -> drawArcher(g, step, flash);
            case GUARDIAN -> drawGuardian(g, step, flash);
        }
    }

    private void drawSoldier(GraphicsContext g, int step, double flash) {
        g.setFill(Color.web("#18131d"));
        g.fillRect(31, 39, 58, 70);
        g.setFill(Color.web(flash > 0 ? "#b88655" : "#665a67"));
        g.fillRect(39, 25, 44, 38);
        g.fillRect(27, 59, 66, 49);
        g.setFill(Color.web("#332b38"));
        g.fillRect(34, 13, 54, 24);
        g.fillRect(29, 28, 10, 26);
        g.setFill(Color.web("#df9b3d"));
        g.fillRect(38, 34, 31, 5);
        g.fillRect(53, 73, 9, 25);
        g.setFill(Color.web("#8a5d32"));
        g.fillRect(17, 70, 13, 52);
        g.setFill(Color.web("#c7b7a4"));
        g.fillRect(13, 38, 6, 75);
        g.fillRect(8, 35, 16, 7);
        g.setFill(Color.web("#443a49"));
        g.fillRect(35, 104, 20, 36 + step);
        g.fillRect(68, 104, 20, 42 - step);
        g.setFill(Color.web("#211a27"));
        g.fillRect(28, 137 + step, 32, 10);
        g.fillRect(63, 139 - step, 33, 10);
    }

    private void drawArcher(GraphicsContext g, int step, double flash) {
        g.setFill(Color.web("#211726"));
        g.fillRect(34, 38, 54, 75);
        g.setFill(Color.web(flash > 0 ? "#8d754d" : "#504358"));
        g.fillRect(41, 26, 40, 38);
        g.setFill(Color.web("#2d2134"));
        g.fillPolygon(new double[]{27, 61, 94}, new double[]{48, 4, 48}, 3);
        g.fillRect(27, 43, 67, 65);
        g.setFill(Color.web("#a8c56d"));
        g.fillRect(48, 35, 24, 5);
        g.setFill(Color.web("#8d6338"));
        g.setLineWidth(5);
        g.strokeArc(5, 38, 52, 91, -80, 160, javafx.scene.shape.ArcType.OPEN);
        g.setStroke(Color.web("#d2c3a3"));
        g.setLineWidth(2);
        g.strokeLine(31, 43, 31, 125);
        g.setFill(Color.web("#3e3045"));
        g.fillRect(38, 106, 19, 34 + step);
        g.fillRect(68, 106, 19, 40 - step);
        g.setFill(Color.web("#1b151f"));
        g.fillRect(31, 137 + step, 31, 10);
        g.fillRect(63, 139 - step, 31, 10);
    }

    private void drawGuardian(GraphicsContext g, int step, double flash) {
        g.setFill(Color.web("#17121b"));
        g.fillRect(31, 54, 128, 105);
        g.setFill(Color.web(flash > 0 ? "#94725a" : "#51464f"));
        g.fillRect(58, 20, 76, 57);
        g.fillRect(39, 70, 112, 82);
        g.fillRect(8, 78, 42, 102);
        g.fillRect(142, 78, 40, 102);
        g.setFill(Color.web("#2d2631"));
        g.fillRect(49, 6, 92, 30);
        g.fillRect(24, 61, 42, 32);
        g.fillRect(126, 61, 42, 32);
        g.setFill(Color.web("#ff9c38"));
        g.fillRect(69, 45, 54, 7);
        g.fillRect(91, 84, 10, 52);
        g.fillRect(72, 103, 48, 9);
        g.setFill(Color.web("#463a45"));
        g.fillRect(47, 151, 38, 55 + step);
        g.fillRect(108, 151, 38, 61 - step);
        g.setFill(Color.web("#211a25"));
        g.fillRect(34, 198 + step, 57, 18);
        g.fillRect(101, 202 - step, 58, 18);
    }
}
