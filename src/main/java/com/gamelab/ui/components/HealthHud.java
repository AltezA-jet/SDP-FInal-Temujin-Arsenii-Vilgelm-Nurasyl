package com.gamelab.ui.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/** Standalone player health HUD. */
public final class HealthHud extends Canvas {
    private static final double WIDTH = 420, HEIGHT = 76;
    private int currentHealth = 100, maximumHealth = 100;

    public HealthHud() { super(WIDTH, HEIGHT); setMouseTransparent(true); draw(); }

    public void setHealth(int currentHealth, int maximumHealth) {
        if (maximumHealth <= 0) throw new IllegalArgumentException("maximumHealth must be greater than zero");
        this.maximumHealth = maximumHealth;
        this.currentHealth = Math.max(0, Math.min(currentHealth, maximumHealth));
        draw();
    }

    public int getCurrentHealth() { return currentHealth; }
    public int getMaximumHealth() { return maximumHealth; }
    public double getHealthPercent() { return (double) currentHealth / maximumHealth; }

    private void draw() {
        GraphicsContext g = getGraphicsContext2D();
        g.clearRect(0, 0, WIDTH, HEIGHT);
        g.setFill(Color.rgb(12, 10, 15, .92)); g.fillRect(5, 7, 410, 62);
        g.setStroke(Color.rgb(190, 151, 83)); g.setLineWidth(2); g.strokeRect(8, 10, 404, 56);
        double barX = 25, barY = 31, barW = 370, barH = 22;
        g.setFill(Color.rgb(45, 19, 23)); g.fillRect(barX, barY, barW, barH);
        double fillW = barW * getHealthPercent();
        if (fillW > 0) { g.setFill(Color.rgb(174, 31, 39)); g.fillRect(barX, barY, fillW, barH); }
        g.setStroke(Color.rgb(222, 190, 126)); g.setLineWidth(1); g.strokeRect(barX, barY, barW, barH);
        g.setFont(Font.font("Serif", FontWeight.BOLD, 17)); g.setTextAlign(TextAlignment.LEFT);
        g.setFill(Color.rgb(245, 222, 177)); g.fillText("HEALTH", 26, 25);
        g.setFont(Font.font("Serif", FontWeight.BOLD, 15)); g.setTextAlign(TextAlignment.RIGHT);
        g.setFill(Color.WHITE); g.fillText(currentHealth + " / " + maximumHealth, 394, 25);
    }
}
