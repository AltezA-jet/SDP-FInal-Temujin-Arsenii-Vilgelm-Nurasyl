package com.gamelab.ui.level;

import com.gamelab.application.arena.ArenaCatalog;
import com.gamelab.application.arena.ArenaSimulation;
import com.gamelab.application.arena.PlatformPhysics;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.pattern.builder.CharacterBuilder;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.strategy.BalancedStrategy;
import com.gamelab.pattern.strategy.HeavyAttackStrategy;
import com.gamelab.ui.components.MaleWarriorSprite;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.net.URL;
import java.util.*;

/** Enemy sandbox, intentionally separate from the tutorial and campaign progression. */
public final class EnemyArenaScreen extends Pane {
    private static final double W=1920, H=1080;
    private static final String ROOT="/assets/enemies/";
    private final Canvas canvas=new Canvas(W,H);
    private final MaleWarriorSprite hero=new MaleWarriorSprite(170);
    private final ArenaSimulation simulation;
    private final Set<KeyCode> keys=EnumSet.noneOf(KeyCode.class);
    private final Map<String,Image> images=new HashMap<>();
    private final ComboBox<String> stage=new ComboBox<>(), encounter=new ComboBox<>();
    private final Label status=new Label(), health=new Label();
    private final CheckBox paused=new CheckBox("Пауза");
    private final Runnable onBack;
    private double time;
    private long previous, renderedAttack;
    private int activeStage=1;
    private final AnimationTimer timer=new AnimationTimer() {
        @Override public void handle(long now) {
            double dt=previous==0 ? .016 : Math.min(.05,(now-previous)/1e9);
            previous=now;
            if (!paused.isSelected()) {
                time+=dt;
                double movement=(keys.contains(KeyCode.D)||keys.contains(KeyCode.RIGHT) ? 1 : 0)
                        -(keys.contains(KeyCode.A)||keys.contains(KeyCode.LEFT) ? 1 : 0);
                simulation.update(dt,movement,keys.contains(KeyCode.S)||keys.contains(KeyCode.DOWN));
                hero.setWalking(movement!=0,simulation.crouching());
                hero.setFacingLeft(simulation.facingLeft());
                if (renderedAttack!=simulation.attackSerial()) { renderedAttack=simulation.attackSerial(); hero.setAttacking(); }
                hero.update(dt);
            }
            render();
        }
    };

    public EnemyArenaScreen(Runnable onBack) {
        this(onBack,new GameEventBus());
    }

    EnemyArenaScreen(Runnable onBack, GameEventBus events) {
        this.onBack=Objects.requireNonNull(onBack);
        PlayerCharacter player=CharacterBuilder.warrior("Испытатель").maxHealth(600).damage(35).defense(0).build();
        simulation=new ArenaSimulation(player,events);
        events.subscribe(event -> status.setText(event.message()));
        setPrefSize(W,H); setMinSize(W,H); setMaxSize(W,H);
        setStyle("-fx-background-color: #100d18; -fx-font-size: 18px;");
        getChildren().addAll(canvas,hero);
        hero.setMouseTransparent(true);
        buildControls();
        setFocusTraversable(true);
        setOnKeyPressed(event -> {
            KeyCode code=event.getCode();
            boolean first=keys.add(code);
            if (first && !paused.isSelected()) {
                if (code==KeyCode.W || code==KeyCode.UP || code==KeyCode.SPACE) simulation.jump();
                if (code==KeyCode.J) simulation.attack();
                if (code==KeyCode.SHIFT) simulation.dodge();
            }
            if (first && code==KeyCode.R) simulation.restart();
            if (first && code==KeyCode.ESCAPE) leave();
        });
        setOnKeyReleased(event -> keys.remove(event.getCode()));
        setOnMousePressed(event -> {
            if (event.getY()<180 || event.getY()>=950 || event.getButton()!=MouseButton.PRIMARY) return;
            requestFocus();
            if (!paused.isSelected()) simulation.attack();
        });
        focusedProperty().addListener((obs,old,value) -> { if (!value) keys.clear(); });
        sceneProperty().addListener((obs,old,value) -> {
            if (old!=null && value==null) stop();
            if (value!=null) Platform.runLater(this::requestFocus);
        });
        spawnSelection();
        timer.start();
    }

