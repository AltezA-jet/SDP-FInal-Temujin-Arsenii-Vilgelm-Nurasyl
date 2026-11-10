package com.gamelab.ui.character;

import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.progression.Difficulty;
import com.gamelab.ui.components.MenuButton;
import com.gamelab.ui.components.MaleWarriorSprite;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Ellipse;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import java.util.function.Consumer;
import java.util.function.BiConsumer;

/** SHADOWFALL character forge. Empty framed slots are deliberate insertion points for future sprites. */
public final class CharacterCreationScreen extends Pane {
    public static final double W = 1920, H = 1080;
    private static final double PREVIEW_SCREEN_W = 1909.0;
    private static final double PREVIEW_ANCHOR_X = W * 960.0 / PREVIEW_SCREEN_W;
    private static final double PREVIEW_ANCHOR_Y = H * 658.0 / 1080.0;
    private static final double PREVIEW_SIZE = H * 190.0 / 1080.0;
    private static final double PREVIEW_WIDTH = PREVIEW_SIZE * 64.0 / 96.0;
    private static final String[] CLASSES = {"WARRIOR", "ARCHER", "MAGE", "ASSASSIN"};
    private static final String[] CLASS_NAMES = {"Воин", "Лучник", "Маг", "Ассасин"};
    private static final String[] CLASS_INFO = {
            "Несокрушимый боец передовой. Высокая защита и мощные удары по площади; медлителен в движении и атаке.",
            "Смертоносен с луком: высокая точность и скорострельность. Слаб с прочим оружием, полагается на дистанцию.",
            "Повелевает заклинаниями. Магический урон растёт вместе с запасом маны; физическая защита низкая.",
            "Молниеносные выпады и уклонения. Быстр, но хрупок; может переносить меньше предметов."
    };
    private static final String[][] STAT_NAMES = {
            {"Здоровье", "Защита", "Скорость", "Скорость атаки", "Урон", "Вместимость"},
            {"Здоровье", "Защита", "Скорость", "Скорострельность", "Точность", "Владение луком"},
            {"Здоровье", "Защита", "Скорость", "Мана", "Магический урон", "Сила чар"},
            {"Здоровье", "Защита", "Скорость", "Скорость атаки", "Уклонение", "Вместимость"}
    };
    private static final int[][] STATS = {
            {82, 92, 34, 38, 88, 76}, {66, 48, 70, 88, 94, 90},
            {58, 32, 54, 96, 94, 80}, {62, 30, 98, 94, 90, 42}
    };
    private static final Color[] HAIR_COLORS = {Color.web("#30202a"), Color.web("#70412c"), Color.web("#bd6238"), Color.web("#e3c078"), Color.web("#3d78a8"), Color.web("#71428a"), Color.web("#b94f82")};
    private static final Color[] OUTFIT_COLORS = {Color.web("#273f87"), Color.web("#74452e"), Color.web("#426b48"), Color.web("#777985"), Color.web("#67417a")};
    private static final String[] COLOR_NAMES = {"ЧЁРНЫЙ", "КАШТАН", "РЫЖИЙ", "БЛОНД", "СИНИЙ", "ФИОЛЕТОВЫЙ", "РОЗОВЫЙ"};
    private static final String[] OUTFIT_COLOR_NAMES = {"СИНИЙ", "КОРИЧНЕВЫЙ", "ЗЕЛЁНЫЙ", "СЕРЫЙ", "ФИОЛЕТОВЫЙ"};
    private static final Color GOLD = Color.web("#f2c66d"), DIM = Color.web("#9c784e"), TEXT = Color.web("#dfd4c1");

