package com.gamelab.ui.level;

import com.gamelab.application.CampaignWaves;
import com.gamelab.application.CombatService;
import com.gamelab.application.RewardService;
import com.gamelab.application.RuneCombatService;
import com.gamelab.domain.Enemy;
import com.gamelab.domain.Item;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.WaveDefinition;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.Potion;
import com.gamelab.domain.item.PotionType;
import com.gamelab.domain.item.Weapon;
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
import com.gamelab.ui.inventory.ItemSpriteResolver;
import javafx.animation.AnimationTimer;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Playable stage one: three waves in the Forgotten Ruins. */
public final class StageOneLevelScreen extends Pane {

    public static final double W = 1920;
    public static final double H = 1080;
    /** The first visible stone row in ruin_ground.png is drawn at this world Y. */
    private static final double GROUND_Y = 842;
    private static final double GROUND_TEXTURE_TOP = 310;
    private static final double HERO_DISPLAY_HEIGHT = 180;
    private static final double HERO_DISPLAY_WIDTH = HERO_DISPLAY_HEIGHT * 64.0 / 96.0;
    /** Visible feet in the 64x96 frame are slightly above the frame bottom. */
    private static final double HERO_FOOT_ANCHOR = 174;
    private static final double HERO_CENTER_OFFSET = HERO_DISPLAY_WIDTH / 2;
    private static final double ENEMY_FOOT_CORRECTION = 3;
    private static final int LAST_WAVE = 3;
    private static final double INTER_WAVE_SECONDS = 4.0;
    private static final int QUICK_SLOT_COUNT = 5;

    private final PlayerCharacter player;
    private final Inventory inventory;
    private final CoinWallet wallet;
    private final RuneLoadout loadout;
    private final Runnable onComplete;
    private final Runnable onDefeat;
    private final Canvas world = new Canvas(W, H);
    private final Canvas projectileCanvas = new Canvas(W, H);
    private final Pane enemyLayer = new Pane();
    private final MaleWarriorSprite hero = new MaleWarriorSprite(HERO_DISPLAY_HEIGHT);
    private final HealthHud healthHud = new HealthHud();
    private final Label enemyCount = label("", 16, "#e6d7bd");
    private final Label coinLabel = label("", 18, "#ffe29b");
    private final Label message = label("", 18, "#ffe29b");
    private final Label quickStatus = label("", 13, "#f6cf7a");
    private final Pane quickBar = new Pane();
    private final Item[] quickItems = new Item[QUICK_SLOT_COUNT];
    private final Pane waveProgress = new Pane();
    private final Polygon[] waveDiamonds = new Polygon[LAST_WAVE];
    private final Pane waveTransitionOverlay = new Pane();
    private final Label transitionTitle = label("", 30, "#f6cf7a");
    private final Label transitionCountdown = label("", 21, "#f4e2bd");
    private final Pane completionOverlay = new Pane();
    private final Pane defeatOverlay = new Pane();

    private final List<Enemy> enemies = new ArrayList<>();
    private final Map<Enemy, EnemyType> enemyTypes = new LinkedHashMap<>();
    private final Map<Enemy, RuinEnemySprite> enemySprites = new LinkedHashMap<>();
    private final Map<Enemy, EnemyHealthHud> enemyHealth = new LinkedHashMap<>();
    private final Map<Enemy, Double> enemyAttackCooldowns = new LinkedHashMap<>();
    private final Map<Enemy, Boolean> guardianUsesRangedNext = new LinkedHashMap<>();
    private final List<EnemyProjectile> projectiles = new ArrayList<>();
    private final List<Rectangle2D> platforms = List.of(
            new Rectangle2D(535, 690, 220, 70),
            new Rectangle2D(1060, 565, 330, 90)
    );

