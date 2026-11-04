package com.gamelab.ui.game;

import com.gamelab.domain.PlayerCharacter;
import com.gamelab.ui.components.HealthHud;
import com.gamelab.ui.menu.MenuScreen;
import com.gamelab.domain.Enemy;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.inventory.InventoryRules;
import com.gamelab.pattern.observer.GameEvent;
import com.gamelab.pattern.observer.GameEventTypes;
import com.gamelab.application.EnemyHealthState;
import com.gamelab.application.GameFacade;
import com.gamelab.ui.components.EnemyHealthHud;
import com.gamelab.ui.components.BossHealthHud;
import com.gamelab.ui.inventory.InventoryView;
import java.util.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/** Initial gameplay view with the player's live health HUD. */
public final class GameScreen extends Pane {
    private final PlayerCharacter player;
    private final HealthHud healthHud = new HealthHud();
    private final GameFacade facade = new GameFacade();
    private final Map<String, EnemyHealthHud> enemyHuds = new LinkedHashMap<>();
    private final BossHealthHud bossHud = new BossHealthHud(700);
    private final Inventory inventory;
    private final InventoryView inventoryView;

    public GameScreen(PlayerCharacter player, Runnable onReturnToMenu) {
        this.player = player;
        this.inventory = new Inventory(player.getCharacterClass(), InventoryRules.defaults(player.getCharacterClass(), 16));
        this.inventoryView = new InventoryView(inventory);
        setPrefSize(MenuScreen.W, MenuScreen.H);
        setMinSize(MenuScreen.W, MenuScreen.H);
        setMaxSize(MenuScreen.W, MenuScreen.H);
        setStyle("-fx-background-image: url('/assets/ui/backgrounds/menu_bg.png');"
                + "-fx-background-size: cover; -fx-background-color: #100d18;");

        Label title = new Label("SHADOWFALL");
        title.setTextFill(Color.web("#f2d08a"));
        title.setFont(Font.font("Serif", 36));
        title.setLayoutX(800);
        title.setLayoutY(80);

        Label hero = new Label(player.getName() + "  ·  " + player.getCharacterClass());
        hero.setTextFill(Color.web("#dfd4c1"));
        hero.setFont(Font.font("Serif", 22));
        hero.setLayoutX(40);
        hero.setLayoutY(120);

        healthHud.setLayoutX(32);
        healthHud.setLayoutY(32);
        refreshHealth();

        Label note = new Label("Путешествие начинается");
        note.setTextFill(Color.web("#c5a5ef"));
        note.setFont(Font.font("Serif", 28));
        note.setLayoutX(760);
        note.setLayoutY(510);

        Label back = new Label("ВЕРНУТЬСЯ В МЕНЮ");
        back.setTextFill(Color.web("#f2d08a"));
        back.setFont(Font.font("Serif", 18));
        back.setStyle("-fx-padding: 12 20; -fx-border-color: #9b7040; -fx-background-color: #100e1b;");
        back.setLayoutX(830);
        back.setLayoutY(950);
        back.setOnMouseClicked(event -> onReturnToMenu.run());
        back.setOnMouseEntered(event -> back.setOpacity(.75));
        back.setOnMouseExited(event -> back.setOpacity(1));

        bossHud.setLayoutX((MenuScreen.W-700)/2); bossHud.setLayoutY(MenuScreen.H-105); bossHud.setVisible(false);
        inventoryView.setLayoutX(610); inventoryView.setLayoutY(240); inventoryView.setPrefSize(700, 500); inventoryView.setVisible(false);
        facade.events().subscribe(event -> handleEvent(event));
        getChildren().addAll(title, hero, healthHud, note, back, bossHud, inventoryView);
    }

    /** Call after combat, healing, or a level-up changes the player's health. */
    public void refreshHealth() {
        healthHud.setHealth(player.getHealth(), player.getMaxHealth());
    }

    public HealthHud getHealthHud() {
        return healthHud;
    }

    public GameFacade getFacade() { return facade; }
    public Inventory getInventory() { return inventory; }
    public InventoryView getInventoryView() { return inventoryView; }
    public Map<String, EnemyHealthHud> getEnemyHuds() { return Collections.unmodifiableMap(enemyHuds); }

    /** Connect the active simulation's entities; call refreshEnemyPositions from its existing frame loop. */
    public void setEnemies(Collection<Enemy> enemies) {
        Set<String> live = new HashSet<>();
        for (Enemy enemy : enemies) {
            live.add(enemy.getId());
            if (enemy.isBoss()) { bossHud.setBoss(enemy.getName(), enemy.getHealth(), enemy.getMaxHealth()); bossHud.setVisible(enemy.isAlive()); }
            else if (enemy.isAlive()) {
                EnemyHealthHud hud = enemyHuds.computeIfAbsent(enemy.getId(), id -> { EnemyHealthHud h=new EnemyHealthHud(id,enemy.getHealth(),enemy.getMaxHealth()); getChildren().add(h); return h; });
                hud.setHealth(enemy.getHealth(),enemy.getMaxHealth()); hud.setVisible(true);
            } else { EnemyHealthHud dead=enemyHuds.remove(enemy.getId()); if(dead!=null)getChildren().remove(dead); }
        }
        Iterator<Map.Entry<String,EnemyHealthHud>> it=enemyHuds.entrySet().iterator();
        while(it.hasNext()){Map.Entry<String,EnemyHealthHud> entry=it.next();if(!live.contains(entry.getKey())){getChildren().remove(entry.getValue());it.remove();}}
        refreshEnemyPositions(enemies);
    }

    public void refreshEnemyPositions(Collection<Enemy> enemies) {
        for (Enemy enemy : enemies) if (!enemy.isBoss()) {
            EnemyHealthHud hud=enemyHuds.get(enemy.getId());
            if (hud != null) { hud.setLayoutX(enemy.getX()-55); hud.setLayoutY(enemy.getY()-30); hud.setVisible(enemy.isAlive()); }
        }
    }

    private void handleEvent(GameEvent event) {
        if (GameEventTypes.ENEMY_HEALTH_CHANGED.equals(event.type()) && event.payload() instanceof EnemyHealthState state) {
            if (state.boss()) { bossHud.setBoss(state.name(),state.health(),state.maxHealth()); bossHud.setVisible(state.alive()); }
            else {
                EnemyHealthHud hud=enemyHuds.get(state.id());
                if (hud != null) { hud.setHealth(state.health(),state.maxHealth()); hud.setVisible(state.alive()); if(!state.alive()){enemyHuds.remove(state.id());getChildren().remove(hud);} }
            }
        }
    }

    public void toggleInventory() { inventoryView.setVisible(!inventoryView.isVisible()); }
}