    private final BiConsumer<PlayerCharacter, Difficulty> onCreate;
    private final Runnable onBack;
    private final Canvas scene = new Canvas(W, H), fx = new Canvas(W, H);
    private final MaleWarriorSprite warriorPreview = new MaleWarriorSprite(PREVIEW_SIZE);
    private final Ellipse previewShadow = new Ellipse(45, 9);
    private final MenuButton[] classButtons = new MenuButton[4], difficultyButtons = new MenuButton[3], colorButtons = new MenuButton[7], shirtButtons = new MenuButton[5], pantsButtons = new MenuButton[5], bootsButtons = new MenuButton[5], headwearButtons = new MenuButton[6];
    private final java.util.List<MenuButton> menuButtons = new java.util.ArrayList<>();
    private final Label description = new Label(), stats = new Label(), notice = new Label();
    private final Label hairColorValue = new Label(), shirtColorValue = new Label(), pantsColorValue = new Label(), bootsColorValue = new Label(), headwearColorValue = new Label();
    private final TextField name = new TextField("Герой");
    private int selectedClass, selectedDifficulty = 1, color, shirtColor, pantsColor, bootsColor, headwear = -1;
    private double time;
    private long last;
    private final AnimationTimer animation = new AnimationTimer() {
        @Override public void handle(long now) {
            if (last != 0) time += Math.min(.08, (now - last) / 1e9);
            last = now;
            drawFx();
            for (MenuButton button : menuButtons) button.update(.016, time);
        }
    };

    public CharacterCreationScreen(Consumer<PlayerCharacter> onCreate, Runnable onBack) {
        this((player, difficulty) -> onCreate.accept(player), onBack);
    }

    public CharacterCreationScreen(BiConsumer<PlayerCharacter, Difficulty> onCreate, Runnable onBack) {
        this.onCreate = (player, difficulty) -> { player.setAppearance(color, shirtColor, pantsColor, bootsColor, headwear); animation.stop(); onCreate.accept(player, difficulty); }; this.onBack = onBack;
        setPrefSize(W, H); setMinSize(W, H); setMaxSize(W, H); setClip(new Rectangle(W, H));
        drawWorld(); fx.setMouseTransparent(true); getChildren().addAll(scene, fx);
        build(); refresh(); animation.start();
    }

    private void build() {
        // Title and subtle framework
        Label title = label("THE FORGE OF SHADOWFALL", 34, "#f4d38b"); title.setLayoutX(690); title.setLayoutY(32); title.setPrefWidth(540); title.setAlignment(Pos.CENTER);
        Label subtitle = label("CREATE YOUR HERO", 13, "#c49b69"); subtitle.setLayoutX(820); subtitle.setLayoutY(76); subtitle.setPrefWidth(280); subtitle.setAlignment(Pos.CENTER);
        getChildren().addAll(title, subtitle);
        line(710, 101, 500, .55); line(708, 102, 2, .55); line(1210, 102, 2, .55);

        panel(72, 148, 414, 800, "I     CLASS SELECTION");
        panel(1434, 148, 414, 800, "II     APPEARANCE");

        Label classHint = label("CHOOSE YOUR PATH", 13, "#b69b72"); classHint.setLayoutX(102); classHint.setLayoutY(223); getChildren().add(classHint);
        for (int i=0;i<4;i++) {
            final int n=i; MenuButton b=imageButton("/assets/ui/buttons/labels/label_"+CLASSES[i].toLowerCase()+".png", ()->{selectedClass=n;refresh();},356,60);
            b.setLayoutX(101); b.setLayoutY(258+i*77); classButtons[i]=b; getChildren().add(b);
        }
        divider(101, 586, 356);
        Label lore = label("PATHFINDER'S RECORD", 13, "#e1bd7c"); lore.setLayoutX(101); lore.setLayoutY(607); getChildren().add(lore);
        description.setWrapText(true); description.setLayoutX(101); description.setLayoutY(642); description.setPrefSize(350,76);
        description.setStyle("-fx-text-fill:#d5c8b2;-fx-font-size:15px;-fx-line-spacing:3px;"); getChildren().add(description);
        stats.setLayoutX(101); stats.setLayoutY(741); stats.setPrefWidth(356); stats.setStyle("-fx-font-size:13px;"); getChildren().add(stats);

        // Central rune plinth is part of the world illustration, with a deliberately empty sprite bay.
        Label preview = label("CHARACTER PREVIEW", 22, "#c5a5ef"); preview.setLayoutX(710); preview.setLayoutY(380); preview.setPrefWidth(500); preview.setAlignment(Pos.CENTER);
        Label previewSub = label("SPRITE SOCKET  //  AWAITING SOUL", 12, "#8c78a8"); previewSub.setLayoutX(760); previewSub.setLayoutY(410); previewSub.setPrefWidth(400); previewSub.setAlignment(Pos.CENTER);
        getChildren().addAll(preview, previewSub);
        previewShadow.setMouseTransparent(true);
        previewShadow.setFill(new RadialGradient(
                0, 0, .5, .5, .5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(0, 0, 0, .38)),
                new Stop(1, Color.TRANSPARENT)));
        previewShadow.relocate(PREVIEW_ANCHOR_X - 45, PREVIEW_ANCHOR_Y - 9);
        getChildren().add(previewShadow);

