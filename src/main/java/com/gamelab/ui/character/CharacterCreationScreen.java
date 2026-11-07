package com.gamelab.ui.character;

import com.gamelab.domain.PlayerCharacter;
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
import javafx.scene.text.Font;

import java.util.function.Consumer;

/** SHADOWFALL character forge. Empty framed slots are deliberate insertion points for future sprites. */
public final class CharacterCreationScreen extends Pane {
    public static final double W = 1920, H = 1080;
    private static final double PREVIEW_SCREEN_W = 1909.0;
    private static final double PREVIEW_ANCHOR_X = W * 960.0 / PREVIEW_SCREEN_W;
    private static final double PREVIEW_ANCHOR_Y = H * 658.0 / 1080.0;
    private static final double PREVIEW_SIZE = H * 190.0 / 1080.0;
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
    private static final Color[] HAIR_COLORS = {Color.web("#30202a"), Color.web("#70412c"), Color.web("#bd7540"), Color.web("#e3c078"), Color.web("#d8d4dc"), Color.web("#843b55"), Color.web("#71428a")};
    private static final String[] COLOR_NAMES = {"ЭБЕН", "КАШТАН", "МЕДЬ", "ЗОЛОТО", "ПЕПЕЛ", "ВИНО", "РУНА"};
    private static final Color GOLD = Color.web("#f2c66d"), DIM = Color.web("#9c784e"), TEXT = Color.web("#dfd4c1");

    private final Consumer<PlayerCharacter> onCreate;
    private final Runnable onBack;
    private final Canvas scene = new Canvas(W, H), fx = new Canvas(W, H);
    private final MaleWarriorSprite warriorPreview = new MaleWarriorSprite(PREVIEW_SIZE);
    private final Ellipse previewShadow = new Ellipse(45, 9);
    private final MenuButton[] classButtons = new MenuButton[4], genderButtons = new MenuButton[2], hairButtons = new MenuButton[4], colorButtons = new MenuButton[7], outfitButtons = new MenuButton[4], headwearButtons = new MenuButton[5];
    private final java.util.List<MenuButton> menuButtons = new java.util.ArrayList<>();
    private final Label description = new Label(), stats = new Label(), notice = new Label();
    private final TextField name = new TextField("Герой");
    private final Label[] hairCaptions = new Label[4], outfitCaptions = new Label[4];
    private int selectedClass, gender, hair, color, outfit, headwear = -1;
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
        this.onCreate = player -> { player.setAppearance(color, outfit, headwear); animation.stop(); onCreate.accept(player); }; this.onBack = onBack;
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
        warriorPreview.setTranslateX(-warriorPreview.prefWidth(-1) / 2);
        warriorPreview.setTranslateY(-PREVIEW_SIZE);
        getChildren().add(warriorPreview);
        Label centerNote = label("THE VEIL OPENS TO THOSE WHO CHOOSE", 13, "#c09a62"); centerNote.setLayoutX(690); centerNote.setLayoutY(868); centerNote.setPrefWidth(540); centerNote.setAlignment(Pos.CENTER); getChildren().add(centerNote);