    private void buildControls() {
        Label title=new Label("АРЕНА ВРАГОВ  /  ТЕСТОВЫЙ УРОВЕНЬ");
        title.setStyle("-fx-text-fill: #f2d08a; -fx-font-size: 27px; -fx-font-weight: bold;");
        title.relocate(30,15); getChildren().add(title);
        for (int i=1;i<=5;i++) stage.getItems().add("Этап "+i);
        stage.getItems().add("Финальный босс"); stage.getSelectionModel().select(0);
        encounter.getItems().addAll("Обычные враги этапа","Босс этапа"); encounter.getSelectionModel().select(0);
        stage.setOnAction(event -> {
            if (stage.getSelectionModel().getSelectedIndex()==5) encounter.getSelectionModel().select(1);
            encounter.setDisable(stage.getSelectionModel().getSelectedIndex()==5);
        });
        HBox row=new HBox(12,stage,encounter,button("Создать",this::spawnSelection),
                button("Заново / R",simulation::restart),button("Вылечить",simulation::restorePlayer),
                button("Обычным врагам 20% HP",simulation::woundEnemies),button("В меню",this::leave));
        row.relocate(30,62); getChildren().add(row);
        ComboBox<String> behavior=new ComboBox<>();
        behavior.getItems().addAll("Поведение: по типу","Поведение: сближаться","Поведение: дистанция");
        behavior.getSelectionModel().select(0);
        behavior.setOnAction(event -> { simulation.setBehaviorMode(ArenaSimulation.BehaviorMode.values()[behavior.getSelectionModel().getSelectedIndex()]); requestFocus(); });
        ComboBox<String> attack=new ComboBox<>();
        attack.getItems().addAll("Удар: обычный","Удар: тяжёлый"); attack.getSelectionModel().select(0);
        attack.setOnAction(event -> { simulation.setPlayerStrategy(attack.getSelectionModel().getSelectedIndex()==0 ? new BalancedStrategy() : new HeavyAttackStrategy()); requestFocus(); });
        CheckBox immortal=new CheckBox("Бессмертие"); immortal.setStyle("-fx-text-fill: #eadbc2;");
        immortal.setOnAction(event -> { simulation.setInvincible(immortal.isSelected()); requestFocus(); });
        paused.setStyle("-fx-text-fill: #eadbc2;");
        paused.setOnAction(event -> { keys.clear(); requestFocus(); });
        health.setStyle("-fx-text-fill: #f2d08a;");
        HBox options=new HBox(20,behavior,attack,immortal,paused,health);
        options.relocate(30,119); getChildren().add(options);
        Label help=new Label("A/D — движение   W/Space — прыжок   S — присесть   Shift — уклонение   J/ЛКМ — удар   Esc — меню");
        help.setStyle("-fx-text-fill: #f2d08a; -fx-font-size: 21px;"); help.relocate(30,956);
        status.setStyle("-fx-text-fill: #dfd4c1; -fx-font-size: 20px;"); status.relocate(30,1000);
        Label note=new Label("Боссы на земле. Их снаряды проходят платформы. История убитых сохраняется между боями арены.");
        note.setStyle("-fx-text-fill: #ac9cb6; -fx-font-size: 17px;"); note.relocate(30,1038);
        getChildren().addAll(help,status,note);
    }
    private Button button(String text,Runnable action) {
        Button button=new Button(text);
        button.setOnAction(event -> { action.run(); requestFocus(); });
        return button;
    }
    private void spawnSelection() {
        activeStage=stage.getSelectionModel().getSelectedIndex()+1;
        keys.clear();
        simulation.start(activeStage,activeStage==6 || encounter.getSelectionModel().getSelectedIndex()==1);
        render();
        requestFocus();
    }
    private void render() {
        hero.relocate(simulation.hero().x-57,simulation.hero().y-170);
        hero.setOpacity(simulation.dodging() ? .4 : 1);
        health.setText("HP "+simulation.player().getHealth()+" / "+simulation.player().getMaxHealth()
                +(simulation.poisoned() ? "   ЯД" : "")+"    Убито типов: "+simulation.defeatedTypes().size());
        draw();
    }
    private void leave() { stop(); onBack.run(); }
    public void stop() { timer.stop(); keys.clear(); }

