package com.gamelab.ui.level;

import com.gamelab.application.arena.ArenaCatalog;
import com.gamelab.application.arena.ArenaLayouts;
import com.gamelab.application.arena.CampaignRun;
import com.gamelab.application.arena.ArenaSimulation;
import com.gamelab.application.arena.PlatformPhysics;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.item.*;
import com.gamelab.domain.rune.*;
import com.gamelab.pattern.builder.CharacterBuilder;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.pattern.strategy.BalancedStrategy;
import com.gamelab.pattern.strategy.HeavyAttackStrategy;
import com.gamelab.ui.components.MaleWarriorSprite;
import com.gamelab.ui.components.SpriteGrounding;
import com.gamelab.ui.audio.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.net.URL;
import java.util.*;

/** Shared arena renderer with separate campaign controls and sandbox tools. */
public final class EnemyArenaScreen extends Pane {
    private static final double W=1920, H=1080;
    private static final double HERO_DRAW_OFFSET_Y=6;
    private static final double ENEMY_DRAW_OFFSET_Y=4;
    private static final String ROOT="/assets/enemies/";
    private final Canvas canvas=new Canvas(W,H);
    private final Canvas playerProjectileCanvas=new Canvas(W,H);
    private final MaleWarriorSprite hero=new MaleWarriorSprite(MaleWarriorSprite.GAME_HEIGHT);
    private final ArenaSimulation simulation;
    private final CombatAudio audio;
    private final boolean ownsAudio;
    private final GameEventBus eventBus;
    private final com.gamelab.pattern.observer.GameObserver eventSubscription;
    private final Set<KeyCode> keys=EnumSet.noneOf(KeyCode.class);
    private final Map<String,Image> images=new HashMap<>();
    private final ComboBox<String> stage=new ComboBox<>(), encounter=new ComboBox<>();
    private final Label status=new Label(), health=new Label();
    private final CheckBox paused=new CheckBox("Пауза");
    private final Runnable onBack;
    private final boolean campaign;
    private Runnable usePotion=() -> { };
    private java.util.function.IntSupplier potionCount=() -> 0;
    private String notice="";
    private double noticeTime,hitFlash;
    private double time;
    private long previous, renderedAttack;
    private int activeStage=1;
    private RuneLoadout loadout=new RuneLoadout(WeaponCatalog.createStarterSword());
    private final Label equipment=new Label();
    private java.util.function.IntConsumer onStageCompleted=stage -> { };
    /** Campaign integration point: open the shop only after the cleared arena's right-hand exit. */
    public void setOnStageCompleted(java.util.function.IntConsumer action) { onStageCompleted=Objects.requireNonNull(action); }
    public void setUsePotion(Runnable action) { usePotion=Objects.requireNonNull(action); }
    public void setPotionCount(java.util.function.IntSupplier value) { potionCount=Objects.requireNonNull(value); }
    public void resume() {
        paused.setSelected(false); previous=0; keys.clear();
        hero.setWeapon(loadout.getWeapon().getName()); updateEquipment(); timer.start();
        Platform.runLater(this::requestFocus);
    }
    public void suspend() { timer.stop(); keys.clear(); audio.stop(); }
    private final AnimationTimer timer=new AnimationTimer() {
        @Override public void handle(long now) {
            double dt=previous==0 ? .016 : Math.min(.05,(now-previous)/1e9);
            previous=now;
            if (!paused.isSelected()) {
                time+=dt;
                noticeTime=Math.max(0,noticeTime-dt);hitFlash=Math.max(0,hitFlash-dt);
                double movement=(keys.contains(KeyCode.D)||keys.contains(KeyCode.RIGHT) ? 1 : 0)
                        -(keys.contains(KeyCode.A)||keys.contains(KeyCode.LEFT) ? 1 : 0);
                simulation.update(dt,movement,keys.contains(KeyCode.S)||keys.contains(KeyCode.DOWN));
                hero.setWalking(movement!=0,simulation.crouching());
                hero.setFacingLeft(simulation.facingLeft());
                if (renderedAttack!=simulation.attackSerial()) { renderedAttack=simulation.attackSerial(); hero.setAttackDuration(simulation.playerAttackDuration()); hero.setAttacking(); }
                if(!simulation.attacking()) hero.cancelAttack();
                hero.update(dt);
            }
            render();
        }
    };

