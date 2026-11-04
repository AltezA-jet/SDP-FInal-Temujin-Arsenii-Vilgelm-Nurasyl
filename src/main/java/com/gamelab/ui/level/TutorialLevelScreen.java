package com.gamelab.ui.level;

import com.gamelab.domain.PlayerCharacter;
import com.gamelab.ui.components.MaleWarriorSprite;
import com.gamelab.ui.components.IceEnemySprite;
import com.gamelab.ui.components.MenuButton;
import com.gamelab.ui.components.EnemyHealthHud;
import com.gamelab.ui.components.HealthHud;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.LinkedHashMap;
import java.util.Map;

/** Level 0: a small playable training yard teaching the four core controls. */
public final class TutorialLevelScreen extends Pane {
    public static final double W = 1920, H = 1080;
    private static final double GROUND_Y = 790;
    private static final double SPRITE_BOTTOM_MARGIN = 0;
    private final PlayerCharacter player;
    private final Runnable onComplete;
    private final Canvas world = new Canvas(W, H);
    private final Image trainingBackground = new Image(
            TutorialLevelScreen.class
                    .getResource("/assets/training level textures/training bg.jpg")
                    .toExternalForm());
    private final MaleWarriorSprite hero = new MaleWarriorSprite(210);
    private final IceEnemySprite enemy = new IceEnemySprite(210);
    private final HealthHud healthHud = new HealthHud();
    private final EnemyHealthHud enemyHealthHud = new EnemyHealthHud("tutorial-enemy", 3, 3);
    private final ImageView snowball = new ImageView(new Image(TutorialLevelScreen.class.getResource("/assets/2 Этап-Enemy(1,2,3)/ice_snowball.png").toExternalForm()));
    private final Label objective = label("", 46, 164, 520, 42, "#ffe3a0");
    private final Label progress = label("", 46, 214, 520, 32, "#d9c8b6");
    private final Map<String, Boolean> done = new LinkedHashMap<>();
    private double x = 800, y = GROUND_Y - SPRITE_BOTTOM_MARGIN, vy, time, enemyX = 1450;
    private double snowX, snowY, snowTimer;
    private boolean snowballFlying, enemyAlive = true;
    private int enemyDirection = -1, enemyHealth = 3;
    private boolean left, right, crouching, jumping;
    private long last;
    private final AnimationTimer timer = new AnimationTimer() {
        @Override public void handle(long now) {
            double dt = last == 0 ? .016 : Math.min(.05, (now - last) / 1e9);
            last = now; time += dt; update(dt); draw();
        }
    };

    public TutorialLevelScreen(PlayerCharacter player, Runnable onComplete) {
        this.player = player; this.onComplete = onComplete;
        done.put("walk", false); done.put("jump", false); done.put("crouch", false); done.put("attack", false);
        setPrefSize(W, H); setMinSize(W, H); setMaxSize(W, H); setClip(new Rectangle(W, H));
        getChildren().add(world);
        hero.setConfiguration(player.getHairColorIndex(), player.getOutfitIndex(), player.getHeadwearIndex()); hero.relocate(x, y - 185); getChildren().add(hero);
        enemy.relocate(1450, GROUND_Y - 190); getChildren().add(enemy);
        healthHud.relocate(32, 32); healthHud.setHealth(player.getHealth(), player.getMaxHealth()); getChildren().add(healthHud);
        enemyHealthHud.relocate(enemyX - 55, GROUND_Y - 220); getChildren().add(enemyHealthHud);
        snowball.setFitWidth(34); snowball.setFitHeight(34); snowball.setSmooth(false); snowball.setVisible(false); snowball.setMouseTransparent(true); getChildren().add(snowball);
        buildHud();
        setFocusTraversable(true); requestFocus();
        setOnKeyPressed(e -> key(e.getCode(), true)); setOnKeyReleased(e -> key(e.getCode(), false));
        timer.start();
    }

    private void buildHud() {
        Label title = label("LEVEL 0  //  TRAINING YARD", 46, 46, 800, 56, "#f6c66d"); getChildren().add(title);
        Label subtitle = label("Освой управление, чтобы открыть первую локацию", 21, 48, 108, 700, 34, "#c8b39b"); getChildren().add(subtitle);
        Rectangle card = new Rectangle(600, 270, Color.rgb(11, 8, 18, .88)); card.setLayoutX(42); card.setLayoutY(140); card.setStroke(Color.web("#96613c")); getChildren().add(card);
        getChildren().addAll(objective, progress);
        MenuButton attack = new MenuButton("J / ЛКМ  УДАР", 300, 58); attack.relocate(1470, 870); attack.setOnAction(() -> { hero.setAttacking(); attackEnemy(); action("attack"); }); getChildren().add(attack);
        Label keys = label("A / D или ← / →   ходьба\nW / SPACE          прыжок\nS                    присяд\nJ / ЛКМ              удар", 22, 1470, 742, 390, 112, "#e8d5bb"); keys.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 18px; -fx-line-spacing: 8px;"); getChildren().add(keys);
        Label hint = label("Доберись до костра после выполнения всех упражнений", 18, 600, 950, 720, 30, "#caa56e"); hint.setAlignment(Pos.CENTER); getChildren().add(hint);
        refreshHud();
    }

