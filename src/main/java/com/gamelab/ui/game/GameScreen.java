package com.gamelab.ui.game;

import com.gamelab.application.EnemyHealthState;
import com.gamelab.application.GameFacade;
import com.gamelab.domain.Enemy;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.pattern.observer.GameEvent;
import com.gamelab.pattern.observer.GameEventTypes;
import com.gamelab.ui.components.BossHealthHud;
import com.gamelab.ui.components.EnemyHealthHud;
import com.gamelab.ui.components.HealthHud;
import com.gamelab.ui.inventory.InventoryView;
import com.gamelab.ui.menu.MenuScreen;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Gameplay view with player, enemy and boss health HUDs plus inventory. */
public final class GameScreen extends Pane {
    private final PlayerCharacter player;
    private final HealthHud healthHud = new HealthHud();
    private final GameFacade facade = new GameFacade();
    private final Map<String, EnemyHealthHud> enemyHuds = new LinkedHashMap<>();
    private final BossHealthHud bossHud = new BossHealthHud(700);
    private final Inventory inventory = new Inventory(16);
    private final InventoryView inventoryView = new InventoryView(inventory);

    public GameScreen(PlayerCharacter player, Runnable onReturnToMenu) {
        this.player = player;
        setPrefSize(MenuScreen.W, MenuScreen.H); setMinSize(MenuScreen.W, MenuScreen.H); setMaxSize(MenuScreen.W, MenuScreen.H);
        setStyle("-fx-background-color: #100d18;");
        Label title = new Label("SHADOWFALL"); title.setTextFill(Color.web("#f2d08a")); title.setFont(Font.font("Serif", 36)); title.relocate(800, 80);
        Label hero = new Label(player.getName() + "  ·  " + player.getCharacterClass()); hero.setTextFill(Color.web("#dfd4c1")); hero.setFont(Font.font("Serif", 22)); hero.relocate(40, 120);
        healthHud.relocate(32, 32); refreshHealth();
        Label back = new Label("RETURN TO MENU"); back.setTextFill(Color.web("#f2d08a")); back.setFont(Font.font("Serif", 18));
        back.setStyle("-fx-padding: 12 20; -fx-border-color: #9b7040; -fx-background-color: #100e1b;"); back.relocate(830, 950); back.setOnMouseClicked(event -> onReturnToMenu.run());
        bossHud.relocate((MenuScreen.W - 700) / 2, MenuScreen.H - 105); bossHud.setVisible(false);
        inventoryView.relocate(610, 240); inventoryView.setPrefSize(700, 500); inventoryView.setVisible(false);
        facade.events().subscribe(this::handleEvent);
        getChildren().addAll(title, hero, healthHud, back, bossHud, inventoryView);
    }

    public void refreshHealth() { healthHud.setHealth(player.getHealth(), player.getMaxHealth()); }
    public HealthHud getHealthHud() { return healthHud; }
    public GameFacade getFacade() { return facade; }
    public Inventory getInventory() { return inventory; }
    public InventoryView getInventoryView() { return inventoryView; }
    public Map<String, EnemyHealthHud> getEnemyHuds() { return Collections.unmodifiableMap(enemyHuds); }

    public void setEnemies(Collection<Enemy> enemies) {
        Set<String> live = new HashSet<>();
        for (Enemy enemy : enemies) {
            live.add(enemy.getId());
            if (enemy.isBoss()) {
                bossHud.setBoss(enemy.getName(), enemy.getHealth(), enemy.getMaxHealth()); bossHud.setVisible(enemy.isAlive());
            } else if (enemy.isAlive()) {
                EnemyHealthHud hud = enemyHuds.computeIfAbsent(enemy.getId(), id -> { EnemyHealthHud h = new EnemyHealthHud(id, enemy.getHealth(), enemy.getMaxHealth()); getChildren().add(h); return h; });
                hud.setHealth(enemy.getHealth(), enemy.getMaxHealth()); hud.setVisible(true);
            } else {
                EnemyHealthHud dead = enemyHuds.remove(enemy.getId()); if (dead != null) getChildren().remove(dead);
            }
        }
        Iterator<Map.Entry<String, EnemyHealthHud>> it = enemyHuds.entrySet().iterator();
        while (it.hasNext()) { Map.Entry<String, EnemyHealthHud> e = it.next(); if (!live.contains(e.getKey())) { getChildren().remove(e.getValue()); it.remove(); } }
        refreshEnemyPositions(enemies);
    }

    public void refreshEnemyPositions(Collection<Enemy> enemies) {
        for (Enemy enemy : enemies) if (!enemy.isBoss()) {
            EnemyHealthHud hud = enemyHuds.get(enemy.getId());
            if (hud != null) { hud.relocate(enemy.getX() - 55, enemy.getY() - 30); hud.setVisible(enemy.isAlive()); }
        }
    }

    private void handleEvent(GameEvent event) {
        if (!GameEventTypes.ENEMY_HEALTH_CHANGED.equals(event.type()) || !(event.payload() instanceof EnemyHealthState state)) return;
        if (state.boss()) { bossHud.setBoss(state.name(), state.health(), state.maxHealth()); bossHud.setVisible(state.alive()); return; }
        EnemyHealthHud hud = enemyHuds.get(state.id());
        if (hud != null) { hud.setHealth(state.health(), state.maxHealth()); hud.setVisible(state.alive()); if (!state.alive()) { enemyHuds.remove(state.id()); getChildren().remove(hud); } }
    }

    public void toggleInventory() { inventoryView.setVisible(!inventoryView.isVisible()); }
}