    public EnemyArenaScreen(Runnable onBack) {
        this(onBack,new GameEventBus());
    }

    EnemyArenaScreen(Runnable onBack, GameEventBus events) {
        this(onBack,events,new JavaSoundPlayer());
    }

    EnemyArenaScreen(Runnable onBack, GameEventBus events, SoundPlayer soundPlayer) {
        this(onBack,events,soundPlayer,null,1,null);
    }
    public EnemyArenaScreen(CampaignRun run,Runnable onPause) {
        this(onPause,run.events(),new JavaSoundPlayer(),run.simulation(),run.activeStage(),null);
    }
    public EnemyArenaScreen(CampaignRun run,Runnable onPause,CombatAudio sharedAudio) {
        this(onPause,run.events(),null,run.simulation(),run.activeStage(),Objects.requireNonNull(sharedAudio));
    }
    private EnemyArenaScreen(Runnable onBack,GameEventBus events,SoundPlayer soundPlayer,ArenaSimulation provided,int campaignStage,CombatAudio sharedAudio) {
        this.onBack=Objects.requireNonNull(onBack);
        campaign=provided!=null;
        PlayerCharacter player=CharacterBuilder.warrior("Испытатель").maxHealth(250).damage(10).defense(0).build();
        simulation=campaign?provided:new ArenaSimulation(player,events);
        if(campaign) { loadout=simulation.loadout(); activeStage=campaignStage; }
        simulation.setLoadout(loadout);
        PlayerCharacter appearance=simulation.player();
        hero.setClass(appearance.getCharacterClass());
        hero.setConfiguration(appearance.getHairColorIndex(),appearance.getShirtColorIndex(),appearance.getPantsColorIndex(),appearance.getBootsColorIndex(),appearance.getHeadwearIndex());
        hero.setWeapon(loadout.getWeapon().getName());
        ownsAudio=sharedAudio==null;
        audio=ownsAudio?new CombatAudio(events,soundPlayer):sharedAudio;
        eventBus=events;
        eventSubscription=event -> {
            if(!campaign) status.setText(event.message());
            if(event.type().equals("PLAYER_HIT")) hitFlash=.18;
            if(campaign && (event.type().equals("ARENA_WAVE")||event.type().equals("ARENA_EXIT_READY")||event.type().equals("BOSS_PHASE_CHANGED"))) {
                notice=event.type().equals("BOSS_PHASE_CHANGED")?"Лорд меняет облик":event.message();noticeTime=3;
            }
            if(event.type().equals("ARENA_STAGE_COMPLETED")) Platform.runLater(() -> onStageCompleted.accept((Integer)event.payload()));
        };
        events.subscribe(eventSubscription);
        setPrefSize(W,H); setMinSize(W,H); setMaxSize(W,H);
        setStyle("-fx-background-color: #100d18; -fx-font-size: 18px;");
        playerProjectileCanvas.setMouseTransparent(true);
        getChildren().addAll(canvas,hero,playerProjectileCanvas);
        hero.setMouseTransparent(true);
        if(campaign) buildCampaignControls(); else buildControls();
        setFocusTraversable(true);
        setOnKeyPressed(event -> {
            KeyCode code=event.getCode();
            boolean first=keys.add(code);
            if (first && !paused.isSelected()) {
                if (code==KeyCode.W || code==KeyCode.UP || code==KeyCode.SPACE) simulation.jump();
                if (code==KeyCode.J) simulation.attack();
                if (code==KeyCode.SHIFT) simulation.boost();
                if (code==KeyCode.E) simulation.useClassAbility();
                if (code==KeyCode.DIGIT1) simulation.useSkill(0);
                if (code==KeyCode.DIGIT2) simulation.useSkill(1);
                if (code==KeyCode.DIGIT3) simulation.useSkill(2);
                if (code==KeyCode.DIGIT4) simulation.useSkill(3);
                if (code==KeyCode.DIGIT5) simulation.useSkill(4);
                if(code==KeyCode.H && campaign) usePotion.run();
            }
            if (first && code==KeyCode.R) simulation.restart();
            if (first && code==KeyCode.ESCAPE) leave();
        });
        setOnKeyReleased(event -> keys.remove(event.getCode()));
        setOnMousePressed(event -> {
            if ((!campaign && (event.getY()<180 || event.getY()>=950)) || event.getButton()!=MouseButton.PRIMARY) return;
            requestFocus();
            if (!paused.isSelected()) simulation.attack();
        });
        focusedProperty().addListener((obs,old,value) -> { if (!value) keys.clear(); });
        sceneProperty().addListener((obs,old,value) -> {
            if (old!=null && value==null) { timer.stop();keys.clear(); }
            if (value!=null) Platform.runLater(this::requestFocus);
        });
        if(!campaign) spawnSelection(); else render();
        timer.start();
    }

