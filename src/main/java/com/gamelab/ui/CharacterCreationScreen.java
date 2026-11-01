package com.gamelab.ui;

import com.gamelab.domain.PlayerCharacter;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.util.function.Consumer;

/** SHADOWFALL character forge. Empty framed slots are deliberate insertion points for future sprites. */
public final class CharacterCreationScreen extends Pane {
    public static final double W = 1920, H = 1080;
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
    private final Button[] classButtons = new Button[4], genderButtons = new Button[2], hairButtons = new Button[4], colorButtons = new Button[7], outfitButtons = new Button[4];
    private final Label description = new Label(), stats = new Label(), notice = new Label();
    private final TextField name = new TextField("Герой");
    private final Label[] hairCaptions = new Label[4], outfitCaptions = new Label[4];
    private int selectedClass, gender, hair, color, outfit;
    private double time;
    private long last;
    private final AnimationTimer animation = new AnimationTimer() {
        @Override public void handle(long now) {
            if (last != 0) time += Math.min(.08, (now - last) / 1e9);
            last = now;
            drawFx();
        }
    };

    public CharacterCreationScreen(Consumer<PlayerCharacter> onCreate, Runnable onBack) {
        this.onCreate = onCreate; this.onBack = onBack;
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
            final int n=i; Button b=button(CLASSES[i], ()->{selectedClass=n;refresh();});
            b.setLayoutX(101); b.setLayoutY(258+i*77); b.setPrefSize(356,60); classButtons[i]=b; getChildren().add(b);
        }
        divider(101, 586, 356);
        Label lore = label("PATHFINDER'S RECORD", 13, "#e1bd7c"); lore.setLayoutX(101); lore.setLayoutY(607); getChildren().add(lore);
        description.setWrapText(true); description.setLayoutX(101); description.setLayoutY(642); description.setPrefSize(350,76);
        description.setStyle("-fx-text-fill:#d5c8b2;-fx-font-size:15px;-fx-line-spacing:3px;"); getChildren().add(description);
        stats.setLayoutX(101); stats.setLayoutY(741); stats.setPrefWidth(356); stats.setStyle("-fx-font-size:13px;"); getChildren().add(stats);

        // Central rune plinth is part of the world illustration, with a deliberately empty sprite bay.
        Label preview = label("CHARACTER PREVIEW", 22, "#c5a5ef"); preview.setLayoutX(710); preview.setLayoutY(492); preview.setPrefWidth(500); preview.setAlignment(Pos.CENTER);
        Label previewSub = label("SPRITE SOCKET  //  AWAITING SOUL", 12, "#8c78a8"); previewSub.setLayoutX(760); previewSub.setLayoutY(526); previewSub.setPrefWidth(400); previewSub.setAlignment(Pos.CENTER);
        getChildren().addAll(preview, previewSub);
        Label centerNote = label("THE VEIL OPENS TO THOSE WHO CHOOSE", 13, "#c09a62"); centerNote.setLayoutX(690); centerNote.setLayoutY(868); centerNote.setPrefWidth(540); centerNote.setAlignment(Pos.CENTER); getChildren().add(centerNote);