        // The preview is anchored by the centre of the feet, equivalent to translate(-50%, -100%).
        warriorPreview.relocate(PREVIEW_ANCHOR_X, PREVIEW_ANCHOR_Y);
        warriorPreview.setTranslateX(-PREVIEW_WIDTH / 2);
        warriorPreview.setTranslateY(-PREVIEW_SIZE);
        getChildren().add(warriorPreview);
        Label centerNote = label("THE VEIL OPENS TO THOSE WHO CHOOSE", 13, "#c09a62"); centerNote.setLayoutX(690); centerNote.setLayoutY(868); centerNote.setPrefWidth(540); centerNote.setAlignment(Pos.CENTER); getChildren().add(centerNote);

        Label difficultyTitle=label("DIFFICULTY",14,"#edcf8c");difficultyTitle.setLayoutX(1464);difficultyTitle.setLayoutY(221);getChildren().add(difficultyTitle);
        String[] difficulties={"EASY","NORMAL","HARD"};
        for(int i=0;i<3;i++){final int n=i;MenuButton b=slot(()->{selectedDifficulty=n;refresh();},104,47);b.setText(difficulties[i]);b.setLayoutX(1464+i*119);b.setLayoutY(248);difficultyButtons[i]=b;getChildren().add(b);}

        appearanceCard(1464, 310, 354, 88);
        Label colors=label("01  ВОЛОСЫ",13,"#edcf8c");colors.setLayoutX(1480);colors.setLayoutY(322);getChildren().add(colors);
        configureValueLabel(hairColorValue,1674,318,124);getChildren().add(hairColorValue);
        for(int i=0;i<7;i++){final int n=i;MenuButton sw=colorSlot(()->{color=n;refresh();},42,38,HAIR_COLORS[i]);sw.setLayoutX(1480+i*47);sw.setLayoutY(348);colorButtons[i]=sw;getChildren().add(sw);}

        appearanceCard(1464, 408, 354, 88);
        Label shirts=label("02  ВЕРХНЯЯ ОДЕЖДА",13,"#edcf8c");shirts.setLayoutX(1480);shirts.setLayoutY(420);getChildren().add(shirts);
        configureValueLabel(shirtColorValue,1674,416,124);getChildren().add(shirtColorValue);
        for(int i=0;i<5;i++){final int n=i;MenuButton sw=colorSlot(()->{shirtColor=n;refresh();},50,40,OUTFIT_COLORS[i]);sw.setLayoutX(1480+i*62);sw.setLayoutY(447);shirtButtons[i]=sw;getChildren().add(sw);}

        appearanceCard(1464, 506, 354, 88);
        Label pants=label("03  БРЮКИ",13,"#edcf8c");pants.setLayoutX(1480);pants.setLayoutY(518);getChildren().add(pants);
        configureValueLabel(pantsColorValue,1674,514,124);getChildren().add(pantsColorValue);
        for(int i=0;i<5;i++){final int n=i;MenuButton sw=colorSlot(()->{pantsColor=n;refresh();},50,40,OUTFIT_COLORS[i]);sw.setLayoutX(1480+i*62);sw.setLayoutY(545);pantsButtons[i]=sw;getChildren().add(sw);}

