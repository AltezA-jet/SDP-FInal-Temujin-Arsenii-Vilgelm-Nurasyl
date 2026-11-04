package com.gamelab.ui.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/** Compact world-space health bar for one enemy. */
public final class EnemyHealthHud extends Canvas {
    private final String enemyId;
    private int current, maximum;

    public EnemyHealthHud(String enemyId, int current, int maximum) {
        super(110, 18);
        if (enemyId == null || enemyId.isBlank()) throw new IllegalArgumentException("enemyId required");
        this.enemyId = enemyId;
        setMouseTransparent(true);
        setHealth(current, maximum);
    }
    public String getEnemyId() { return enemyId; }
    public int getCurrentHealth() { return current; }
    public int getMaximumHealth() { return maximum; }
    public void setHealth(int health, int max) {
        if (max <= 0) throw new IllegalArgumentException("maximum health must be positive");
        maximum = max; current = Math.max(0, Math.min(health, max));
        GraphicsContext g = getGraphicsContext2D(); g.clearRect(0, 0, 110, 18);
        g.setFill(Color.rgb(12, 9, 14, .9)); g.fillRoundRect(1, 1, 108, 16, 4, 4);
        g.setFill(Color.rgb(65, 17, 25)); g.fillRect(5, 5, 100, 8);
        g.setFill(Color.rgb(178, 30, 39)); g.fillRect(5, 5, 100.0 * current / maximum, 8);
        g.setStroke(Color.rgb(151, 111, 78)); g.setLineWidth(1); g.strokeRect(1.5, 1.5, 107, 15);
        g.setFill(Color.rgb(250, 220, 190)); g.setFont(Font.font("Serif", 9));
        g.fillText(current + "/" + maximum, 76, 13);
    }
}