    private final Image background;
    private final Image ground;
    private final Image smallPlatform;
    private final Image mediumPlatform;
    private final Image arrow;
    private final Image guardianProjectile;
    private final CombatService combat;
    private final RuneCombatService runeCombat;
    private final BalancedStrategy attackStrategy = new BalancedStrategy();
    private double heroX = 220;
    private double heroFeetY = GROUND_Y;
    private double verticalSpeed;
    private double playerAttackCooldown;
    private double quickStatusTimer;
    private double waveTransition = -1;
    private int waveNumber = 1;
    private boolean left;
    private boolean right;
    private boolean crouching;
    private boolean airborne;
    private boolean stageComplete;
    private boolean gameOver;
    private long lastFrame;

    private static final class EnemyProjectile {
        private final Enemy source;
        private final Image image;
        private final boolean guardianShot;
        private double launchDelay;
        private double x;
        private double y;
        private final double velocityX;
        private final double velocityY;

        private EnemyProjectile(
                Enemy source,
                Image image,
                boolean guardianShot,
                double launchDelay,
                double x,
                double y,
                double velocityX,
                double velocityY
        ) {
            this.source = source;
            this.image = image;
            this.guardianShot = guardianShot;
            this.launchDelay = launchDelay;
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
        }
    }

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
            Inventory inventory,
            CoinWallet wallet,
            RuneLoadout loadout,
            Runnable onComplete,
            Runnable onDefeat
    ) {
        this.player = Objects.requireNonNull(player);
        this.inventory = Objects.requireNonNull(inventory);
        this.wallet = Objects.requireNonNull(wallet);
        this.loadout = Objects.requireNonNull(loadout);
        this.onComplete = Objects.requireNonNull(onComplete);
        this.onDefeat = Objects.requireNonNull(onDefeat);

        background = loadImage("/assets/stage1/background/stage1_ruins_background_1920x1080.png");
        ground = loadImage("/assets/stage1/terrain/ruin_ground.png");
        smallPlatform = loadImage("/assets/stage1/terrain/stage1_ruins_platform_small.png");
        mediumPlatform = loadImage("/assets/stage1/terrain/stage1_ruins_platform_medium.png");
        arrow = loadImage("/assets/stage1/enemies/archer/arrow.png");
        guardianProjectile = loadImage("/assets/stage1/enemies/miniboss/rune_guardian_projectile.png");

        GameEventBus events = new GameEventBus();
        combat = new CombatService(events, new RewardService(events), wallet);
        runeCombat = new RuneCombatService(combat);

        setPrefSize(W, H);
        setMinSize(W, H);
        setMaxSize(W, H);
        setClip(new Rectangle(W, H));
        world.setMouseTransparent(true);
        projectileCanvas.setMouseTransparent(true);
        getChildren().addAll(world, enemyLayer);

        hero.setClass(player.getCharacterClass());
        hero.setConfiguration(
                player.getHairColorIndex(),
                player.getShirtColorIndex(),
                player.getPantsColorIndex(),
                player.getBootsColorIndex(),
                player.getHeadwearIndex()
        );
        getChildren().add(hero);
        // Projectiles are deliberately above actors so arrows never disappear behind a sprite.
        getChildren().add(projectileCanvas);
        buildHud();
        buildOverlays();
        spawnWave(1);

        setFocusTraversable(true);
        setOnKeyPressed(event -> handleKey(event.getCode(), true));
        setOnKeyReleased(event -> handleKey(event.getCode(), false));
        world.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) attack();
        });
        sceneProperty().addListener((observable, previous, current) -> {
            if (previous != null && current == null) stop();
        });
        requestFocus();
        draw();
        timer.start();
    }

    private void buildHud() {
        Rectangle topShade = new Rectangle(W, 112, Color.rgb(7, 5, 14, .86));
        getChildren().add(topShade);

        healthHud.relocate(32, 25);
        healthHud.setHealth(player.getHealth(), player.getMaxHealth());

        Label stageNumber = label("—  ◇  ЭТАП 1  ◇  —", 18, "#e7b95d");
        stageNumber.relocate(700, 9);
        stageNumber.setPrefSize(520, 28);
        stageNumber.setAlignment(Pos.CENTER);

        Label stageTitle = label("ЗАБЫТЫЕ РУИНЫ", 29, "#f6cf7a");
        stageTitle.relocate(670, 34);
        stageTitle.setPrefSize(580, 38);
        stageTitle.setAlignment(Pos.CENTER);

        waveProgress.relocate(908, 78);
        waveProgress.setPrefSize(104, 24);
        for (int index = 0; index < waveDiamonds.length; index++) {
            Polygon diamond = new Polygon(0, 10, 10, 0, 20, 10, 10, 20);
            diamond.relocate(index * 42, 0);
            diamond.setStroke(Color.web("#e7b95d"));
            diamond.setStrokeWidth(2);
            waveDiamonds[index] = diamond;
            waveProgress.getChildren().add(diamond);
        }

        enemyCount.relocate(1300, 35);
        enemyCount.setPrefSize(270, 30);
        enemyCount.setAlignment(Pos.CENTER_RIGHT);

        coinLabel.relocate(1590, 34);
        coinLabel.setPrefSize(270, 30);
        coinLabel.setAlignment(Pos.CENTER_RIGHT);

        message.relocate(700, 116);
        message.setPrefSize(520, 36);
        message.setAlignment(Pos.CENTER);
        message.setStyle(message.getStyle()
                + "-fx-background-color:rgba(9,7,15,.84);"
                + "-fx-border-color:#9b6d3d;"
                + "-fx-border-width:2;"
                + "-fx-padding:5 14 5 14;");

        Label quickTitle = label("БЫСТРЫЙ ДОСТУП  [ 1—5 ]", 13, "#e7b95d");
        quickTitle.relocate(42, 105);
        quickTitle.setPrefSize(390, 24);

        quickBar.relocate(40, 130);
        quickBar.setPrefSize(390, 72);

        quickStatus.relocate(42, 204);
        quickStatus.setPrefSize(390, 24);
        quickStatus.setAlignment(Pos.CENTER_LEFT);
        quickStatus.setMouseTransparent(true);
        quickStatus.setEffect(new DropShadow(5, Color.BLACK));

        refreshQuickBar();

        getChildren().addAll(
                healthHud,
                quickTitle,
                quickBar,
                quickStatus,
                stageNumber,
                stageTitle,
                waveProgress,
                enemyCount,
                coinLabel,
                message
        );
    }

    private void buildOverlays() {
        Rectangle transitionCard = card(1330, 170, 535, 190);
        transitionTitle.relocate(1365, 205);
        transitionTitle.setPrefSize(465, 44);
        transitionTitle.setAlignment(Pos.CENTER);
        transitionCountdown.relocate(1380, 288);
        transitionCountdown.setPrefSize(435, 38);
        transitionCountdown.setAlignment(Pos.CENTER);
        Rectangle divider = new Rectangle(300, 2, Color.web("#b8813f"));
        divider.relocate(1447, 269);
        Polygon ornament = new Polygon(0, 8, 8, 0, 16, 8, 8, 16);
        ornament.relocate(1589, 262);
        ornament.setFill(Color.web("#f0b84d"));
        waveTransitionOverlay.getChildren().addAll(
                transitionCard,
                transitionTitle,
                divider,
                ornament,
                transitionCountdown
        );
        waveTransitionOverlay.setMouseTransparent(true);
        waveTransitionOverlay.setVisible(false);

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

        getChildren().addAll(waveTransitionOverlay, completionOverlay, defeatOverlay);
    }

    private void update(double dt) {
        if (stageComplete || gameOver) return;

        playerAttackCooldown = Math.max(0, playerAttackCooldown - dt);
        if (quickStatusTimer > 0) {
            quickStatusTimer -= dt;
            if (quickStatusTimer <= 0) quickStatus.setText("");
        }
        boolean waitingForNextWave = waveTransition >= 0
                && waveNumber < LAST_WAVE
                && enemies.stream().noneMatch(Enemy::isAlive);
        updateHero(dt);
        if (!waitingForNextWave) {
            updateEnemies(dt);
            updateProjectiles(dt);
        }
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

        double centerX = heroX + HERO_CENTER_OFFSET;
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
        hero.relocate(heroX, heroFeetY - HERO_FOOT_ANCHOR);
    }

    private Rectangle2D platformBelow(double x, double y) {
        return platforms.stream()
                .filter(platform -> x >= platform.getMinX() && x <= platform.getMaxX())
                .filter(platform -> Math.abs(y - platform.getMinY()) < 3)
                .findFirst()
                .orElse(null);
    }

    private void updateEnemies(double dt) {
        double heroCenterX = heroX + HERO_CENTER_OFFSET;

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            enemy.update(heroCenterX, GROUND_Y, dt);
            EnemyType type = enemyTypes.get(enemy);
            RuinEnemySprite sprite = enemySprites.get(enemy);
            sprite.setWalking(true);
            sprite.setFacingLeft(enemy.getX() > heroCenterX);
            sprite.update(dt);
            double enemyTop = GROUND_Y - sprite.getActorHeight() + ENEMY_FOOT_CORRECTION;
            sprite.relocate(enemy.getX() - sprite.getActorWidth() / 2, enemyTop);

            EnemyHealthHud bar = enemyHealth.get(enemy);
            bar.relocate(enemy.getX() - 55, enemyTop - 28);

            double cooldown = enemyAttackCooldowns.getOrDefault(enemy, 0.0) - dt;
            double distance = Math.abs(enemy.getX() - heroCenterX);
            double attackRange = type == EnemyType.RUIN_ARCHER ? 560
                    : type == EnemyType.RUNE_GUARDIAN ? 620 : 125;

            if (cooldown <= 0 && distance <= attackRange) {
                if (type == EnemyType.RUIN_ARCHER) {
                    sprite.setAttacking(true);
                    launchProjectile(enemy, false);
                    cooldown = 2.15;
                } else if (type == EnemyType.RUNE_GUARDIAN) {
                    // The guardian alternates attacks. Its ranged animation is therefore
                    // guaranteed to play even if the hero stays close to the boss.
                    boolean useRanged = guardianUsesRangedNext.getOrDefault(enemy, true);
                    sprite.setAttacking(useRanged);
                    if (useRanged) {
                        launchProjectile(enemy, true);
                        cooldown = 2.55;
                    } else {
                        combat.attackPlayer(enemy, player);
                        cooldown = 1.65;
                    }
                    guardianUsesRangedNext.put(enemy, !useRanged);
                } else {
                    sprite.setAttacking(false);
                    combat.attackPlayer(enemy, player);
                    cooldown = type == EnemyType.RUNE_GUARDIAN ? 1.65 : 1.25;
                }
            }
            enemyAttackCooldowns.put(enemy, cooldown);
        }
    }

    private void launchProjectile(Enemy enemy, boolean guardianShot) {
        double targetX = heroX + HERO_CENTER_OFFSET;
        double targetY = heroFeetY - 90;
        double direction = Math.signum(targetX - enemy.getX());
        if (direction == 0) direction = -1;
        double startX = enemy.getX() + direction * (guardianShot ? 62 : 48);
        double startY = GROUND_Y - (guardianShot ? 112 : 88);

        double speed = guardianShot ? 430 : 560;
        double travelTime = Math.max(.2, Math.abs(targetX - startX) / speed);
        double velocityY = (targetY - startY) / travelTime;

        projectiles.add(new EnemyProjectile(
                enemy,
                guardianShot ? guardianProjectile : arrow,
                guardianShot,
                guardianShot ? .31 : .25,
                startX,
                startY,
                direction * speed,
                velocityY
        ));
    }

    private void updateProjectiles(double dt) {
        Iterator<EnemyProjectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            EnemyProjectile projectile = iterator.next();
            if (projectile.launchDelay > 0) {
                projectile.launchDelay -= dt;
                continue;
            }
            projectile.x += projectile.velocityX * dt;
            projectile.y += projectile.velocityY * dt;

            boolean insideHero = projectile.x >= heroX + 8
                    && projectile.x <= heroX + HERO_DISPLAY_WIDTH - 8
                    && projectile.y >= heroFeetY - 150
                    && projectile.y <= heroFeetY;

            if (insideHero) {
                combat.attackPlayer(projectile.source, player);
                iterator.remove();
            } else if (projectile.x < -100 || projectile.x > W + 100
                    || projectile.y < 80 || projectile.y > H + 100
                    || !projectile.source.isAlive()) {
                iterator.remove();
            }
        }
    }

    private void updateWave(double dt) {
        long living = enemies.stream().filter(Enemy::isAlive).count();
        enemyCount.setText("ВРАГОВ: " + living);

        if (living == 0 && waveTransition < 0) {
            projectiles.clear();
            if (waveNumber < LAST_WAVE) {
                waveTransition = INTER_WAVE_SECONDS;
                showWaveTransition(waveNumber, waveNumber + 1);
                message.setText("ВОЛНА " + waveNumber + " ОЧИЩЕНА");
            } else {
                waveTransition = 1.4;
                message.setText("ХРАНИТЕЛЬ РУН ПОБЕЖДЁН");
            }
        }

        if (waveTransition >= 0) {
            if (waveNumber < LAST_WAVE && waveTransitionOverlay.isVisible()) {
                int seconds = Math.max(1, (int) Math.ceil(waveTransition));
                transitionCountdown.setText("ВОЛНА " + (waveNumber + 1) + " ЧЕРЕЗ " + seconds);
            }
            waveTransition -= dt;
            if (waveTransition <= 0) {
                if (waveNumber < LAST_WAVE) {
                    waveTransitionOverlay.setVisible(false);
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

        updateWaveDiamonds(number);
        message.setText(number == LAST_WAVE
                ? "ФИНАЛЬНАЯ ВОЛНА — ХРАНИТЕЛЬ РУН"
                : "ВОЛНА " + number + " НАЧАЛАСЬ");
        waveTransition = -1;
    }

    private void updateWaveDiamonds(int currentWave) {
        for (int index = 0; index < waveDiamonds.length; index++) {
            boolean filled = index < currentWave;
            waveDiamonds[index].setFill(filled
                    ? Color.web("#f0b84d")
                    : Color.TRANSPARENT);
        }
    }

    private void showWaveTransition(int completedWave, int nextWave) {
        transitionTitle.setText("ВОЛНА " + completedWave + " ЗАВЕРШЕНА");
        transitionCountdown.setText("ВОЛНА " + nextWave + " ЧЕРЕЗ 4");
        waveTransitionOverlay.setVisible(true);
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
        if (stageComplete || gameOver || waveTransition >= 0 || playerAttackCooldown > 0) return;
        playerAttackCooldown = .38 / Math.max(.35, loadout.createDecoratedWeapon().getAttackSpeed());
        hero.setAttacking();

        double heroCenterX = heroX + HERO_CENTER_OFFSET;
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
        enemySprites.get(target).setHurt();

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
        if (pressed) {
            int quickSlot = quickSlotIndex(code);
            if (quickSlot >= 0) activateQuickSlot(quickSlot);
        }
        if (pressed && code == KeyCode.ESCAPE) {
            if (!player.isAlive()) player.heal(player.getMaxHealth());
            stop();
            onDefeat.run();
        }
    }

    private void refreshQuickBar() {
        quickBar.getChildren().clear();
        java.util.Arrays.fill(quickItems, null);

        List<Item> inventoryItems = inventory.getItems();
        List<Item> usableItems = new ArrayList<>();
        Weapon equipped = inventory.getEquippedWeapon().orElse(null);
        if (equipped != null) usableItems.add(equipped);

        // Keep room both for weapon switching and healing: current weapon, up to two
        // strongest alternatives, then separate potion objects. Remaining places are
        // filled by the next available weapons.
        List<Weapon> alternativeWeapons = inventoryItems.stream()
                .filter(Weapon.class::isInstance)
                .map(Weapon.class::cast)
                .filter(weapon -> weapon != equipped)
                .sorted(java.util.Comparator.comparingInt(Weapon::getUpgradeLevel).reversed())
                .toList();
        alternativeWeapons.stream().limit(2).forEach(usableItems::add);
        inventoryItems.stream()
                .filter(Potion.class::isInstance)
                .limit(QUICK_SLOT_COUNT - usableItems.size())
                .forEach(usableItems::add);
        alternativeWeapons.stream()
                .skip(2)
                .limit(QUICK_SLOT_COUNT - usableItems.size())
                .forEach(usableItems::add);

        for (int index = 0; index < QUICK_SLOT_COUNT; index++) {
            Item item = index < usableItems.size() ? usableItems.get(index) : null;
            quickItems[index] = item;
            quickBar.getChildren().add(createQuickSlot(index, item));
        }
    }

    private Pane createQuickSlot(int index, Item item) {
        Pane slot = new Pane();
        slot.relocate(index * 78, 0);
        slot.setPrefSize(70, 70);
        slot.setCursor(Cursor.HAND);

        boolean equipped = item instanceof Weapon weapon
                && inventory.getEquippedWeapon().map(current -> current == weapon).orElse(false);
        Rectangle frame = new Rectangle(70, 70, Color.rgb(12, 9, 20, .94));
        frame.setArcWidth(9);
        frame.setArcHeight(9);
        frame.setStroke(Color.web(equipped ? "#ffd267" : "#a4743d"));
        frame.setStrokeWidth(equipped ? 3 : 1.6);
        if (equipped) frame.setEffect(new DropShadow(11, Color.web("#ffc34f")));

        Label key = label(Integer.toString(index + 1), 13, equipped ? "#fff0ac" : "#cba868");
        key.relocate(7, 3);
        key.setPrefSize(18, 18);

        slot.getChildren().addAll(frame, key);
        if (item == null) {
            Label empty = label("◇", 21, "#4f405c");
            empty.relocate(23, 25);
            empty.setPrefSize(24, 25);
            empty.setAlignment(Pos.CENTER);
            slot.getChildren().add(empty);
        } else {
            Node icon = quickItemIcon(item);
            icon.relocate(15, 19);
            slot.getChildren().add(icon);
        }

        slot.setOnMouseClicked(event -> {
            activateQuickSlot(index);
            requestFocus();
            event.consume();
        });
        return slot;
    }

    private Node quickItemIcon(Item item) {
        try {
            URL resource = getClass().getResource(ItemSpriteResolver.resolve(item));
            if (resource != null) {
                ImageView icon = new ImageView(new Image(resource.toExternalForm()));
                icon.setFitWidth(42);
                icon.setFitHeight(42);
                icon.setPreserveRatio(true);
                icon.setSmooth(false);
                return icon;
            }
        } catch (RuntimeException ignored) {
            // The slot remains usable even while an optional item image is missing.
        }
        Label fallback = label("✦", 26, "#d3ad63");
        fallback.setPrefSize(42, 42);
        fallback.setAlignment(Pos.CENTER);
        return fallback;
    }

    private void activateQuickSlot(int index) {
        if (stageComplete || gameOver || index < 0 || index >= quickItems.length) return;
        Item item = quickItems[index];
        if (item == null) {
            showQuickStatus("ЯЧЕЙКА " + (index + 1) + " ПУСТА");
            return;
        }

        if (item instanceof Potion potion) {
            usePotion(potion);
        } else if (item instanceof Weapon weapon) {
            equipWeapon(weapon);
        }
    }

    private void usePotion(Potion potion) {
        if (potion.getPotionType() != PotionType.HEALTH) {
            showQuickStatus("ЭТО ЗЕЛЬЕ ПОКА НЕЛЬЗЯ ИСПОЛЬЗОВАТЬ");
            return;
        }
        if (player.getHealth() >= player.getMaxHealth()) {
            showQuickStatus("ЗДОРОВЬЕ УЖЕ ПОЛНОЕ");
            return;
        }

        int before = player.getHealth();
        player.heal(potion.getEffectAmount());
        inventory.removeItem(potion);
        healthHud.setHealth(player.getHealth(), player.getMaxHealth());
        showQuickStatus("ЗЕЛЬЕ: +" + (player.getHealth() - before) + " ЗДОРОВЬЯ");
        refreshQuickBar();
    }

    private void equipWeapon(Weapon weapon) {
        if (inventory.getEquippedWeapon().map(current -> current == weapon).orElse(false)) {
            showQuickStatus("ЭТОТ МЕЧ УЖЕ ЭКИПИРОВАН");
            return;
        }
        if (loadout.getInstalledRunes().size() > weapon.getRuneSlotCount()) {
            showQuickStatus("В НОВОМ МЕЧЕ НЕДОСТАТОЧНО СЛОТОВ РУН");
            return;
        }

        try {
            inventory.equipWeapon(weapon, player.getCharacterClass());
            loadout.changeWeapon(weapon);
            showQuickStatus("ЭКИПИРОВАН: " + weapon.getName().toUpperCase());
            refreshQuickBar();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showQuickStatus("НЕЛЬЗЯ ЭКИПИРОВАТЬ ЭТОТ МЕЧ");
        }
    }

    private void showQuickStatus(String text) {
        quickStatus.setText(text);
        quickStatusTimer = 2.2;
    }

    private static int quickSlotIndex(KeyCode code) {
        return switch (code) {
            case DIGIT1, NUMPAD1 -> 0;
            case DIGIT2, NUMPAD2 -> 1;
            case DIGIT3, NUMPAD3 -> 2;
            case DIGIT4, NUMPAD4 -> 3;
            case DIGIT5, NUMPAD5 -> 4;
            default -> -1;
        };
    }

    private void removeEnemyVisuals() {
        enemyLayer.getChildren().clear();
        enemies.clear();
        enemyTypes.clear();
        enemySprites.clear();
        enemyHealth.clear();
        enemyAttackCooldowns.clear();
        guardianUsesRangedNext.clear();
        projectiles.clear();
    }

    private void draw() {
        GraphicsContext g = world.getGraphicsContext2D();
        g.setImageSmoothing(false);
        g.drawImage(background, 0, 0, W, H);
        g.setFill(Color.rgb(8, 6, 14, .16));
        g.fillRect(0, 112, W, H - 112);

        double groundSourceY = GROUND_TEXTURE_TOP;
        g.drawImage(
                ground,
                0,
                groundSourceY,
                ground.getWidth(),
                ground.getHeight() - groundSourceY,
                0,
                GROUND_Y,
                W,
                H - GROUND_Y
        );

        for (int index = 0; index < platforms.size(); index++) {
            Rectangle2D platform = platforms.get(index);
            Image texture = index == 0 ? smallPlatform : mediumPlatform;
            g.drawImage(
                    texture,
                    platform.getMinX(),
                    platform.getMinY(),
                    platform.getWidth(),
                    platform.getHeight()
            );
        }

        drawProjectiles();
    }

    private void drawProjectiles() {
        GraphicsContext graphics = projectileCanvas.getGraphicsContext2D();
        graphics.clearRect(0, 0, W, H);
        graphics.setImageSmoothing(false);
        for (EnemyProjectile projectile : projectiles) {
            if (projectile.launchDelay > 0) continue;
            double height = projectile.guardianShot ? 22 : 12;
            double width = height * projectile.image.getWidth() / projectile.image.getHeight();
            graphics.save();
            graphics.translate(projectile.x, projectile.y);
            graphics.scale(projectile.velocityX < 0 ? 1 : -1, 1);
            graphics.drawImage(projectile.image, -width / 2, -height / 2, width, height);
            graphics.restore();
        }
    }

    private static Image loadImage(String path) {
        URL resource = StageOneLevelScreen.class.getResource(path);
        if (resource == null) throw new IllegalStateException("Missing stage-one asset: " + path);
        return new Image(resource.toExternalForm());
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