        appearanceCard(1464, 604, 354, 88);
        Label boots=label("04  БОТИНКИ",13,"#edcf8c");boots.setLayoutX(1480);boots.setLayoutY(616);getChildren().add(boots);
        configureValueLabel(bootsColorValue,1674,612,124);getChildren().add(bootsColorValue);
        for(int i=0;i<5;i++){final int n=i;MenuButton sw=colorSlot(()->{bootsColor=n;refresh();},50,40,OUTFIT_COLORS[i]);sw.setLayoutX(1480+i*62);sw.setLayoutY(643);bootsButtons[i]=sw;getChildren().add(sw);}

        appearanceCard(1464, 702, 354, 105);
        Label headwearTitle=label("05  ГОЛОВНОЙ УБОР",13,"#edcf8c");headwearTitle.setLayoutX(1480);headwearTitle.setLayoutY(714);getChildren().add(headwearTitle);
        configureValueLabel(headwearColorValue,1674,710,124);getChildren().add(headwearColorValue);
        MenuButton noHat = new MenuButton("БЕЗ УБОРА", 104, 44);
        noHat.setOnAction(() -> { headwear = -1; refresh(); });
        noHat.setLayoutX(1480); noHat.setLayoutY(744);
        headwearButtons[0] = noHat; menuButtons.add(noHat); getChildren().add(noHat);
        for(int i=0;i<5;i++){final int n=i;MenuButton sw=colorSlot(()->{headwear=n;refresh();},40,44,OUTFIT_COLORS[i]);sw.setLayoutX(1594+i*43);sw.setLayoutY(744);headwearButtons[i+1]=sw;getChildren().add(sw);}

        Label nameLabel=label("ИМЯ ГЕРОЯ",14,"#edcf8c");nameLabel.setLayoutX(1464);nameLabel.setLayoutY(824);getChildren().add(nameLabel);
        name.setLayoutX(1464);name.setLayoutY(850);name.setPrefSize(354,48);name.setPromptText("Введите имя");
        name.setStyle("-fx-background-color:#100e1b;-fx-text-fill:#f2dfb6;-fx-prompt-text-fill:#806b54;-fx-font-size:17px;-fx-border-color:#9b7040;-fx-border-width:1.5;-fx-background-radius:0;-fx-border-radius:0;");getChildren().add(name);

