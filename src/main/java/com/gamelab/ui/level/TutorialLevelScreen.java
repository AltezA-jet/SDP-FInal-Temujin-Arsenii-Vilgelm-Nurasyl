package com.gamelab.ui.level;

import com.gamelab.domain.PlayerCharacter;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.application.audio.WeaponImpact;
import com.gamelab.ui.audio.CombatAudio;
import com.gamelab.ui.audio.JavaSoundPlayer;
import com.gamelab.ui.components.MaleWarriorSprite;
import com.gamelab.ui.components.IceEnemySprite;
import com.gamelab.ui.components.MenuButton;
import com.gamelab.ui.components.EnemyHealthHud;
import com.gamelab.ui.components.HealthHud;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.LinkedHashMap;
import java.util.Map;

/** Level 0: a small playable training yard teaching the four core controls. */
public final class TutorialLevelScreen extends Pane {
    public static final double W = 1920, H = 1080;
    private static final double GROUND_Y = 790;
    private static final double SPRITE_BOTTOM_MARGIN = 0;
    private final PlayerCharacter player;
    private final Runnable onComplete;
    private final GameEventBus events=new GameEventBus();
    private final CombatAudio audio=new CombatAudio(events,new JavaSoundPlayer());
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
    private final Label objective = label("", 15, 492, 52, 390, 48, "#ffe3a0");
    private final Label progress = label("", 14, 888, 52, 246, 48, "#f4ead9");
    private final Pane tutorialHints = new Pane();
    private final Pane completionHud = new Pane();
    private final Map<String, Boolean> done = new LinkedHashMap<>();
    private double x = 800, y = GROUND_Y - SPRITE_BOTTOM_MARGIN, vy, time, enemyX = 1450;
    private double snowX, snowY, snowTimer;
    private boolean snowballFlying, enemyAlive = true;
    private int enemyDirection = -1, enemyHealth = 3;
    private boolean left, right, crouching, jumping;
    private boolean nextButtonAdded;
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
        hero.setClass(player.getCharacterClass());
        hero.setConfiguration(
                player.getHairColorIndex(),
                player.getShirtColorIndex(),
                player.getPantsColorIndex(),
                player.getBootsColorIndex(),
                player.getHeadwearIndex()
        ); hero.relocate(x, y - 185); getChildren().add(hero);
        enemy.relocate(1450, GROUND_Y - 190); getChildren().add(enemy);
        healthHud.relocate(32, 32); healthHud.setHealth(player.getHealth(), player.getMaxHealth()); getChildren().add(healthHud);
        enemyHealthHud.relocate(enemyX - 55, GROUND_Y - 220); getChildren().add(enemyHealthHud);
        snowball.setFitWidth(34); snowball.setFitHeight(34); snowball.setSmooth(false); snowball.setVisible(false); snowball.setMouseTransparent(true); getChildren().add(snowball);
        buildHud();
        setFocusTraversable(true); requestFocus();
        setOnKeyPressed(e -> key(e.getCode(), true)); setOnKeyReleased(e -> key(e.getCode(), false));
        sceneProperty().addListener((obs,old,value) -> { if (old!=null && value==null) stop(); });
        world.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                hero.setAttacking();
                attackEnemy();
                action("attack");
            }
        });
        timer.start();
    }

    private void buildHud() {
        Rectangle taskCard = panel(470, 12, 680, 96);
        Rectangle controlsCard = panel(1170, 12, 720, 96);
        taskCard.setEffect(new DropShadow(18, Color.rgb(0, 0, 0, .65)));
        controlsCard.setEffect(new DropShadow(18, Color.rgb(0, 0, 0, .65)));

        Label title = label("STAGE 0  //  TRAINING GROUNDS", 19,
                492, 20, 430, 28, "#f6c66d");
        Label subtitle = label("COMPLETE 4 ACTIONS TO PROCEED", 11,
                888, 22, 246, 24, "#d5b98c");
        subtitle.setAlignment(Pos.CENTER_RIGHT);

        Label controlsTitle = label("CONTROLS", 18,
                1192, 20, 180, 28, "#f6c66d");
        Label controlsHint = label("KEYBOARD  /  MOUSE", 11,
                1644, 22, 222, 24, "#d5b98c");
        controlsHint.setAlignment(Pos.CENTER_RIGHT);

        GridPane keys = new GridPane();
        keys.relocate(1192, 52);
        keys.setHgap(14);
        keys.setVgap(3);
        keys.getColumnConstraints().addAll(
                new ColumnConstraints(100), new ColumnConstraints(116),
                new ColumnConstraints(116), new ColumnConstraints(112));
        addControlPair(keys, 0, 0, "A / D", "MOVE");
        addControlPair(keys, 0, 2, "W / SPACE", "JUMP");
        addControlPair(keys, 1, 0, "S", "CROUCH");
        addControlPair(keys, 1, 2, "J / LEFT MOUSE", "ATTACK");

        tutorialHints.getChildren().addAll(
                taskCard, controlsCard, title, subtitle, objective, progress,
                controlsTitle, controlsHint, keys
        );

        Rectangle completeCard = panel(610, 910, 700, 126);
        completeCard.setEffect(new DropShadow(24, Color.rgb(0, 0, 0, .72)));
        Label completeText = label("✓ TRAINING COMPLETE", 19, 650, 920, 620, 30, "#f6c66d");
        completeText.setAlignment(Pos.CENTER);
        MenuButton next = new MenuButton("ENTER LOCATION 1", 420, 62);
        next.relocate(750, 958);
        next.setOnAction(() -> { timer.stop(); onComplete.run(); });
        completionHud.getChildren().addAll(completeCard, completeText, next);
        completionHud.setVisible(false);
        completionHud.setOpacity(0);
        completionHud.setMouseTransparent(true);

        getChildren().addAll(tutorialHints, completionHud);
        refreshHud();
    }

    private static void addControlPair(GridPane grid, int row, int column,
                                       String key, String action) {
        Label keyLabel = label(key, 13, 0, 0, 110, 21, "#fff2d1");
        Label actionLabel = label(action, 13, 0, 0, 110, 21, "#f6c66d");
        grid.add(keyLabel, column, row);
        grid.add(actionLabel, column + 1, row);
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
        events.publish("PLAYER_ATTACK","Взмах меча");
        if (!enemyAlive || Math.abs(enemyX - x) > 240) return;
        enemyHealth--;
        events.publish("WEAPON_IMPACT","Удар по ледяному элементалю",new WeaponImpact(WeaponImpact.Surface.HARD,1));
        enemyHealthHud.setHealth(enemyHealth, 3);
        if (enemyHealth <= 0) {
            enemyAlive = false; enemy.setVisible(false); snowballFlying = false; snowball.setVisible(false);
            enemyHealthHud.setVisible(false);
        }
    }

    private void action(String name) { if (!done.get(name)) { done.put(name, true); refreshHud(); } }

    private void refreshHud() {
        String current = done.get("walk") ? done.get("jump") ? done.get("crouch") ? done.get("attack") ? "Complete! The passage is open." : "Now attack (J / LEFT MOUSE)" : "Now crouch (S)" : "Now jump (W / SPACE)" : "Start by moving (A / D)";
        objective.setText("CURRENT OBJECTIVE\n" + current);
        progress.setText("MOVE " + mark("walk") + "   JUMP " + mark("jump")
                + "\nCROUCH " + mark("crouch") + "   ATTACK " + mark("attack"));
        if (done.values().stream().allMatch(Boolean::booleanValue) && !nextButtonAdded) {
            nextButtonAdded = true;
            showCompletionHud();
        }
    }

    private void showCompletionHud() {
        FadeTransition hideHints = new FadeTransition(Duration.millis(420), tutorialHints);
        hideHints.setFromValue(1);
        hideHints.setToValue(0);
        hideHints.setOnFinished(e -> tutorialHints.setVisible(false));
        hideHints.play();

        completionHud.setVisible(true);
        completionHud.setMouseTransparent(false);
        FadeTransition showComplete = new FadeTransition(Duration.millis(520), completionHud);
        showComplete.setFromValue(0);
        showComplete.setToValue(1);
        showComplete.play();
    }
    private String mark(String key) { return done.get(key) ? "✓" : "○"; }

    private void draw() {
        GraphicsContext g = world.getGraphicsContext2D();
        g.drawImage(trainingBackground, 0, 0, W, H);
        g.setFill(Color.rgb(0, 0, 0, .35)); g.fillRect(0, 0, W, 118);
    }
    private static Label label(String text, double x, double y, double w, double h, String color) { Label l = new Label(text); l.relocate(x, y); l.setPrefSize(w, h); l.setTextFill(Color.web(color)); l.setStyle("-fx-font-family: 'Monospaced'; -fx-font-weight: bold;"); return l; }
    private static Label label(String text, double size, double x, double y, double w, double h, String color) { Label l = label(text, x, y, w, h, color); l.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: " + size + "px; -fx-font-weight: bold;"); return l; }
    private static Rectangle panel(double x, double y, double w, double h) {
        Rectangle panel = new Rectangle(w, h, Color.rgb(9, 7, 15, .91));
        panel.setLayoutX(x);
        panel.setLayoutY(y);
        panel.setStroke(Color.web("#d79b4f"));
        panel.setStrokeWidth(3);
        panel.setArcWidth(12);
        panel.setArcHeight(12);
        return panel;
    }
    public void stop() { timer.stop(); audio.close(); }
}