    private void update(double dt) {
        boolean walking = left || right;
        if (left) x -= 260 * dt; if (right) x += 260 * dt; x = Math.max(120, Math.min(1620, x));
        if (walking) action("walk");
        if (enemyAlive) {
            enemyX += enemyDirection * 72 * dt;
            if (enemyX < 1190 || enemyX > 1500) enemyDirection *= -1;
            // Enemy atlas faces left by default: mirror only when the hero is to its right.
            enemy.setWalking(true); enemy.setFacingLeft(enemyX < x); enemy.relocate(enemyX, 565); enemy.update(dt);
            enemyHealthHud.relocate(enemyX - 55, GROUND_Y - 220);
            snowTimer += dt;
        }
        if (enemyAlive && !snowballFlying && snowTimer > 2.2) { snowTimer = 0; snowballFlying = true; snowX = enemyX + 40; snowY = 665; enemy.setAttacking(); snowball.setVisible(true); }
        if (snowballFlying) { snowX -= 420 * dt; snowY += Math.sin(time * 8) * 3; snowball.relocate(snowX, snowY); if (snowX < x - 30) { snowballFlying = false; snowball.setVisible(false); } }
        if (jumping) { vy += 1050 * dt; y += vy * dt; if (y >= GROUND_Y - SPRITE_BOTTOM_MARGIN) { y = GROUND_Y - SPRITE_BOTTOM_MARGIN; vy = 0; jumping = false; } }
        hero.relocate(x, y - 185);
        if (left && !right) hero.setFacingLeft(true);
        else if (right && !left) hero.setFacingLeft(false);
        hero.setWalking(walking, crouching); hero.update(dt);
        draw();
    }

    private void key(KeyCode code, boolean pressed) {
        if (code == KeyCode.A || code == KeyCode.LEFT) left = pressed;
        if (code == KeyCode.D || code == KeyCode.RIGHT) right = pressed;
        if (pressed && (code == KeyCode.W || code == KeyCode.UP || code == KeyCode.SPACE) && !jumping && y >= 750) { jumping = true; vy = -570; action("jump"); }
        if (code == KeyCode.S || code == KeyCode.DOWN) { crouching = pressed; if (pressed) action("crouch"); }
        if (pressed && code == KeyCode.J) { hero.setAttacking(); attackEnemy(); action("attack"); }
    }

    private void attackEnemy() {
        if (!enemyAlive || Math.abs(enemyX - x) > 240) return;
        enemyHealth--;
        enemyHealthHud.setHealth(enemyHealth, 3);
        if (enemyHealth <= 0) {
            enemyAlive = false; enemy.setVisible(false); snowballFlying = false; snowball.setVisible(false);
            enemyHealthHud.setVisible(false);
        }
    }

    private void action(String name) { if (!done.get(name)) { done.put(name, true); refreshHud(); } }

    private void refreshHud() {
        String current = done.get("walk") ? done.get("jump") ? done.get("crouch") ? done.get("attack") ? "Готово! Вход в локацию открыт." : "Теперь попробуй ударить (J или кнопка)" : "Теперь присядь (S)" : "Теперь прыгни (W / SPACE)" : "Начни с ходьбы (A / D)";
        objective.setText(current);
        progress.setText("Ходьба " + mark("walk") + "   Прыжок " + mark("jump") + "\nПрисяд " + mark("crouch") + "   Удар " + mark("attack"));
        if (done.values().stream().allMatch(Boolean::booleanValue)) {
            MenuButton next = new MenuButton("ПЕРЕЙТИ В ЛОКАЦИЮ 1", 420, 66); next.relocate(750, 930); next.setOnAction(() -> { timer.stop(); onComplete.run(); }); getChildren().add(next);
        }
    }
    private String mark(String key) { return done.get(key) ? "✓" : "○"; }

    private void draw() {
        GraphicsContext g = world.getGraphicsContext2D();
        g.drawImage(trainingBackground, 0, 0, W, H);
        g.setFill(Color.rgb(0, 0, 0, .35)); g.fillRect(0, 0, W, 118); g.setFill(Color.rgb(9, 7, 15, .78)); g.fillRect(42, 140, 600, 270);
    }
    private static Label label(String text, double x, double y, double w, double h, String color) { Label l = new Label(text); l.relocate(x, y); l.setPrefSize(w, h); l.setTextFill(Color.web(color)); l.setStyle("-fx-font-family: 'Monospaced'; -fx-font-weight: bold;"); return l; }
    private static Label label(String text, double size, double x, double y, double w, double h, String color) { Label l = label(text, x, y, w, h, color); l.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: " + size + "px; -fx-font-weight: bold;"); return l; }
    public void stop() { timer.stop(); }
}