        Label genderTitle=label("GENDER",14,"#edcf8c");genderTitle.setLayoutX(1464);genderTitle.setLayoutY(221);getChildren().add(genderTitle);
        String[] genders={"MALE","FEMALE"};
        for(int i=0;i<2;i++){final int n=i;MenuButton b=imageButton("/assets/ui/buttons/labels/label_"+genders[i].toLowerCase()+".png",()->{gender=n;hair=0;outfit=0;headwear=-1;refresh();},170,47);b.setLayoutX(1464+i*185);b.setLayoutY(248);genderButtons[i]=b;getChildren().add(b);}
        Label hairstyle=label("HAIRSTYLE   ·   EMPTY SPRITE SLOTS",14,"#edcf8c");hairstyle.setLayoutX(1464);hairstyle.setLayoutY(321);getChildren().add(hairstyle);
        String[][] hairNames={{"CROP","WAVES","BRAID","TIED"},{"BOB","LENGTH","BRAID","TIED"}};
        for(int i=0;i<4;i++){final int n=i;MenuButton slot=slot(()->{hair=n;refresh();},72,80);slot.setLayoutX(1464+i*88);slot.setLayoutY(352);hairButtons[i]=slot;getChildren().add(slot);Label cap=label(hairNames[gender][i],9,"#b9a98e");cap.setLayoutX(1460+i*88);cap.setLayoutY(438);cap.setPrefWidth(80);cap.setAlignment(Pos.CENTER);hairCaptions[i]=cap;getChildren().add(cap);}
        Label headwearTitle=label("HEADWEAR",14,"#edcf8c");headwearTitle.setLayoutX(1464);headwearTitle.setLayoutY(462);getChildren().add(headwearTitle);
        for(int i=0;i<5;i++){final int n=i;MenuButton hat=slot(()->{headwear=n;refresh();},54,34);hat.setLayoutX(1464+i*68);hat.setLayoutY(480);headwearButtons[i]=hat;getChildren().add(hat);}
        Label colors=label("HAIR COLOR",14,"#edcf8c");colors.setLayoutX(1464);colors.setLayoutY(528);getChildren().add(colors);
        for(int i=0;i<7;i++){final int n=i;MenuButton sw=slot(()->{color=n;refresh();},38,34,HAIR_COLORS[i]);sw.setLayoutX(1464+i*52);sw.setLayoutY(550);colorButtons[i]=sw;getChildren().add(sw);}
        Label outfits=label("OUTFIT COLOR",14,"#edcf8c");outfits.setLayoutX(1464);outfits.setLayoutY(605);getChildren().add(outfits);
        String[][] outfitNames={{"BLUE","BROWN","GREEN","GREY"},{"BLUE","BROWN","GREEN","GREY"}};
        for(int i=0;i<4;i++){final int n=i;MenuButton slot=slot(()->{outfit=n;refresh();},72,76);slot.setLayoutX(1464+i*88);slot.setLayoutY(637);outfitButtons[i]=slot;getChildren().add(slot);Label cap=label(outfitNames[gender][i],9,"#b9a98e");cap.setLayoutX(1460+i*88);cap.setLayoutY(719);cap.setPrefWidth(80);cap.setAlignment(Pos.CENTER);outfitCaptions[i]=cap;getChildren().add(cap);}
        Label nameLabel=label("HERO NAME",14,"#edcf8c");nameLabel.setLayoutX(1464);nameLabel.setLayoutY(765);getChildren().add(nameLabel);
        name.setLayoutX(1464);name.setLayoutY(793);name.setPrefSize(354,48);name.setPromptText("Enter a name");
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
        for(int i=0;i<2;i++)genderButtons[i].setSelected(i==gender);
        String[] hairSet=gender==0?new String[]{"CROP","WAVES","BRAID","TIED"}:new String[]{"BOB","LENGTH","BRAID","TIED"};
        String[] outfitSet=gender==0?new String[]{"TRAVEL","LEATHER","GUARD","REGAL"}:new String[]{"TRAVEL","MANTLE","WARDEN","COURT"};
        for(int i=0;i<4;i++){hairButtons[i].setSelected(i==hair);outfitButtons[i].setSelected(i==outfit);hairCaptions[i].setText(hairSet[i]);outfitCaptions[i].setText(outfitSet[i]);}
        for(int i=0;i<7;i++)colorButtons[i].setSelected(i==color);
        for(int i=0;i<5;i++)headwearButtons[i].setSelected(i==headwear);
        description.setText(CLASS_INFO[selectedClass]);StringBuilder s=new StringBuilder();
        for(int i=0;i<6;i++)s.append(String.format("%-20s %3d%%\n",STAT_NAMES[selectedClass][i],STATS[selectedClass][i]));
        stats.setText(s.toString());
        warriorPreview.setConfiguration(color, outfit, headwear);
    }

    private void createCharacter(){String n=name.getText()==null?"":name.getText().trim();if(n.isEmpty()){notice.setText("Введите имя героя, прежде чем начать путь.");name.requestFocus();return;}if(n.length()>24){notice.setText("Имя должно содержать не более 24 символов.");return;}PlayerCharacter player=new PlayerCharacter(n,CLASS_NAMES[selectedClass],STATS[selectedClass][0]+60,STATS[selectedClass][4]/5,selectedClass==0?12:selectedClass==2?2:3);String[] hairs=gender==0?new String[]{"CROP","WAVES","BRAID","TIED"}:new String[]{"BOB","LENGTH","BRAID","TIED"};String[] outfits=gender==0?new String[]{"TRAVEL","LEATHER","GUARD","REGAL"}:new String[]{"TRAVEL","MANTLE","WARDEN","COURT"};notice.setText(n+"  ·  "+CLASSES[selectedClass]+"  ·  "+(gender==0?"MALE":"FEMALE")+"  ·  "+hairs[hair]+" / "+COLOR_NAMES[color]+" / "+outfits[outfit]);onCreate.accept(player);}
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
    public void stop(){animation.stop();}
}
