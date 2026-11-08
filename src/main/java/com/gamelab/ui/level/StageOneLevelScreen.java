package com.gamelab.ui.level;

import com.gamelab.application.CampaignWaves;
import com.gamelab.application.CombatService;
import com.gamelab.application.RewardService;
import com.gamelab.application.RuneCombatService;
import com.gamelab.domain.Enemy;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.WaveDefinition;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.rune.RuneLoadout;
import com.gamelab.pattern.factory.EnemyFactory;
import com.gamelab.pattern.factory.EnemyType;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.strategy.BalancedStrategy;
import com.gamelab.ui.components.EnemyHealthHud;
import com.gamelab.ui.components.HealthHud;
import com.gamelab.ui.components.MaleWarriorSprite;
import com.gamelab.ui.components.MenuButton;
import com.gamelab.ui.components.RuinEnemySprite;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Playable stage one: three waves in the Forgotten Ruins. */
public final class StageOneLevelScreen extends Pane {

    public static final double W = 1920;
    public static final double H = 1080;
    private static final double GROUND_Y = 825;
    private static final double HERO_HEIGHT = 185;
    private static final int LAST_WAVE = 3;

    private final PlayerCharacter player;
    private final CoinWallet wallet;
    private final RuneLoadout loadout;
    private final Runnable onComplete;
    private final Runnable onDefeat;
    private final Canvas world = new Canvas(W, H);
    private final Pane enemyLayer = new Pane();
    private final MaleWarriorSprite hero = new MaleWarriorSprite(210);
    private final HealthHud healthHud = new HealthHud();
    private final Label waveLabel = label("", 22, "#f6cf7a");
    private final Label enemyCount = label("", 16, "#e6d7bd");
    private final Label coinLabel = label("", 18, "#ffe29b");
    private final Label message = label("", 18, "#ffe29b");
    private final Pane completionOverlay = new Pane();
    private final Pane defeatOverlay = new Pane();

    private final List<Enemy> enemies = new ArrayList<>();
    private final Map<Enemy, EnemyType> enemyTypes = new LinkedHashMap<>();
    private final Map<Enemy, RuinEnemySprite> enemySprites = new LinkedHashMap<>();
    private final Map<Enemy, EnemyHealthHud> enemyHealth = new LinkedHashMap<>();
    private final Map<Enemy, Double> enemyAttackCooldowns = new LinkedHashMap<>();
    private final List<Rectangle2D> platforms = List.of(
            new Rectangle2D(665, 690, 270, 34),
            new Rectangle2D(1085, 560, 360, 34)
    );