    private Image image(String relative) {
        if (images.containsKey(relative)) return images.get(relative);
        URL resource=getClass().getResource(ROOT+relative);
        Image image=resource==null ? null : new Image(resource.toExternalForm());
        images.put(relative,image);
        return image;
    }
    private void draw() {
        GraphicsContext g=canvas.getGraphicsContext2D();
        g.setImageSmoothing(false);
        g.setFill(Color.web(activeStage==2 ? "#122739" : "#191527")); g.fillRect(0,0,W,H);
        String theme=activeStage==2 ? "2_background_platform/stage2_ice" : "1_background_platform/stage1_ruins";
        Image background=activeStage<=2 ? image("Background+platform/"+theme+"_background_1920x1080.png") : null;
        if (background!=null) g.drawImage(background,0,0,W,950);
        g.setFill(Color.rgb(8,8,18,.4)); g.fillRect(0,180,W,770);
        g.setFill(Color.web("#302638")); g.fillRect(0,PlatformPhysics.FLOOR,W,50);
        for (PlatformPhysics.Platform platform : ArenaSimulation.PLATFORMS) {
            Image texture=activeStage<=2 ? image("Background+platform/"+theme+"_platform_medium.png") : null;
            if (texture!=null) g.drawImage(texture,platform.x(),platform.y(),platform.width(),45);
            else { g.setFill(Color.web("#63516d")); g.fillRect(platform.x(),platform.y(),platform.width(),24); }
            g.setStroke(Color.web("#b8a58b")); g.strokeLine(platform.x(),platform.y(),platform.x()+platform.width(),platform.y());
        }
        for (ArenaSimulation.Zone zone : simulation.zones()) {
            g.setFill(zone.poison ? Color.rgb(117,206,65,.55) : Color.rgb(255,103,20,.65));
            g.fillOval(zone.x-zone.radius,zone.y-12,zone.radius*2,24);
            if (!zone.poison) {
                Image fire=image("3 Этап/fire_mag/burning_ground.png");
                if (fire!=null) g.drawImage(fire,((int)(time*8)%4)*64,0,64,32,zone.x-zone.radius,zone.y-55,zone.radius*2,55);
            }
        }
        for (ArenaSimulation.Fighter fighter : simulation.fighters()) if (fighter.enemy.isAlive()) drawEnemy(g,fighter);
        for (ArenaSimulation.Projectile shot : simulation.projectiles()) drawProjectile(g,shot);
        g.setFill(Color.rgb(10,8,20,.96)); g.fillRect(0,0,W,180); g.fillRect(0,950,W,130);
        if (!simulation.player().isAlive()) {
            g.setFill(Color.rgb(0,0,0,.65)); g.fillRect(500,400,920,130);
            g.setFill(Color.SALMON); g.setFont(Font.font(32)); g.fillText("Герой погиб. R — начать бой заново",620,475);
        } else if (simulation.fighters().stream().noneMatch(f -> f.enemy.isAlive())) {
            g.setFill(Color.LIGHTGREEN); g.setFont(Font.font(28)); g.fillText("Бой завершён — выбери следующий состав",640,280);
        }
    }

