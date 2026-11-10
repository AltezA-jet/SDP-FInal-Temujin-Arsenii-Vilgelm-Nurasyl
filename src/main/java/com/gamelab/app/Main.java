package com.gamelab.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.progression.Difficulty;
import com.gamelab.application.RuneService;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.Weapon;
import com.gamelab.domain.item.WeaponCatalog;
import com.gamelab.domain.rune.RuneLoadout;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.ui.character.CharacterCreationScreen;
import com.gamelab.ui.menu.MenuScreen;
import com.gamelab.ui.menu.GameMenuScreen;
import com.gamelab.ui.level.TutorialLevelScreen;
import com.gamelab.application.arena.CampaignRun;
import com.gamelab.ui.inventory.InventoryScreen;
import com.gamelab.ui.level.EnemyArenaScreen;
import com.gamelab.ui.shop.StageOneMarketScreen;
import com.gamelab.ui.audio.CombatAudio;
import com.gamelab.ui.audio.JavaSoundPlayer;


/**
 * Game Lab: Shadowfall - application entry point.
 * The menu is designed for 1920x1080 and scaled uniformly to any window size (black bars if the
 * aspect ratio differs). F11 toggles fullscreen.
 */
public class Main extends Application {

    private static final String TITLE = "Game Lab: Shadowfall";
    private Stage stage;
    private MenuScreen menu;
    private EnemyArenaScreen enemyArena;
    private PlayerCharacter campaignPlayer;
    private Inventory campaignInventory;
    private CoinWallet campaignWallet;
    private RuneLoadout campaignRuneLoadout;
    private RuneService campaignRuneService;
    private GameMenuScreen gameMenu;
    private CampaignRun campaignRun;
    private EnemyArenaScreen campaignArena;
    private CombatAudio campaignAudio;
    private boolean tutorialCompleted;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        menu = new MenuScreen();
        menu.setOnNewGame(this::onNewGame);
        menu.setOnContinue(this::onContinue);
        menu.setOnArena(this::openEnemyArena);
        menu.setOnSettings(this::onSettings);
        menu.setOnExit(() -> {
            menu.stop();
            Platform.exit();
        });
        menu.setContinueEnabled(false);