        Label genderTitle=label("GENDER",14,"#edcf8c");genderTitle.setLayoutX(1464);genderTitle.setLayoutY(221);getChildren().add(genderTitle);
        String[] genders={"MALE","FEMALE"};
        for(int i=0;i<2;i++){final int n=i;Button b=button(genders[i],()->{gender=n;hair=0;outfit=0;refresh();});b.setLayoutX(1464+i*185);b.setLayoutY(248);b.setPrefSize(170,47);genderButtons[i]=b;getChildren().add(b);}
        Label hairstyle=label("HAIRSTYLE   ·   EMPTY SPRITE SLOTS",14,"#edcf8c");hairstyle.setLayoutX(1464);hairstyle.setLayoutY(321);getChildren().add(hairstyle);
        String[][] hairNames={{"CROP","WAVES","BRAID","TIED"},{"BOB","LENGTH","BRAID","TIED"}};
        for(int i=0;i<4;i++){final int n=i;Button slot=slot(()->{hair=n;refresh();});slot.setLayoutX(1464+i*88);slot.setLayoutY(352);slot.setPrefSize(72,80);hairButtons[i]=slot;getChildren().add(slot);Label cap=label(hairNames[gender][i],9,"#b9a98e");cap.setLayoutX(1460+i*88);cap.setLayoutY(438);cap.setPrefWidth(80);cap.setAlignment(Pos.CENTER);hairCaptions[i]=cap;getChildren().add(cap);}
        Label colors=label("HAIR COLOR",14,"#edcf8c");colors.setLayoutX(1464);colors.setLayoutY(482);getChildren().add(colors);
        for(int i=0;i<7;i++){final int n=i;Button sw=slot(()->{color=n;refresh();});sw.setLayoutX(1464+i*52);sw.setLayoutY(509);sw.setPrefSize(38,34);sw.setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(HAIR_COLORS[i],javafx.scene.layout.CornerRadii.EMPTY,javafx.geometry.Insets.EMPTY)));colorButtons[i]=sw;getChildren().add(sw);}
        Label outfits=label("OUTFIT   ·   EMPTY SPRITE SLOTS",14,"#edcf8c");outfits.setLayoutX(1464);outfits.setLayoutY(575);getChildren().add(outfits);
        String[][] outfitNames={{"TRAVEL","LEATHER","GUARD","REGAL"},{"TRAVEL","LEATHER","GUARD","REGAL"}};
        for(int i=0;i<4;i++){final int n=i;Button slot=slot(()->{outfit=n;refresh();});slot.setLayoutX(1464+i*88);slot.setLayoutY(607);slot.setPrefSize(72,76);outfitButtons[i]=slot;getChildren().add(slot);Label cap=label(outfitNames[gender][i],9,"#b9a98e");cap.setLayoutX(1460+i*88);cap.setLayoutY(689);cap.setPrefWidth(80);cap.setAlignment(Pos.CENTER);outfitCaptions[i]=cap;getChildren().add(cap);}
        Label nameLabel=label("HERO NAME",14,"#edcf8c");nameLabel.setLayoutX(1464);nameLabel.setLayoutY(735);getChildren().add(nameLabel);
        name.setLayoutX(1464);name.setLayoutY(763);name.setPrefSize(354,48);name.setPromptText("Enter a name");
        name.setStyle("-fx-background-color:#100e1b;-fx-text-fill:#f2dfb6;-fx-prompt-text-fill:#806b54;-fx-font-size:17px;-fx-border-color:#9b7040;-fx-border-width:1.5;-fx-background-radius:0;-fx-border-radius:0;");getChildren().add(name);

        Button back=button("‹   BACK TO MENU",()->{animation.stop();onBack.run();});back.setLayoutX(685);back.setLayoutY(967);back.setPrefSize(240,62);getChildren().add(back);
        Button create=button("CREATE CHARACTER",this::createCharacter);create.setLayoutX(970);create.setLayoutY(967);create.setPrefSize(265,62);getChildren().add(create);
        notice.setLayoutX(640);notice.setLayoutY(1035);notice.setPrefWidth(640);notice.setAlignment(Pos.CENTER);notice.setStyle("-fx-text-fill:#efcf84;-fx-font-size:13px;");getChildren().add(notice);
    }

    private void drawWorld() {
        GraphicsContext g=scene.getGraphicsContext2D();
        g.setFill(Color.web("#090817"));g.fillRect(0,0,W,H);
        g.setFill(Color.web("#17102c"));g.fillOval(500,100,930,800);
        // distant cavern arches and broken pillars
        g.setFill(Color.web("#100d22"));g.fillPolygon(new double[]{0,0,180,230,270,340,410,465,520,0},new double[]{0,800,550,250,550,310,590,390,820,1080},10);
        g.fillPolygon(new double[]{1920,1920,1740,1690,1640,1580,1510,1450,1400,1920},new double[]{0,790,520,280,560,330,600,420,850,1080},10);
        for(int i=0;i<4;i++){double x=i<2?35+i*102:1580+(i-2)*112;double y=150+(i%2)*110;double h=440+(i%2)*90;g.setFill(Color.web("#201934"));g.fillRect(x,y,54,h);g.setFill(Color.web("#392a4b"));g.fillRect(x-12,y,78,18);g.fillRect(x-5,y+h-18,66,21);g.setFill(Color.web("#0e0b1b"));g.fillRect(x+9,y+30,7,h-52);}
        // floor
        g.setFill(Color.web("#100c1c"));g.fillPolygon(new double[]{0,460,730,1190,1470,1920,1920,0},new double[]{820,700,740,740,700,820,1080,1080},8);
        for(int i=0;i<48;i++){double x=i*43;double y=814+Math.sin(i*1.7)*13;g.setFill(i%2==0?Color.web("#21172b"):Color.web("#181326"));g.fillRect(x,y,38,10);g.setFill(Color.web("#49354d",.32));g.fillRect(x,y,38,2);}
        // pedestal: carved stone tiers and inlaid golden edges
        g.setFill(Color.web("#171525"));g.fillPolygon(new double[]{690,1230,1310,610},new double[]{752,752,819,819},4);
        g.setFill(Color.web("#342940"));g.fillPolygon(new double[]{610,1310,1278,642},new double[]{819,819,854,854},4);
        g.setFill(Color.web("#665171"));g.fillPolygon(new double[]{642,1278,1245,675},new double[]{854,854,875,875},4);
        g.setStroke(Color.web("#bd8b4c",.78));g.setLineWidth(3);g.strokePolygon(new double[]{690,1230,1310,610},new double[]{752,752,819,819},4);g.strokePolygon(new double[]{642,1278,1245,675},new double[]{854,854,875,875},4);
        g.setFill(Color.web("#151222"));g.fillPolygon(new double[]{785,1135,1190,730},new double[]{575,575,750,750},4);
        g.setFill(Color.web("#271e36"));g.fillPolygon(new double[]{785,1135,1190,730},new double[]{575,575,633,633},4);
        g.setStroke(Color.web("#8c67ac",.65));g.setLineWidth(2);g.strokePolygon(new double[]{785,1135,1190,730},new double[]{575,575,750,750},4);
        // rune marks around the empty sprite socket
        for(int i=0;i<12;i++){double a=i*Math.PI/6, cx=960+Math.cos(a)*246,cy=660+Math.sin(a)*80;g.save();g.translate(cx,cy);g.rotate(i*30);g.setFill(Color.web("#b18be7",.78));g.fillRect(-3,-13,6,26);g.fillRect(-9,-3,18,5);g.restore();}
        // candles
        for(int i=0;i<6;i++){double x=430+i*212,y=777+(i%2)*15;g.setFill(Color.web("#49303c"));g.fillRect(x,y,12,38);g.setFill(Color.web("#ffb764"));g.fillRect(x+4,y-9,4,11);}
        // vignette
        g.setFill(Color.rgb(3,3,10,.40));g.fillRect(0,0,W,130);g.fillRect(0,920,W,160);
    }

    private void drawFx(){GraphicsContext g=fx.getGraphicsContext2D();g.clearRect(0,0,W,H);double pulse=.55+.35*Math.sin(time*1.4);g.setFill(Color.rgb(173,112,255,.08*pulse));g.fillOval(690,510,540,315);for(int i=0;i<14;i++){double x=620+(i*137%690), y=220+(i*193%650), a=.18+.35*(.5+.5*Math.sin(time*1.7+i));g.setFill(Color.rgb(188,151,255,a));g.fillRect(Math.floor(x/4)*4,Math.floor(y/4)*4,3,3);}for(int i=0;i<6;i++){double x=430+i*212,y=767+(i%2)*15,r=30+4*Math.sin(time*4+i);g.setFill(Color.rgb(255,153,65,.08));g.fillOval(x-r,y-r,r*2,r*2);} }

    private void refresh(){
        for(int i=0;i<4;i++)classButtons[i].setStyle(buttonStyle(i==selectedClass));
        for(int i=0;i<2;i++)genderButtons[i].setStyle(buttonStyle(i==gender));
        String[] hairSet=gender==0?new String[]{"CROP","WAVES","BRAID","TIED"}:new String[]{"BOB","LENGTH","BRAID","TIED"};
        String[] outfitSet=gender==0?new String[]{"TRAVEL","LEATHER","GUARD","REGAL"}:new String[]{"TRAVEL","MANTLE","WARDEN","COURT"};
        for(int i=0;i<4;i++){hairButtons[i].setStyle(slotStyle(i==hair));outfitButtons[i].setStyle(slotStyle(i==outfit));hairCaptions[i].setText(hairSet[i]);outfitCaptions[i].setText(outfitSet[i]);}
        for(int i=0;i<7;i++)colorButtons[i].setStyle("-fx-background-color:"+hex(HAIR_COLORS[i])+";-fx-border-color:"+(i==color?"#ffe18b":"#795b45")+";-fx-border-width:"+(i==color?3:1)+";-fx-background-radius:0;-fx-border-radius:0;");
        description.setText(CLASS_INFO[selectedClass]);StringBuilder s=new StringBuilder();
        for(int i=0;i<6;i++)s.append(String.format("%-20s %3d%%\n",STAT_NAMES[selectedClass][i],STATS[selectedClass][i]));
        stats.setText(s.toString());
    }

    private void createCharacter(){String n=name.getText()==null?"":name.getText().trim();if(n.isEmpty()){notice.setText("Введите имя героя, прежде чем начать путь.");name.requestFocus();return;}if(n.length()>24){notice.setText("Имя должно содержать не более 24 символов.");return;}PlayerCharacter player=new PlayerCharacter(n,CLASS_NAMES[selectedClass],STATS[selectedClass][0]+60,STATS[selectedClass][4]/5,selectedClass==0?12:selectedClass==2?2:3);String[] hairs=gender==0?new String[]{"CROP","WAVES","BRAID","TIED"}:new String[]{"BOB","LENGTH","BRAID","TIED"};String[] outfits=gender==0?new String[]{"TRAVEL","LEATHER","GUARD","REGAL"}:new String[]{"TRAVEL","MANTLE","WARDEN","COURT"};notice.setText(n+"  ·  "+CLASSES[selectedClass]+"  ·  "+(gender==0?"MALE":"FEMALE")+"  ·  "+hairs[hair]+" / "+COLOR_NAMES[color]+" / "+outfits[outfit]);onCreate.accept(player);}
    private void panel(double x,double y,double w,double h,String title){Rectangle r=new Rectangle(w,h);r.setLayoutX(x);r.setLayoutY(y);r.setFill(Color.rgb(9,8,18,.88));r.setStroke(Color.web("#9a713f",.9));r.setStrokeWidth(2);r.setEffect(new DropShadow(22,Color.rgb(0,0,0,.65)));getChildren().add(r);Label t=label(title,17,"#f0d18b");t.setLayoutX(x+28);t.setLayoutY(y+25);getChildren().add(t);Rectangle d=new Rectangle(w-56,1,Color.web("#84613f",.7));d.setLayoutX(x+28);d.setLayoutY(y+61);getChildren().add(d);}
    private void divider(double x,double y,double w){Rectangle r=new Rectangle(w,1,Color.web("#936b43",.7));r.setLayoutX(x);r.setLayoutY(y);getChildren().add(r);}
    private void line(double x,double y,double w,double op){Rectangle r=new Rectangle(w,1,Color.web("#d0a85f",op));r.setLayoutX(x);r.setLayoutY(y);getChildren().add(r);}
    private Label label(String text,int size,String color){Label l=new Label(text);l.setTextFill(Color.web(color));l.setFont(Font.font("Serif",size));return l;}
    private Button button(String text,Runnable action){Button b=new Button(text);b.setStyle(buttonStyle(false));b.setOnAction(e->action.run());b.setOnMouseEntered(e->{if(b!=null)b.setScaleX(1.025);b.setScaleY(1.025);});b.setOnMouseExited(e->{b.setScaleX(1);b.setScaleY(1);});return b;}
    private Button slot(Runnable action){Button b=button("✧",action);b.setStyle(slotStyle(false));b.setTextFill(Color.web("#806b93"));b.setFont(Font.font("Serif",23));return b;}
    private String buttonStyle(boolean active){return "-fx-background-color:"+(active?"linear-gradient(#392a27,#191321)":"linear-gradient(#211b2c,#100e19)")+";-fx-text-fill:"+(active?"#ffe5a0":"#d6c4a3")+";-fx-font-family:'Serif';-fx-font-size:18px;-fx-font-weight:bold;-fx-border-color:"+(active?"#ffc75b":"#816044")+";-fx-border-width:"+(active?3:2)+";-fx-background-radius:0;-fx-border-radius:0;-fx-effect:"+(active?"dropshadow(gaussian, rgba(255,184,72,0.48), 18, 0.35, 0, 0)":"dropshadow(gaussian, rgba(0,0,0,0.6), 7, 0.2, 0, 2)")+";-fx-cursor:hand;";}
    private String slotStyle(boolean active){return "-fx-background-color:linear-gradient(#211b2c,#100e19);-fx-text-fill:#c6a6e8;-fx-border-color:"+(active?"#ffd36b":"#806044")+";-fx-border-width:"+(active?3:1.5)+";-fx-background-radius:0;-fx-border-radius:0;-fx-cursor:hand;";}
    private String hex(Color c){return String.format("#%02x%02x%02x",(int)(c.getRed()*255),(int)(c.getGreen()*255),(int)(c.getBlue()*255));}
    public void stop(){animation.stop();}
}
