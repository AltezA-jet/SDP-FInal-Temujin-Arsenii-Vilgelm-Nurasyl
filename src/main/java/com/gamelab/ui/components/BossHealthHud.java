package com.gamelab.ui.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/** Fixed screen-space boss health display. */
public final class BossHealthHud extends Canvas {
    private String bossName = "";
    private int current, maximum = 1;
    public BossHealthHud(double width) { super(width, 76); setMouseTransparent(true); }
    public void setBoss(String name, int health, int maxHealth) {
        if (maxHealth <= 0) throw new IllegalArgumentException("maximum health must be positive");
        bossName = name == null ? "BOSS" : name; maximum = maxHealth; current = Math.max(0, Math.min(health, maxHealth)); draw();
    }
    public int getCurrentHealth() { return current; }
    public int getMaximumHealth() { return maximum; }
    private void draw() {
        double w=getWidth(); GraphicsContext g=getGraphicsContext2D(); g.clearRect(0,0,w,76);
        g.setFill(Color.rgb(10,7,12,.95)); g.fillRoundRect(2,2,w-4,72,10,10);
        g.setStroke(Color.rgb(89,28,35)); g.setLineWidth(7); g.strokeRoundRect(4,4,w-8,68,8,8);
        g.setStroke(Color.rgb(190,135,83)); g.setLineWidth(1.5); g.strokeRoundRect(8,8,w-16,60,5,5);
        g.setFill(Color.rgb(50,13,20)); g.fillRect(24,43,w-48,16);
        g.setFill(Color.rgb(143,18,31)); g.fillRect(24,43,(w-48)*current/maximum,16);
        g.setFill(Color.rgb(206,43,52)); g.fillRect(24,43,(w-48)*current/maximum,4);
        g.setStroke(Color.rgb(214,164,112)); g.setLineWidth(1); g.strokeRect(24,43,w-48,16);
        g.setFill(Color.rgb(240,207,163)); g.setFont(Font.font("Serif",FontWeight.BOLD,16)); g.setTextAlign(TextAlignment.CENTER);
        g.fillText(bossName.toUpperCase(),w/2,29);
        g.setFont(Font.font("Serif",FontWeight.BOLD,12)); g.fillText(current+" / "+maximum,w/2,56);
    }
}