    private final Image background;
    private final RuneCombatService runeCombat;
    private final BalancedStrategy attackStrategy = new BalancedStrategy();
    private double heroX = 220;
    private double heroFeetY = GROUND_Y;
    private double verticalSpeed;
    private double playerAttackCooldown;
    private double waveTransition = -1;
    private int waveNumber = 1;
    private boolean left;
    private boolean right;
    private boolean crouching;
    private boolean airborne;
    private boolean stageComplete;
    private boolean gameOver;
    private long lastFrame;

    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            double dt = lastFrame == 0 ? .016 : Math.min(.05, (now - lastFrame) / 1e9);
            lastFrame = now;
            update(dt);
            draw();
        }
    };

    public StageOneLevelScreen(
            PlayerCharacter player,
            CoinWallet wallet,
            RuneLoadout loadout,
            Runnable onComplete,
            Runnable onDefeat
    ) {
        this.player = Objects.requireNonNull(player);
        this.wallet = Objects.requireNonNull(wallet);
        this.loadout = Objects.requireNonNull(loadout);
        this.onComplete = Objects.requireNonNull(onComplete);
        this.onDefeat = Objects.requireNonNull(onDefeat);

        URL backgroundUrl = StageOneLevelScreen.class.getResource(
                "/assets/references/moonlit_castle_background.jpg"
        );
        if (backgroundUrl == null) {
            throw new IllegalStateException("Missing temporary stage-one background");
        }
        background = new Image(backgroundUrl.toExternalForm());

        GameEventBus events = new GameEventBus();
        CombatService combat = new CombatService(events, new RewardService(events), wallet);
        runeCombat = new RuneCombatService(combat);

        setPrefSize(W, H);
        setMinSize(W, H);
        setMaxSize(W, H);
        setClip(new Rectangle(W, H));
        getChildren().addAll(world, enemyLayer);

        hero.setConfiguration(
                player.getHairColorIndex(),
                player.getOutfitIndex(),
                player.getHeadwearIndex()
        );
        getChildren().add(hero);
        buildHud();
        buildOverlays();
        spawnWave(1);

        setFocusTraversable(true);
        setOnKeyPressed(event -> handleKey(event.getCode(), true));
        setOnKeyReleased(event -> handleKey(event.getCode(), false));
        world.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) attack();
        });
        requestFocus();
        timer.start();
    }

    private void buildHud() {
        Rectangle topShade = new Rectangle(W, 112, Color.rgb(7, 5, 14, .86));
        getChildren().add(topShade);

        healthHud.relocate(32, 25);
        healthHud.setHealth(player.getHealth(), player.getMaxHealth());

        Label stageTitle = label("ЭТАП 1  //  ЗАБЫТЫЕ РУИНЫ", 28, "#f6cf7a");
        stageTitle.relocate(670, 22);
        stageTitle.setPrefSize(620, 40);
        stageTitle.setAlignment(Pos.CENTER);

        waveLabel.relocate(735, 64);
        waveLabel.setPrefSize(490, 30);
        waveLabel.setAlignment(Pos.CENTER);

        enemyCount.relocate(1300, 35);
        enemyCount.setPrefSize(270, 30);
        enemyCount.setAlignment(Pos.CENTER_RIGHT);

        coinLabel.relocate(1590, 34);
        coinLabel.setPrefSize(270, 30);
        coinLabel.setAlignment(Pos.CENTER_RIGHT);

        message.relocate(610, 930);
        message.setPrefSize(700, 42);
        message.setAlignment(Pos.CENTER);
        message.setStyle(message.getStyle()
                + "-fx-background-color:rgba(9,7,15,.84);"
                + "-fx-border-color:#9b6d3d;"
                + "-fx-border-width:2;"
                + "-fx-padding:7 16 7 16;");

        getChildren().addAll(healthHud, stageTitle, waveLabel, enemyCount, coinLabel, message);
    }

    private void buildOverlays() {
        Rectangle completeCard = card(570, 360, 780, 300);
        Label completeTitle = label("ЭТАП 1 ЗАВЕРШЁН", 32, "#f6cf7a");
        completeTitle.relocate(650, 405);
        completeTitle.setPrefSize(620, 50);
        completeTitle.setAlignment(Pos.CENTER);
        Label completeText = label("Забытые руины очищены\nНаграды получены", 18, "#e7d9c0");
        completeText.relocate(650, 475);
        completeText.setPrefSize(620, 70);
        completeText.setAlignment(Pos.CENTER);
        MenuButton continueButton = new MenuButton("ПЕРЕЙТИ НА РЫНОК", 420, 62);
        continueButton.relocate(750, 570);
        continueButton.setOnAction(() -> {
            stop();
            onComplete.run();
        });
        completionOverlay.getChildren().addAll(completeCard, completeTitle, completeText, continueButton);
        completionOverlay.setVisible(false);

        Rectangle defeatCard = card(570, 360, 780, 300);
        Label defeatTitle = label("ГЕРОЙ ПАЛ", 32, "#ef8d81");
        defeatTitle.relocate(650, 415);
        defeatTitle.setPrefSize(620, 50);
        defeatTitle.setAlignment(Pos.CENTER);
        Label defeatText = label("Вернись на рынок и подготовься лучше", 17, "#e7d9c0");
        defeatText.relocate(650, 490);
        defeatText.setPrefSize(620, 40);
        defeatText.setAlignment(Pos.CENTER);
        MenuButton returnButton = new MenuButton("ВЕРНУТЬСЯ НА РЫНОК", 420, 62);
        returnButton.relocate(750, 570);
        returnButton.setOnAction(() -> {
            player.heal(player.getMaxHealth());
            stop();
            onDefeat.run();
        });
        defeatOverlay.getChildren().addAll(defeatCard, defeatTitle, defeatText, returnButton);
        defeatOverlay.setVisible(false);

        getChildren().addAll(completionOverlay, defeatOverlay);
    }

    private void update(double dt) {
        if (stageComplete || gameOver) return;

        playerAttackCooldown = Math.max(0, playerAttackCooldown - dt);
        updateHero(dt);
        updateEnemies(dt);
        updateWave(dt);
        healthHud.setHealth(player.getHealth(), player.getMaxHealth());
        coinLabel.setText(wallet.getBalance() + " МОНЕТ");

        if (!player.isAlive()) {
            gameOver = true;
            defeatOverlay.setVisible(true);
        }
    }

    private void updateHero(double dt) {
        boolean walking = left ^ right;
        if (left) heroX -= 270 * dt;
        if (right) heroX += 270 * dt;
        heroX = Math.max(35, Math.min(1680, heroX));

        double centerX = heroX + 90;
        if (!airborne && heroFeetY < GROUND_Y && platformBelow(centerX, heroFeetY) == null) {
            airborne = true;
            verticalSpeed = 0;
        }

        if (airborne) {
            double previousY = heroFeetY;
            verticalSpeed += 1080 * dt;
            double nextY = heroFeetY + verticalSpeed * dt;
            double landingY = GROUND_Y;

            if (verticalSpeed >= 0) {
                for (Rectangle2D platform : platforms) {
                    if (centerX >= platform.getMinX()
                            && centerX <= platform.getMaxX()
                            && previousY <= platform.getMinY()
                            && nextY >= platform.getMinY()) {
                        landingY = Math.min(landingY, platform.getMinY());
                    }
                }
            }

            heroFeetY = nextY;
            if (heroFeetY >= landingY) {
                heroFeetY = landingY;
                verticalSpeed = 0;
                airborne = false;
            }
        }

        if (left && !right) hero.setFacingLeft(true);
        if (right && !left) hero.setFacingLeft(false);
        hero.setWalking(walking, crouching);
        hero.update(dt);
        hero.relocate(heroX, heroFeetY - HERO_HEIGHT);
    }

    private Rectangle2D platformBelow(double x, double y) {
        return platforms.stream()
                .filter(platform -> x >= platform.getMinX() && x <= platform.getMaxX())
                .filter(platform -> Math.abs(y - platform.getMinY()) < 3)
                .findFirst()
                .orElse(null);
    }

    private void updateEnemies(double dt) {
        double heroCenterX = heroX + 90;

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            enemy.update(heroCenterX, GROUND_Y, dt);
            EnemyType type = enemyTypes.get(enemy);
            RuinEnemySprite sprite = enemySprites.get(enemy);
            sprite.setWalking(true);
            sprite.setFacingLeft(enemy.getX() > heroCenterX);
            sprite.update(dt);
            sprite.relocate(
                    enemy.getX() - sprite.getActorWidth() / 2,
                    GROUND_Y - sprite.getActorHeight()
            );

            EnemyHealthHud bar = enemyHealth.get(enemy);
            bar.relocate(enemy.getX() - 55, GROUND_Y - sprite.getActorHeight() - 28);

            double cooldown = enemyAttackCooldowns.getOrDefault(enemy, 0.0) - dt;
            double distance = Math.abs(enemy.getX() - heroCenterX);
            double attackRange = type == EnemyType.RUIN_ARCHER ? 470
                    : type == EnemyType.RUNE_GUARDIAN ? 175 : 115;

            if (cooldown <= 0 && distance <= attackRange) {
                sprite.setAttacking();
                new CombatService(new GameEventBus()).attackPlayer(enemy, player);
                cooldown = type == EnemyType.RUIN_ARCHER ? 2.15 : 1.35;
            }
            enemyAttackCooldowns.put(enemy, cooldown);
        }
    }

    private void updateWave(double dt) {
        long living = enemies.stream().filter(Enemy::isAlive).count();
        enemyCount.setText("ВРАГОВ: " + living);

        if (living == 0 && waveTransition < 0) {
            waveTransition = 1.6;
            message.setText(waveNumber < LAST_WAVE
                    ? "ВОЛНА ОЧИЩЕНА — ПРИГОТОВЬСЯ"
                    : "ХРАНИТЕЛЬ РУН ПОБЕЖДЁН");
        }

        if (waveTransition >= 0) {
            waveTransition -= dt;
            if (waveTransition <= 0) {
                if (waveNumber < LAST_WAVE) {
                    spawnWave(++waveNumber);
                } else {
                    stageComplete = true;
                    completionOverlay.setVisible(true);
                }
            }
        }
    }

    private void spawnWave(int number) {
        removeEnemyVisuals();
        WaveDefinition wave = CampaignWaves.get(number);
        int enemyIndex = 0;

        for (Map.Entry<EnemyType, Integer> entry : wave.getComposition().entrySet()) {
            for (int count = 0; count < entry.getValue(); count++) {
                EnemyType type = entry.getKey();
                double spawnX = 1030 + enemyIndex * 140;
                Enemy enemy = EnemyFactory.create(
                        type,
                        "stage1-wave" + number + "-enemy" + enemyIndex,
                        spawnX,
                        GROUND_Y
                );
                addEnemy(enemy, type);
                enemyIndex++;
            }
        }

        waveLabel.setText("ВОЛНА " + number + " / " + LAST_WAVE);
        message.setText(number == LAST_WAVE
                ? "ФИНАЛЬНАЯ ВОЛНА — ХРАНИТЕЛЬ РУН"
                : "ВОЛНА " + number + " НАЧАЛАСЬ");
        waveTransition = -1;
    }

    private void addEnemy(Enemy enemy, EnemyType type) {
        RuinEnemySprite.Kind kind = switch (type) {
            case RUIN_ARCHER -> RuinEnemySprite.Kind.ARCHER;
            case RUNE_GUARDIAN -> RuinEnemySprite.Kind.GUARDIAN;
            default -> RuinEnemySprite.Kind.SOLDIER;
        };
        RuinEnemySprite sprite = new RuinEnemySprite(kind);
        EnemyHealthHud bar = new EnemyHealthHud(
                enemy.getId(),
                enemy.getHealth(),
                enemy.getMaxHealth()
        );

        enemies.add(enemy);
        enemyTypes.put(enemy, type);
        enemySprites.put(enemy, sprite);
        enemyHealth.put(enemy, bar);
        enemyAttackCooldowns.put(enemy, .8 + Math.random());
        enemyLayer.getChildren().addAll(sprite, bar);
    }

    private void attack() {
        if (stageComplete || gameOver || playerAttackCooldown > 0) return;
        playerAttackCooldown = .38;
        hero.setAttacking();

        double heroCenterX = heroX + 90;
        Enemy target = enemies.stream()
                .filter(Enemy::isAlive)
                .filter(enemy -> Math.abs(enemy.getX() - heroCenterX) <= 205)
                .filter(enemy -> Math.abs(GROUND_Y - heroFeetY) <= 180)
                .min(java.util.Comparator.comparingDouble(enemy -> Math.abs(enemy.getX() - heroCenterX)))
                .orElse(null);

        if (target == null) return;
        runeCombat.attack(player, target, attackStrategy, loadout);
        EnemyHealthHud bar = enemyHealth.get(target);
        bar.setHealth(target.getHealth(), target.getMaxHealth());

        if (!target.isAlive()) {
            enemySprites.get(target).setVisible(false);
            bar.setVisible(false);
            coinLabel.setText(wallet.getBalance() + " МОНЕТ");
        }
    }

    private void handleKey(KeyCode code, boolean pressed) {
        if (code == KeyCode.A || code == KeyCode.LEFT) left = pressed;
        if (code == KeyCode.D || code == KeyCode.RIGHT) right = pressed;
        if (code == KeyCode.S || code == KeyCode.DOWN) crouching = pressed;

        if (pressed && (code == KeyCode.W || code == KeyCode.UP || code == KeyCode.SPACE)
                && !airborne && !crouching) {
            airborne = true;
            verticalSpeed = -590;
        }
        if (pressed && code == KeyCode.J) attack();
        if (pressed && code == KeyCode.ESCAPE) {
            if (!player.isAlive()) player.heal(player.getMaxHealth());
            stop();
            onDefeat.run();
        }
    }

    private void removeEnemyVisuals() {
        enemyLayer.getChildren().clear();
        enemies.clear();
        enemyTypes.clear();
        enemySprites.clear();
        enemyHealth.clear();
        enemyAttackCooldowns.clear();
    }

    private void draw() {
        GraphicsContext g = world.getGraphicsContext2D();
        g.setImageSmoothing(false);
        g.drawImage(background, 0, 0, W, H);
        g.setFill(Color.rgb(8, 6, 14, .26));
        g.fillRect(0, 112, W, H - 112);

        g.setFill(Color.web("#26202e"));
        g.fillRect(0, GROUND_Y, W, H - GROUND_Y);
        g.setFill(Color.web("#564458"));
        g.fillRect(0, GROUND_Y, W, 12);
        g.setFill(Color.web("#17131d"));
        for (int x = 0; x < W; x += 64) {
            g.fillRect(x, GROUND_Y + 32 + ((x / 64) % 3) * 28, 46, 14);
        }

        for (Rectangle2D platform : platforms) {
            g.setFill(Color.web("#55475c"));
            g.fillRect(platform.getMinX(), platform.getMinY(), platform.getWidth(), platform.getHeight());
            g.setFill(Color.web("#8b6a58"));
            g.fillRect(platform.getMinX(), platform.getMinY(), platform.getWidth(), 8);
            g.setFill(Color.web("#211a28"));
            for (double x = platform.getMinX() + 20; x < platform.getMaxX(); x += 52) {
                g.fillRect(x, platform.getMinY() + 17, 30, 8);
            }
        }
    }

    private static Rectangle card(double x, double y, double width, double height) {
        Rectangle card = new Rectangle(width, height, Color.rgb(9, 7, 15, .96));
        card.relocate(x, y);
        card.setStroke(Color.web("#d79b4f"));
        card.setStrokeWidth(3);
        card.setArcWidth(14);
        card.setArcHeight(14);
        card.setEffect(new DropShadow(30, Color.rgb(0, 0, 0, .8)));
        return card;
    }

    private static Label label(String value, double size, String color) {
        Label label = new Label(value);
        label.setTextFill(Color.web(color));
        label.setStyle(
                "-fx-font-family:'Monospaced';"
                        + "-fx-font-size:" + size + "px;"
                        + "-fx-font-weight:bold;"
        );
        return label;
    }

    public void stop() {
        timer.stop();
    }
}