        // 1:1 (1920x1080) when the screen is big enough, otherwise the largest 16:9 window that fits
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double s = Math.min(1.0, Math.min(screen.getWidth() / MenuScreen.W, (screen.getHeight() - 40) / MenuScreen.H));
        Scene scene = new Scene(new Pane(), MenuScreen.W * s, MenuScreen.H * s, Color.BLACK);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
                e.consume();
            } else if (e.getCode() == KeyCode.F6 && menu.getScene() == stage.getScene()) {
                openEnemyArena();
                e.consume();
            } else {
                if (menu != null && menu.getScene() == stage.getScene()) menu.handleKey(e.getCode());
            }
        });

        stage.setTitle(TITLE);
        stage.setScene(scene);
        showFixedScreen(menu);
        if (Boolean.getBoolean("shadowfall.debug.finalBoss")) Platform.runLater(this::openFinalBossDebug);
        stage.setMinWidth(960);
        stage.setMinHeight(540);
        stage.setFullScreenExitHint("F11 - toggle fullscreen");
        stage.setOnHidden(e -> { menu.stop(); if (enemyArena != null) enemyArena.stop(); if(campaignArena!=null) campaignArena.stop(); if(campaignAudio!=null) campaignAudio.close(); });
        stage.show();


    }

    private void onNewGame() {
        menu.stop();
        showFixedScreen(new CharacterCreationScreen((player, difficulty) -> onCharacterCreated(player, difficulty), this::returnToMenu));
    }

    private void onCharacterCreated(PlayerCharacter player, Difficulty difficulty) {
        if(campaignArena!=null) { campaignArena.stop(); campaignArena=null; }
        if(campaignAudio!=null) campaignAudio.close();
        tutorialCompleted=false;
        campaignPlayer = player;
        campaignInventory = new Inventory(16);
        campaignWallet = new CoinWallet();
        Weapon starterWeapon = WeaponCatalog.createStarterWeapon(player.getCharacterClass());
        campaignInventory.addItem(starterWeapon);
        campaignInventory.equipWeapon(starterWeapon, player.getCharacterClass());
        campaignRuneLoadout = new RuneLoadout(starterWeapon);
        campaignRuneService = new RuneService(new GameEventBus());
        campaignRun=new CampaignRun(player,campaignInventory,campaignWallet,campaignRuneLoadout,difficulty);
        campaignAudio=new CombatAudio(campaignRun.events(),new JavaSoundPlayer());
        showFixedScreen(new TutorialLevelScreen(player, this::onTutorialCompleted));
    }

    private void onTutorialCompleted() {
        if(!tutorialCompleted) campaignWallet.addCoins(180);
        tutorialCompleted=true;
        showCampaignMarket(0);
    }

    private void showInventory(Inventory inventory) {
        showFixedScreen(new InventoryScreen(
                inventory,
                campaignRuneLoadout,
                campaignRuneService,
                () -> showCampaignHub(this::resumeCampaign),
                campaignPlayer.getCharacterClass(),
                this::startNextCampaignStage
        ));
    }

    private void onCampaignStageCompleted() {
        if(!campaignRun.completeStage()) return;
        if(campaignArena!=null) { campaignArena.stop();campaignArena=null; }
        if(campaignRun.finished()) showCampaignVictory(); else showCampaignMarket(campaignRun.completedStage());
    }

    private void showCampaignMarket(int completedStage) {
        if (campaignPlayer == null || campaignInventory == null || campaignWallet == null) return;
        Runnable continueAction = this::startNextCampaignStage;
        showFixedScreen(new StageOneMarketScreen(
                campaignPlayer,
                campaignInventory,
                campaignWallet,
                completedStage,
                this::returnToMenu,
                continueAction,
                () -> showInventory(campaignInventory),
                () -> showCampaignHub(this::resumeCampaign)
        ));
    }

    private void startNextCampaignStage() {
        if (campaignPlayer == null || campaignInventory == null
                || campaignWallet == null || campaignRuneLoadout == null) return;
        campaignRun.enterNextStage();
        campaignArena=new EnemyArenaScreen(campaignRun,() -> showCampaignHub(this::resumeCampaign),campaignAudio);
        campaignArena.setOnStageCompleted(number -> onCampaignStageCompleted());
        campaignArena.setUsePotion(campaignRun::useHealthPotion);
        campaignArena.setPotionCount(campaignRun::healthPotionCount);
        showFixedScreen(campaignArena);
    }

    private void resumeCampaign() {
        if(campaignRun==null) return;
        campaignRun.syncEquipment();campaignRun.claimPendingRunes();
        if(campaignRun.finished()) { showCampaignVictory();return; }
        if(campaignArena!=null && campaignRun.activeStage()>campaignRun.completedStage()) {
            showFixedScreen(campaignArena);campaignArena.resume();
        } else showCampaignMarket(campaignRun.completedStage());
    }

    private void showCampaignHub(Runnable resume) {
        gameMenu=new GameMenuScreen(campaignPlayer,this::returnToMenu,campaignInventory,this::showInventory,campaignWallet);
        gameMenu.setShopAvailable(campaignArena==null);
        gameMenu.setOnShop(inventory -> showCampaignMarket(campaignRun.completedStage()));
        gameMenu.setOnContinue(() -> {
            if (campaignArena != null && campaignRun.activeStage() > campaignRun.completedStage()) resume.run();
            else startNextCampaignStage();
        });
        Button menuButton=new Button("В главное меню"); menuButton.relocate(1350,1010);
        menuButton.setStyle("-fx-font-size:20px;");menuButton.setOnAction(e -> returnToMenu());
        showFixedScreen(gameMenu);
    }

    private void showCampaignVictory() {
        Pane victory=new Pane();victory.setPrefSize(1920,1080);victory.setStyle("-fx-background-color:#100d18;");
        javafx.scene.control.Label title=new javafx.scene.control.Label("ТЁМНЫЙ ЛОРД ПОВЕРЖЕН");
        title.setStyle("-fx-text-fill:#f2d08a;-fx-font-size:48px;");title.relocate(570,330);
        javafx.scene.control.Label result=new javafx.scene.control.Label(campaignPlayer.getName()+" · Уровень "+campaignPlayer.getLevel()+" · "+campaignWallet.getBalance()+" монет");
        result.setStyle("-fx-text-fill:#eadbc2;-fx-font-size:28px;");result.relocate(620,450);
        Button back=new Button("В главное меню");back.setStyle("-fx-font-size:25px;");back.relocate(810,580);back.setOnAction(e -> returnToMenu());
        victory.getChildren().addAll(title,result,back);showFixedScreen(victory);
    }

    private void returnToMenu() {
        if(campaignArena!=null) campaignArena.suspend();
        menu = new MenuScreen();
        menu.setOnNewGame(this::onNewGame);
        menu.setOnContinue(this::onContinue);
        menu.setOnArena(this::openEnemyArena);
        menu.setOnSettings(this::onSettings);
        menu.setOnExit(() -> { menu.stop(); Platform.exit(); });
        menu.setContinueEnabled(campaignRun!=null);
        showFixedScreen(menu);
    }

    private void showFixedScreen(Pane screen) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color:black;");
        screen.setManaged(false);
        root.getChildren().add(screen);
        Runnable resize = () -> {
            double scale = Math.max(0.01, Math.min(root.getWidth() / MenuScreen.W, root.getHeight() / MenuScreen.H));
            screen.setScaleX(scale);
            screen.setScaleY(scale);
            screen.relocate((root.getWidth() - MenuScreen.W * scale) / 2,
                    (root.getHeight() - MenuScreen.H * scale) / 2);
        };
        root.widthProperty().addListener((obs, oldValue, newValue) -> resize.run());
        root.heightProperty().addListener((obs, oldValue, newValue) -> resize.run());
        resize.run();
        stage.getScene().setRoot(root);
    }

    private void onContinue() {
        if(campaignRun==null) return;
        menu.stop();
        if(!tutorialCompleted) showFixedScreen(new TutorialLevelScreen(campaignPlayer,this::onTutorialCompleted));
        else resumeCampaign();
    }

    private void onSettings() {
        // TODO: open the settings screen
        System.out.println("[Shadowfall] Settings");
    }

    private void openEnemyArena() {
        menu.stop();
        if (enemyArena != null) enemyArena.stop();
        enemyArena = new EnemyArenaScreen(this::returnToMenu);
        showFixedScreen(enemyArena);
    }

    private void openFinalBossDebug() {
        if (enemyArena != null) enemyArena.stop();
        enemyArena = new EnemyArenaScreen(this::returnToMenu);
        enemyArena.startFinalBossDebug();
        showFixedScreen(enemyArena);
    }
}