    private void buildCampaignControls() {
        notice=ArenaLayouts.stage(activeStage).name();noticeTime=3;
    }

    private void buildControls() {
        Label title=new Label("АРЕНА ВРАГОВ  /  ТЕСТОВЫЙ УРОВЕНЬ");
        title.setStyle("-fx-text-fill: #f2d08a; -fx-font-size: 27px; -fx-font-weight: bold;");
        title.relocate(30,15); getChildren().add(title);
        for (int i=1;i<=5;i++) stage.getItems().add("Этап "+i);
        stage.getItems().add("Финальный босс"); stage.getSelectionModel().select(0);
        encounter.getItems().addAll("Обычные враги этапа","Босс этапа","Полный этап: 3 волны"); encounter.getSelectionModel().select(2);
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
        paused.setOnAction(event -> { keys.clear(); if (paused.isSelected()) audio.stop(); requestFocus(); });
        health.setStyle("-fx-text-fill: #f2d08a;");
        HBox options=new HBox(20,behavior,attack,immortal,paused,health);
        options.relocate(30,119); getChildren().add(options);
        Label soundLabel=new Label("Звук"); soundLabel.setStyle("-fx-text-fill: #eadbc2;");
        Slider volume=new Slider(0,100,65); volume.setPrefWidth(150);
        volume.valueProperty().addListener((obs,old,value) -> audio.setVolume(value.doubleValue()/100));
        HBox soundOptions=new HBox(12,soundLabel,volume); soundOptions.relocate(1600,127);
        getChildren().add(soundOptions);
        Label help=new Label("A/D — движение   W/Space — прыжок   S — присесть   Shift — ускорение   E — способность класса   J/ЛКМ — удар   Esc — меню");
        help.setStyle("-fx-text-fill: #f2d08a; -fx-font-size: 21px;"); help.relocate(30,956);
        status.setStyle("-fx-text-fill: #dfd4c1; -fx-font-size: 20px;"); status.relocate(30,1000);
        getChildren().addAll(help,status);
        ComboBox<String> weapons=new ComboBox<>();
        WeaponCatalog.createAll().forEach(w -> weapons.getItems().add(w.getName())); weapons.getSelectionModel().select(0);
        weapons.setOnAction(e -> {
            loadout=new RuneLoadout(WeaponCatalog.createByLevel(weapons.getSelectionModel().getSelectedIndex()+1));
            simulation.setLoadout(loadout); hero.setWeapon(loadout.getWeapon().getName()); updateEquipment(); requestFocus();
        });
        ComboBox<RuneType> runes=new ComboBox<>(); runes.getItems().addAll(RuneType.values()); runes.getSelectionModel().select(RuneType.POISON);
        equipment.setStyle("-fx-text-fill: #f2d08a; -fx-font-size: 16px;");
        HBox gear=new HBox(10,weapons,runes,button("Установить руну",() -> {
            try { loadout.installRune(RuneCatalog.create(runes.getValue())); updateEquipment(); }
            catch(IllegalStateException ex) { status.setText(ex.getMessage()); }
        }),button("Снять руны",() -> {loadout.removeAllRunes();updateEquipment();}),equipment);
        gear.relocate(30,1035); getChildren().add(gear); updateEquipment();
    }
    private void updateEquipment() {
        Weapon w=loadout.createDecoratedWeapon();
        equipment.setText("Урон +"+w.getDamageBonus()+" | Скорость "+String.format(java.util.Locale.ROOT,"%.2f",w.getAttackSpeed())+
                " | Слоты "+loadout.getInstalledRunes().size()+"/"+w.getRuneSlotCount()+" "+loadout.getInstalledRunes().stream().map(r -> r.getRuneType().name()).toList());
    }
    private Button button(String text,Runnable action) {
        Button button=new Button(text);
        button.setOnAction(event -> { action.run(); requestFocus(); });
        return button;
    }
    private void spawnSelection() {
        activeStage=stage.getSelectionModel().getSelectedIndex()+1;
        keys.clear();
        if(encounter.getSelectionModel().getSelectedIndex()==2 || activeStage==6) simulation.startStage(activeStage);
        else simulation.start(activeStage,encounter.getSelectionModel().getSelectedIndex()==1);
        render();
        requestFocus();
    }
    /** Opens the final Lord encounter directly in the sandbox. */
    public void startFinalBossDebug() {
        if (campaign) throw new IllegalStateException("Debug boss launch is only available in the sandbox");
        stage.getSelectionModel().select(5);
        encounter.getSelectionModel().select(1);
        spawnSelection();
    }
    private void render() {
        hero.relocate(Math.rint(simulation.hero().x-hero.prefWidth(-1)/2),simulation.hero().y-hero.prefHeight(-1)+hero.getFootOffset()+HERO_DRAW_OFFSET_Y);
        hero.setOpacity(simulation.invisible() ? .16 : simulation.boosting() ? .82 : 1);
        health.setText("HP "+simulation.player().getHealth()+" / "+simulation.player().getMaxHealth()
                +(simulation.player().getMaxMana()>0 ? "    MP "+simulation.player().getMana()+" / "+simulation.player().getMaxMana() : "")
                +(simulation.poisoned() ? "   ЯД" : "")+(simulation.slowed()?"   ЛЁД":"")+
                (simulation.fullStage()?"    Волна "+simulation.wave()+"/"+(activeStage==6?1:3):"")+
                "    Ур. "+simulation.player().getLevel()+"    Shift: "+(simulation.boostCooldown()<=0?"готово":String.format(java.util.Locale.ROOT,"%.1f",simulation.boostCooldown())));
        draw();
        drawPlayerProjectiles();
    }
    private void leave() { if(campaign) suspend(); else stop(); onBack.run(); }
    public void stop() { timer.stop(); keys.clear(); if(ownsAudio) audio.close(); eventBus.unsubscribe(eventSubscription); }