    private void drawEnemy(GraphicsContext g,ArenaSimulation.Fighter fighter) {
        ArenaCatalog.Profile profile=fighter.profile;
        double height=profile.boss() ? 195 : profile.frameHeight()==64 ? 85 : 140;
        double width=height*profile.frameWidth()/profile.frameHeight();
        String animation=fighter.animation;
        String base=profile.spriteFolder()+"/"+profile.spriteName();
        Image hurt=fighter.hurtTime>0 && !profile.spriteFolder().isEmpty() ? image(base+"_hurt.png") : null;
        Image sprite=hurt!=null ? hurt : profile.spriteFolder().isEmpty() ? null : image(base+"_"+animation+".png");
        if (sprite==null && animation.startsWith("attack")) sprite=image(base+"_attack.png");
        if (sprite==null && !profile.spriteFolder().isEmpty()) sprite=image(base+"_idle.png");
        if (sprite==null && profile.id().equals("elemental")) sprite=image(base+".png");
        double x=fighter.body.x, y=fighter.body.y;
        if (sprite!=null) {
            int frames=Math.max(1,(int)sprite.getWidth()/profile.frameWidth());
            int frame=hurt!=null ? Math.min(frames-1,(int)(fighter.hurtElapsed/.12)) : (int)(time*8)%frames;
            g.save(); g.translate(x,y);
            // Supplied enemy atlases face left; mirror toward the player on the right.
            if (simulation.hero().x>x) g.scale(-1,1);
            g.drawImage(sprite,frame*profile.frameWidth(),0,profile.frameWidth(),profile.frameHeight(),-width/2,-height,width,height);
            g.restore();
        } else {
            g.setFill(profile.boss() ? Color.web("#efb073") : Color.web("#d8c8ee"));
            g.setFont(Font.font(profile.boss() ? 25 : 21));
            g.fillText(profile.name(),x-75,y-65+Math.sin(time*3)*5);
        }
        double top=y-height-48;
        g.setFill(Color.web("#261c29")); g.fillRect(x-72,top,144,10);
        g.setFill(fighter.retreating() ? Color.ORANGE : Color.web("#c95567"));
        g.fillRect(x-72,top,144.0*fighter.enemy.getHealth()/fighter.enemy.getMaxHealth(),10);
        g.setFont(Font.font(15)); g.setFill(Color.web("#fff0cf"));
        g.fillText(profile.name()+(fighter.summoned ? " (призыв)" : ""),x-90,top-8);
        g.fillText(fighter.enemy.getHealth()+" / "+fighter.enemy.getMaxHealth(),x-50,top+28);
        g.setFill(fighter.windup>0 ? Color.YELLOW : Color.LIGHTGRAY);
        g.fillText(fighter.action,x-100,top+46);
        if (fighter.windup>0) { g.setStroke(Color.ORANGE); g.strokeOval(x-60,y-15,120,25); }
    }

    private void drawProjectile(GraphicsContext g,ArenaSimulation.Projectile shot) {
        String path=switch (shot.source) {
            case "archer" -> "1 Этап/Archer/Arrow.png";
            case "guardian" -> "1 Этап/Rune_Guardian(miniBoss)/rune_guardian_projectile.png";
            case "elemental" -> "2 Этап/ice_elemental/ice_snowball.png";
            case "ice-golem" -> "2 Этап/ice_golem(miniBoss)/ice_golem_projectile_"+(shot.radius>20 ? "big" : "small")+".png";
            case "mage" -> "3 Этап/fire_mag/fireball_fly.png";
            default -> "";
        };
        Image sprite=path.isEmpty() ? null : image(path);
        if (sprite==null) {
            g.setFill(shot.kind==ArenaCatalog.Attack.POISON || shot.kind==ArenaCatalog.Attack.WITCH ? Color.YELLOWGREEN : Color.MEDIUMPURPLE);
            g.fillOval(shot.x-shot.radius,shot.y-shot.radius,shot.radius*2,shot.radius*2);
        } else {
            // Arrow and guardian projectile were drawn facing left, unlike the other atlases.
            double nativeAngle=shot.source.equals("archer") || shot.source.equals("guardian") ? 180 : 0;
            g.save(); g.translate(shot.x,shot.y); g.rotate(Math.toDegrees(Math.atan2(shot.vy,shot.vx))-nativeAngle);
            int frames=shot.source.equals("mage") ? 4 : 1;
            double frameWidth=sprite.getWidth()/frames;
            double h=Math.max(16,shot.radius*1.5), w=h*frameWidth/sprite.getHeight();
            g.drawImage(sprite,((int)(time*10)%frames)*frameWidth,0,frameWidth,sprite.getHeight(),-w/2,-h/2,w,h);
            g.restore();
        }
    }
}
