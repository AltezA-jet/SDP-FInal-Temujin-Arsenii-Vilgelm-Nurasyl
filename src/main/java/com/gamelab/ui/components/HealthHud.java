package com.gamelab.ui.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Standalone health HUD for Shadowfall.
 * Does not depend on PlayerCharacter or modify any existing game class.
 * Call setHealth(currentHealth, maximumHealth) whenever player health changes.
 */
public final class HealthHud extends Canvas {

    private static final double WIDTH = 420;
    private static final double HEIGHT = 76;

    private int currentHealth = 100;
    private int maximumHealth = 100;

    public HealthHud() {
        super(WIDTH, HEIGHT);
        setMouseTransparent(true);
        draw();
    }

    /** Updates the displayed health and redraws the bar proportionally. */
    public void setHealth(int currentHealth, int maximumHealth) {
        if (maximumHealth <= 0) {
            throw new IllegalArgumentException("maximumHealth must be greater than zero");
        }
        this.maximumHealth = maximumHealth;
        this.currentHealth = Math.max(0, Math.min(currentHealth, maximumHealth));
        draw();
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaximumHealth() {
        return maximumHealth;
    }

    public double getHealthPercent() {
        return (double) currentHealth / maximumHealth;
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        // Dark backing plate with a restrained gold, pixel-fantasy frame.
        gc.setFill(Color.rgb(12, 10, 15, 0.92));
        gc.fillRect(5, 7, 410, 62);

        gc.setStroke(Color.rgb(65, 43, 31));
        gc.setLineWidth(7);
        gc.strokeRect(7, 9, 406, 58);

        gc.setStroke(Color.rgb(190, 151, 83));
        gc.setLineWidth(2);
        gc.strokeRect(8, 10, 404, 56);

        // Small angular corner ornaments, echoing a fantasy item-frame style.
        gc.setFill(Color.rgb(224, 190, 119));
        gc.fillPolygon(new double[]{8, 22, 8}, new double[]{10, 10, 24}, 3);
        gc.fillPolygon(new double[]{412, 398, 412}, new double[]{10, 10, 24}, 3);
        gc.fillPolygon(new double[]{8, 22, 8}, new double[]{66, 66, 52}, 3);
        gc.fillPolygon(new double[]{412, 398, 412}, new double[]{66, 66, 52}, 3);

        // Bar background.
        double barX = 25;
        double barY = 31;
        double barW = 370;
        double barH = 22;
        gc.setFill(Color.rgb(45, 19, 23));
        gc.fillRect(barX, barY, barW, barH);

        // Red fill is directly proportional to currentHealth / maximumHealth.
        double fillW = barW * getHealthPercent();
        if (fillW > 0) {
            gc.setFill(Color.rgb(174, 31, 39));
            gc.fillRect(barX, barY, fillW, barH);
            gc.setFill(Color.rgb(224, 55, 58));
            gc.fillRect(barX, barY, fillW, 5);
            gc.setFill(Color.rgb(112, 20, 29));
            gc.fillRect(barX, barY + barH - 4, fillW, 4);
        }

        // Thin inner border around the health track.
        gc.setStroke(Color.rgb(222, 190, 126));
        gc.setLineWidth(1);
        gc.strokeRect(barX, barY, barW, barH);

        gc.setFont(Font.font("Serif", FontWeight.BOLD, 17));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.rgb(245, 222, 177));
        gc.fillText("HEALTH", 26, 25);

        gc.setFont(Font.font("Serif", FontWeight.BOLD, 15));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(Color.WHITE);
        gc.fillText(currentHealth + " / " + maximumHealth, 394, 25);
    }
}