    private Image image(String relative) {
        if (images.containsKey(relative)) return images.get(relative);
        URL resource=getClass().getResource(relative.startsWith("/") ? relative : ROOT+relative);
        Image image=resource==null ? null : new Image(resource.toExternalForm());
        images.put(relative,image);
        return image;
    }
    /** Align artwork with colliders despite the transparent padding in the supplied terrain PNGs. */
    private Image terrain(String path) {
        String key="crop:"+path;
        if(images.containsKey(key)) return images.get(key);
        Image source=image(path);
        if(source==null) throw new IllegalStateException("Missing terrain: "+path);
        var pixels=source.getPixelReader();
        boolean whiteMatte=path.endsWith(".jpg");
        int left=(int)source.getWidth(),top=(int)source.getHeight(),right=0,bottom=0;
        for(int y=0;y<source.getHeight();y++) for(int x=0;x<source.getWidth();x++) {
            int pixel=pixels.getArgb(x,y);
            // The first two ground sheets have opaque black padding instead of transparent padding.
            int red=pixel>>16&255,green=pixel>>8&255,blue=pixel&255;
            if((pixel>>>24)>48 && !(whiteMatte && red>235 && green>235 && blue>235) && (red+green+blue)>48) {
                left=Math.min(left,x);top=Math.min(top,y);right=Math.max(right,x);bottom=Math.max(bottom,y);
            }
        }
        if(right<left || bottom<top) throw new IllegalStateException("Empty terrain artwork: "+path);
        WritableImage cropped=new WritableImage(right-left+1,bottom-top+1);
        var writer=cropped.getPixelWriter();
        for(int y=top;y<=bottom;y++) for(int x=left;x<=right;x++) {
            int pixel=pixels.getArgb(x,y),red=pixel>>16&255,green=pixel>>8&255,blue=pixel&255;
            writer.setArgb(x-left,y-top,whiteMatte && red>235 && green>235 && blue>235 ? 0 : pixel);
        }
        images.put(key,cropped); return cropped;
    }
    private void draw() {
        GraphicsContext g=canvas.getGraphicsContext2D();
        g.setImageSmoothing(false);
        g.setFill(Color.web(activeStage==2 ? "#122739" : "#191527")); g.fillRect(0,0,W,H);
        ArenaLayouts.Layout layout=ArenaLayouts.stage(activeStage);
        g.drawImage(image(layout.asset(layout.background())),0,campaign?0:180,W,campaign?H:770);
        g.setFill(Color.rgb(8,8,18,.18)); g.fillRect(0,campaign?0:180,W,campaign?H:770);
        Image ground=terrain(layout.asset(layout.ground()));
        if(campaign) {g.setFill(Color.web("#19151e"));g.fillRect(0,PlatformPhysics.FLOOR,W,H-PlatformPhysics.FLOOR);}
        g.drawImage(ground,0,PlatformPhysics.FLOOR,W,Math.max(60,W*ground.getHeight()/ground.getWidth()));
        for (PlatformPhysics.Platform platform : simulation.platforms()) {
            Image texture=terrain(layout.asset(platform.width()<220?layout.small():layout.medium()));
            double height=platform.width()*texture.getHeight()/texture.getWidth();
            g.drawImage(texture,platform.x(),layout.platformTextureY(platform),platform.width(),height);
        }
        if(!campaign) {g.setFill(Color.web("#f2d08a")); g.setFont(Font.font(22)); g.fillText(layout.name(),30,220);}
        if(simulation.exitReady()) {
            g.setStroke(Color.AQUAMARINE); g.setLineWidth(5); g.strokeOval(1820,755,75,145);
            g.setFont(Font.font(20)); g.setFill(Color.AQUAMARINE); g.fillText("Выход →",1770,735);
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
        for (ArenaSimulation.Projectile shot : simulation.projectiles()) if(!shot.fromPlayer) drawProjectile(g,shot);
        for(ArenaSimulation.Impact impact:simulation.impacts()) {
            Image sprite=image("3 Этап/fire_mag/fireball_impact.png");
            int frame=Math.min(3,(int)(impact.time/.1));
            g.drawImage(sprite,frame*64,0,64,64,impact.x-90,impact.y-90,180,180);
        }
        for(var fighter:simulation.fighters()) if(fighter.enemy.isAlive() && fighter.windup>0) {
            if(fighter.animation.contains("summon")) {
                g.setStroke(Color.MEDIUMPURPLE); g.setLineWidth(3);
                g.strokeOval(fighter.body.x-90,fighter.body.y-16,180,32);
            }
            if(fighter.animation.equals("attack_poison")) {
                g.setFill(Color.rgb(155,255,80,.3+.2*Math.sin(time*15)));
                g.fillOval(fighter.body.x-35,fighter.body.y-150,70,70);
            }
        }
        if(campaign) drawCampaignHud(g);
        else {g.setFill(Color.rgb(10,8,20,.96)); g.fillRect(0,0,W,180); g.fillRect(0,950,W,130);}
        if (!simulation.player().isAlive()) {
            g.setFill(Color.rgb(0,0,0,.65)); g.fillRect(500,400,920,130);
            g.setFill(Color.SALMON); g.setFont(Font.font(32)); g.fillText("Герой погиб. R — начать заново · Esc — меню",580,475);
        } else if (simulation.completed() && !campaign) {
            g.setFill(Color.LIGHTGREEN); g.setFont(Font.font(28)); g.fillText("Этап завершён — выход в магазин (тест)",640,280);
        } else if (!campaign && simulation.fighters().stream().noneMatch(f -> f.enemy.isAlive())) {
            g.setFill(Color.LIGHTGREEN); g.setFont(Font.font(28));
            g.fillText(simulation.exitReady()?"Победа! Пройдите к выходу справа":simulation.fullStage()?"Волна пройдена — приготовьтесь к следующей":"Бой завершён — выбери следующий состав",550,280);
        }
    }

    private void drawCampaignHud(GraphicsContext g) {
        g.save();
        if(hitFlash>0) {g.setFill(Color.rgb(190,20,32,.10*hitFlash/.18));g.fillRect(0,0,W,H);}
        g.setFill(Color.rgb(10,8,18,.72));g.fillRoundRect(24,24,324,106,12,12);
        g.setFill(Color.web("#eadbc2"));g.setFont(Font.font(17));g.fillText("HP",40,49);
        g.setTextAlign(javafx.scene.text.TextAlignment.RIGHT);
        g.fillText(simulation.player().getHealth()+" / "+simulation.player().getMaxHealth(),330,49);
        g.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        g.setFill(Color.web("#3b1c27"));g.fillRoundRect(40,59,290,16,5,5);
        g.setFill(Color.web("#ce4b5b"));g.fillRoundRect(40,59,290.0*simulation.player().getHealth()/simulation.player().getMaxHealth(),16,5,5);
        if(simulation.player().getMaxMana()>0) {
            g.setFill(Color.web("#241c48"));g.fillRoundRect(40,81,290,12,5,5);
            g.setFill(Color.web("#7565dc"));g.fillRoundRect(40,81,290.0*simulation.player().getMana()/simulation.player().getMaxMana(),12,5,5);
            g.setFill(Color.web("#c9c2ff"));g.setFont(Font.font(12));g.fillText("MP  "+simulation.player().getMana()+" / "+simulation.player().getMaxMana(),45,91);
        }
        double dodge=1-Math.min(1,simulation.boostCooldown()/.85);
        double dodgeY=simulation.player().getMaxMana()>0?102:92;
        g.setFill(Color.web("#31414f"));g.fillRect(40,dodgeY,84,4);
        g.setFill(dodge==1?Color.web("#a1d5cf"):Color.web("#6f91a6"));g.fillRect(40,dodgeY,84*dodge,4);
        g.setFont(Font.font(13));g.setFill(Color.web("#b7c5ca"));g.fillText("Shift · BOOST",40,dodgeY+25);
        String effects=(simulation.poisoned()?"ЯД ":"")+(simulation.slowed()?"ЛЁД":"");
        g.setFill(Color.YELLOWGREEN);g.fillText(effects,220,113);
        g.setFill(Color.rgb(10,8,18,.70));g.fillRoundRect(24,H-63,160,39,9,9);
        g.setFill(Color.web("#ded1b5"));g.setFont(Font.font(16));g.fillText("H  ·  Зелья: "+potionCount.getAsInt(),39,H-38);
        String classResource=simulation.heroController().resourceHud();
        if (!classResource.isEmpty()) { g.setTextAlign(javafx.scene.text.TextAlignment.LEFT); g.setFill(Color.web("#e4d4b7")); g.setFont(Font.font(16)); g.fillText(classResource,205,H-38); }
        g.setTextAlign(javafx.scene.text.TextAlignment.LEFT);g.setFill(Color.web("#d9bd84"));g.setFont(Font.font(13));g.fillText("1-5  Class skills",24,H-82);
        g.setTextAlign(javafx.scene.text.TextAlignment.RIGHT);g.setFill(Color.web("#e4d4b7"));g.setFont(Font.font(17));
        g.fillText("Волна "+simulation.wave()+" / "+(activeStage==6?1:3),W-28,45);
        var boss=simulation.fighters().stream().filter(f -> f.profile.boss() && f.enemy.isAlive()).findFirst().orElse(null);
        if(boss!=null) {
            double x=580,width=760;
            g.setFill(Color.rgb(10,8,18,.78));g.fillRoundRect(x-18,20,width+36,64,9,9);
            g.setTextAlign(javafx.scene.text.TextAlignment.CENTER);g.setFont(Font.font(19));g.setFill(Color.web("#f0d4a5"));
            String phase=boss.profile.attack()==ArenaCatalog.Attack.FINAL?switch(boss.phase){case 1->" · Ледяная броня";case 2->" · Пылающая ярость";default->" · Истинная тень";}:"";
            g.fillText(boss.profile.name()+phase,W/2,44);
            g.setFill(Color.web("#3b1c27"));g.fillRect(x,56,width,11);
            g.setFill(Color.web("#b84c61"));g.fillRect(x,56,width*boss.enemy.getHealth()/boss.enemy.getMaxHealth(),11);
        }
        if(noticeTime>0) {
            g.setGlobalAlpha(Math.min(1,noticeTime));g.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            g.setFont(Font.font(25));g.setFill(Color.rgb(8,6,15,.65));g.fillRoundRect(510,142,900,50,10,10);
            g.setFill(Color.web("#f2d7a5"));g.fillText(notice,W/2,176);
        }
        g.restore();
    }

    private void drawEnemy(GraphicsContext g,ArenaSimulation.Fighter fighter) {
        ArenaCatalog.Profile profile=fighter.profile;
        double height=profile.frameHeight()*MaleWarriorSprite.GAME_SCALE;
        double width=profile.frameWidth()*MaleWarriorSprite.GAME_SCALE;
        String animation=fighter.animation;
        String base=profile.spriteFolder()+"/"+profile.spriteName();
        Image hurt=fighter.hurtTime>0 && !profile.spriteFolder().isEmpty() ? image(base+"_hurt.png") : null;
        Image sprite=hurt!=null ? hurt : profile.spriteFolder().isEmpty() ? null : image(base+"_"+animation+".png");
        if (sprite==null && animation.startsWith("attack")) sprite=image(base+"_attack.png");
        if (sprite==null && !profile.spriteFolder().isEmpty()) sprite=image(base+"_idle.png");
        if (sprite==null && profile.id().equals("elemental")) sprite=image(base+".png");
        double x=Math.rint(fighter.body.x), y=Math.rint(fighter.body.y);
        if (sprite!=null) {
            int frames=Math.max(1,(int)sprite.getWidth()/profile.frameWidth());
            boolean once=animation.startsWith("attack")||animation.startsWith("transform");
            double frameTime=animation.equals("roll")?.09:animation.equals("transform")||animation.equals("transform_back")?.14:
                once ? Math.max(.08,(fighter.animationTime+Math.max(0,fighter.windup))/frames):.14;
            if(animation.equals("transform")) frameTime=ArenaSimulation.ROLL_WINDUP/2;
            int frame=hurt!=null ? Math.min(frames-1,(int)(fighter.hurtElapsed/.12)) : once?Math.min(frames-1,(int)(fighter.animationTime/frameTime)):(int)(fighter.animationTime/frameTime)%frames;
            boolean hovering=profile.id().equals("ghost")||profile.id().equals("elemental");
            Image feetReference=profile.frameHeight()>=96 && animation.startsWith("attack") ? image(base+"_idle.png"):null;
            int padding=feetReference==null
                    ? SpriteGrounding.bottomPadding(sprite.getPixelReader(),frame*profile.frameWidth(),profile.frameWidth(),profile.frameHeight())
                    : SpriteGrounding.bottomPadding(feetReference.getPixelReader(),0,profile.frameWidth(),profile.frameHeight());
            double footOffset=hovering?0:padding*MaleWarriorSprite.GAME_SCALE;
            g.save(); g.translate(x,y+footOffset+ENEMY_DRAW_OFFSET_Y);
            // Supplied enemy atlases face left; mirror toward the player on the right.
            if (!fighter.facingLeft) g.scale(-1,1);
            g.drawImage(sprite,frame*profile.frameWidth(),0,profile.frameWidth(),profile.frameHeight(),-width/2,-height,width,height);
            g.restore();
        } else {
            g.setFill(profile.boss() ? Color.web("#efb073") : Color.web("#d8c8ee"));
            g.setFont(Font.font(profile.boss() ? 25 : 21));
            g.fillText(profile.name(),x-75,y-65+Math.sin(time*3)*5);
        }
        double top=y-height-48;
        if(!campaign) {
        g.setFill(Color.web("#261c29")); g.fillRect(x-72,top,144,10);
        g.setFill(fighter.retreating() ? Color.ORANGE : Color.web("#c95567"));
        g.fillRect(x-72,top,144.0*fighter.enemy.getHealth()/fighter.enemy.getMaxHealth(),10);
        g.setFont(Font.font(15)); g.setFill(Color.web("#fff0cf"));
        g.fillText(profile.name()+(fighter.summoned ? " (призыв)" : "")+(profile.attack()==ArenaCatalog.Attack.FINAL?" — фаза "+fighter.phase:""),x-90,top-8);
        g.fillText(fighter.enemy.getHealth()+" / "+fighter.enemy.getMaxHealth(),x-50,top+28);
        g.setFill(fighter.windup>0 ? Color.YELLOW : Color.LIGHTGRAY);
        g.fillText(fighter.action,x-100,top+46);
        } else if(!profile.boss() && fighter.enemy.getHealth()<fighter.enemy.getMaxHealth()) {
            g.setFill(Color.rgb(20,10,22,.8));g.fillRect(x-35,y-height-9,70,5);
            g.setFill(Color.web("#c75a69"));g.fillRect(x-35,y-height-9,70.0*fighter.enemy.getHealth()/fighter.enemy.getMaxHealth(),5);
        }
        if (fighter.windup>0) {
            g.setStroke(Color.ORANGE); g.setLineWidth(2); g.strokeOval(x-60,y-15,120,25);
            if(!campaign) {
            double total=fighter.animationTime+fighter.windup;
            g.setFill(Color.rgb(15,10,20,.9));g.fillRect(x-60,top+53,120,6);
            g.setFill(fighter.heavy?Color.ORANGERED:Color.GOLD);g.fillRect(x-60,top+53,120*Math.min(1,fighter.animationTime/total),6);
            }
            if(fighter.animation.equals("attack_quick")||fighter.animation.equals("attack_heavy")) {
                g.setStroke(fighter.heavy?Color.ORANGERED:Color.GOLD);g.setLineWidth(4);
                g.strokeArc(x-115,y-140,230,180,fighter.facingLeft?110:-70,140,javafx.scene.shape.ArcType.OPEN);
            }
        }
    }

    private void drawProjectile(GraphicsContext g,ArenaSimulation.Projectile shot) {
        if (shot.fromPlayer) {
            double angle=Math.toDegrees(Math.atan2(shot.vy,shot.vx));
            g.save();
            g.translate(shot.x,shot.y);
            g.rotate(angle);
            if (shot.source.equals("player-mage")) {
                g.setFill(Color.rgb(255,150,45,.24));g.fillOval(-25,-25,50,50);
                g.setFill(Color.ORANGERED);g.fillOval(-13,-13,26,26);
                g.setFill(Color.YELLOW);g.fillOval(-7,-7,14,14);
            } else {
                g.setStroke(Color.web("#f2d08a"));g.setLineWidth(4);g.strokeLine(-24,0,17,0);
                g.setFill(Color.web("#e7b35c"));
                g.fillPolygon(new double[]{24,8,8},new double[]{0,-9,9},3);
            }
            g.restore();
            return;
        }
        String path=switch (shot.source) {
            case "player-archer" -> "/assets/enemies/1 Р­С‚Р°Рї/Archer/Arrow.png";
            case "player-mage" -> "/assets/enemies/3 Р­С‚Р°Рї/fire_mag/fireball_fly.png";
            case "archer" -> "1 Этап/Archer/Arrow.png";
            case "guardian" -> "1 Этап/Rune_Guardian(miniBoss)/rune_guardian_projectile.png";
            case "elemental" -> "2 Этап/ice_elemental/ice_snowball.png";
            case "ice-golem" -> "2 Этап/ice_golem(miniBoss)/ice_golem_projectile_"+(shot.radius>20 ? "big" : "small")+".png";
            case "mage" -> "3 Этап/fire_mag/fireball_fly.png";
            case "doctor" -> "4 Этап/plague_mage/plague_mage_projectile.png";
            case "witch" -> "4 Этап/Queen(miniBoss)/poisonous_sting.png";
            default -> "";
        };
        if (shot.source.equals("player-archer")) path="1 \u042d\u0442\u0430\u043f/Archer/Arrow.png";
        if (shot.source.equals("player-mage")) path="3 \u042d\u0442\u0430\u043f/fire_mag/fireball_fly.png";
        Image sprite=path.isEmpty() ? null : image(path);
        if (sprite==null) {
            g.setFill(shot.kind==ArenaCatalog.Attack.POISON || shot.kind==ArenaCatalog.Attack.WITCH ? Color.YELLOWGREEN : Color.MEDIUMPURPLE);
            g.fillOval(shot.x-shot.radius,shot.y-shot.radius,shot.radius*2,shot.radius*2);
        } else {
            // All supplied projectiles point left in their source artwork.
            double nativeAngle=180;
            g.save(); g.translate(shot.x,shot.y); g.rotate(Math.toDegrees(Math.atan2(shot.vy,shot.vx))-nativeAngle);
            int frames=shot.source.equals("mage") || shot.source.equals("player-mage") || shot.source.equals("witch") ? 4 : 1;
            double frameWidth=sprite.getWidth()/frames;
            double scale=shot.source.equals("archer") ? .6 : 1;
            double h=Math.max(16,shot.radius*1.5)*scale, w=h*frameWidth/sprite.getHeight();
            g.drawImage(sprite,((int)(time*10)%frames)*frameWidth,0,frameWidth,sprite.getHeight(),-w/2,-h/2,w,h);
            g.restore();
        }
    }

    /** Player shots use a canvas above the hero so the muzzle and projectile stay visible. */
    private void drawPlayerProjectiles() {
        GraphicsContext g=playerProjectileCanvas.getGraphicsContext2D();
        g.clearRect(0,0,W,H);
        for(ArenaSimulation.Projectile shot:simulation.projectiles()) if(shot.fromPlayer) drawProjectile(g,shot);
    }
}