        MenuButton back=imageButton("/assets/ui/buttons/labels/label_back_to_menu.png",()->{animation.stop();onBack.run();},240,62);back.setLayoutX(685);back.setLayoutY(967);getChildren().add(back);
        MenuButton create=imageButton("/assets/ui/buttons/labels/label_create_character.png",this::createCharacter,265,62);create.setLayoutX(970);create.setLayoutY(967);getChildren().add(create);
        notice.setLayoutX(640);notice.setLayoutY(1035);notice.setPrefWidth(640);notice.setAlignment(Pos.CENTER);notice.setStyle("-fx-text-fill:#efcf84;-fx-font-size:13px;");getChildren().add(notice);
    }

    private void drawWorld() {
        GraphicsContext g=scene.getGraphicsContext2D();
        javafx.scene.image.Image background = new javafx.scene.image.Image(CharacterCreationScreen.class.getResource("/assets/ui/backgrounds/character_creation_bg.jpg").toExternalForm());
        g.drawImage(background, 0, 0, W, H);
    }

    private void drawFx(){GraphicsContext g=fx.getGraphicsContext2D();g.clearRect(0,0,W,H);double pulse=.55+.35*Math.sin(time*1.4);g.setFill(Color.rgb(173,112,255,.08*pulse));g.fillOval(690,510,540,315);for(int i=0;i<14;i++){double x=620+(i*137%690), y=220+(i*193%650), a=.18+.35*(.5+.5*Math.sin(time*1.7+i));g.setFill(Color.rgb(188,151,255,a));g.fillRect(Math.floor(x/4)*4,Math.floor(y/4)*4,3,3);}for(int i=0;i<6;i++){double x=430+i*212,y=767+(i%2)*15,r=30+4*Math.sin(time*4+i);g.setFill(Color.rgb(255,153,65,.08));g.fillOval(x-r,y-r,r*2,r*2);} }

    private void refresh(){
        for(int i=0;i<4;i++)classButtons[i].setSelected(i==selectedClass);
        for(int i=0;i<3;i++)difficultyButtons[i].setSelected(i==selectedDifficulty);
        for(int i=0;i<7;i++)colorButtons[i].setSelected(i==color);
        for(int i=0;i<5;i++)shirtButtons[i].setSelected(i==shirtColor);
        for(int i=0;i<5;i++)pantsButtons[i].setSelected(i==pantsColor);
        for(int i=0;i<5;i++)bootsButtons[i].setSelected(i==bootsColor);
        for(int i=0;i<6;i++)headwearButtons[i].setSelected(i==headwear+1);
        hairColorValue.setText(COLOR_NAMES[color]);
        shirtColorValue.setText(OUTFIT_COLOR_NAMES[shirtColor]);
        pantsColorValue.setText(OUTFIT_COLOR_NAMES[pantsColor]);
        bootsColorValue.setText(OUTFIT_COLOR_NAMES[bootsColor]);
        headwearColorValue.setText(headwear < 0 ? "БЕЗ УБОРА" : OUTFIT_COLOR_NAMES[headwear]);
        description.setText(CLASS_INFO[selectedClass]);StringBuilder s=new StringBuilder();
        for(int i=0;i<6;i++)s.append(String.format("%-20s %3d%%\n",STAT_NAMES[selectedClass][i],STATS[selectedClass][i]));
        stats.setText(s.toString());
        warriorPreview.setClass(CLASSES[selectedClass]);
        warriorPreview.setConfiguration(color, shirtColor, pantsColor, bootsColor, headwear);
    }

    private void createCharacter(){String n=name.getText()==null?"":name.getText().trim();if(n.isEmpty()){notice.setText("Введите имя героя, прежде чем начать путь.");name.requestFocus();return;}if(n.length()>24){notice.setText("Имя должно содержать не более 24 символов.");return;}PlayerCharacter player=new PlayerCharacter(n,CLASSES[selectedClass],STATS[selectedClass][0]+60,STATS[selectedClass][4]/5,selectedClass==0?12:selectedClass==2?2:3);String hat=headwear<0?"БЕЗ УБОРА":OUTFIT_COLOR_NAMES[headwear];Difficulty difficulty=Difficulty.values()[selectedDifficulty];notice.setText(n+" · "+CLASSES[selectedClass]+" · "+difficulty+" · ВЕРХ: "+OUTFIT_COLOR_NAMES[shirtColor]+" · БРЮКИ: "+OUTFIT_COLOR_NAMES[pantsColor]+" · БОТИНКИ: "+OUTFIT_COLOR_NAMES[bootsColor]+" · УБОР: "+hat);onCreate.accept(player,difficulty);}
    private void panel(double x,double y,double w,double h,String title){Rectangle r=new Rectangle(w,h);r.setLayoutX(x);r.setLayoutY(y);r.setFill(Color.rgb(9,8,18,.88));r.setStroke(Color.web("#9a713f",.9));r.setStrokeWidth(2);r.setEffect(new DropShadow(22,Color.rgb(0,0,0,.65)));getChildren().add(r);Label t=label(title,17,"#f0d18b");t.setLayoutX(x+28);t.setLayoutY(y+25);getChildren().add(t);Rectangle d=new Rectangle(w-56,1,Color.web("#84613f",.7));d.setLayoutX(x+28);d.setLayoutY(y+61);getChildren().add(d);}
    private void divider(double x,double y,double w){Rectangle r=new Rectangle(w,1,Color.web("#936b43",.7));r.setLayoutX(x);r.setLayoutY(y);getChildren().add(r);}
    private void line(double x,double y,double w,double op){Rectangle r=new Rectangle(w,1,Color.web("#d0a85f",op));r.setLayoutX(x);r.setLayoutY(y);getChildren().add(r);}
    private Label label(String text,int size,String color){Label l=new Label(text);l.setTextFill(Color.web(color));l.setFont(Font.font("Serif",size));return l;}
    private MenuButton imageButton(String asset,Runnable action,double width,double height){
        java.net.URL url=CharacterCreationScreen.class.getResource(asset);
        if(url==null)throw new IllegalStateException("Missing character-creation label "+asset);
        MenuButton button=new MenuButton(new javafx.scene.image.Image(url.toExternalForm()),width,height);
        button.setOnAction(action);menuButtons.add(button);return button;
    }
    private MenuButton slot(Runnable action,double width,double height){return slot(action,width,height,null);}
    private MenuButton slot(Runnable action,double width,double height,Color swatch){
        javafx.scene.Node graphic=swatch==null?null:new Rectangle(20,18,swatch);
        MenuButton button=new MenuButton(swatch==null?"✧":"",graphic,width,height);
        button.setOnAction(action);
        menuButtons.add(button);
        return button;
    }
    private MenuButton colorSlot(Runnable action,double width,double height,Color swatch){
        double sampleWidth=Math.max(20,width-20), sampleHeight=Math.max(16,height-18);
        Rectangle sample=new Rectangle(sampleWidth,sampleHeight,swatch);
        sample.setArcWidth(4);sample.setArcHeight(4);
        sample.setStroke(Color.rgb(255,232,183,.45));sample.setStrokeWidth(1);
        Rectangle shine=new Rectangle(sampleWidth-4,2,Color.rgb(255,255,255,.22));
        shine.setMouseTransparent(true);
        StackPane chip=new StackPane(sample,shine);
        StackPane.setAlignment(shine,Pos.TOP_CENTER);
        chip.setEffect(new DropShadow(5,Color.rgb(0,0,0,.7)));
        MenuButton button=new MenuButton("",chip,width,height);
        button.setOnAction(action);menuButtons.add(button);return button;
    }
    private void appearanceCard(double x,double y,double width,double height){
        Rectangle shadow=new Rectangle(width,height,Color.rgb(0,0,0,.42));
        shadow.setLayoutX(x+3);shadow.setLayoutY(y+4);shadow.setArcWidth(10);shadow.setArcHeight(10);
        Rectangle card=new Rectangle(width,height,Color.rgb(15,11,25,.94));
        card.setLayoutX(x);card.setLayoutY(y);card.setStroke(Color.web("#a4743d",.9));card.setStrokeWidth(1.4);card.setArcWidth(10);card.setArcHeight(10);
        Rectangle accent=new Rectangle(4,height-18,Color.web("#d59a4a",.85));
        accent.setLayoutX(x+7);accent.setLayoutY(y+9);accent.setArcWidth(4);accent.setArcHeight(4);
        Rectangle separator=new Rectangle(width-32,1,Color.web("#745133",.72));
        separator.setLayoutX(x+16);separator.setLayoutY(y+35);
        getChildren().addAll(shadow,card,accent,separator);
    }
    private void configureValueLabel(Label value,double x,double y,double width){
        value.setLayoutX(x);value.setLayoutY(y);value.setPrefWidth(width);value.setAlignment(Pos.CENTER_RIGHT);
        value.setPadding(new javafx.geometry.Insets(3,8,3,8));
        value.setTextFill(Color.web("#d8c0ef"));
        value.setStyle("-fx-font-family:'Monospaced';-fx-font-size:10px;-fx-font-weight:bold;"
                + "-fx-background-color:#21182d;-fx-border-color:#694d39;-fx-border-width:1;"
                + "-fx-background-radius:5;-fx-border-radius:5;");
    }
    public void stop(){animation.stop();}
}
