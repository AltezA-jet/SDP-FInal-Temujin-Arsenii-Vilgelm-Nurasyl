package com.gamelab.ui.menu;

import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.progression.Difficulty;
import com.gamelab.ui.components.MenuButton;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.TextAlignment;
import com.gamelab.domain.inventory.Inventory;
import java.util.function.Consumer;

import java.util.ArrayList;
import java.util.List;

/** Game hub and skill-tree scaffold styled after the supplied reference. */
public final class GameMenuScreen extends Pane {
    public static final double W=1920,H=1080;
    private static final String[] CLASSES={"WARRIOR","ARCHER","MAGE","ASSASSIN"};
    private static final String[][] SKILLS={
        {"Whirlwind","Lunge","Crushing Leap","War Cry","Titan's Wrath"},
        {"Triple Shot","Poison Arrow","Trap","Hunter's Mark","Arrow Rain"},
        {"Blink","Frost Ring","Chain Lightning","Mana Shield","Meteor"},
        {"Shadow Strike","Smoke Bomb","Blade Flurry","Execution","Death Dance"}};
    private static final int[] LEVELS={2,3,5,7,8};
    private static final int[][] COSTS={{1,1,2},{1,2,2},{2,2,3},{2,3,3},{3,4,5}};
    private static final String[] TABS={"CHARACTER","SKILL TREE","INVENTORY","STATS","SETTINGS","SHOP"};
    private final PlayerCharacter player;
    private final Runnable back;
    private final Inventory inventory;
    private final Consumer<Inventory> openInventory;
    private Consumer<Inventory> openShop = inventory -> { };
    private final CoinWallet wallet;
    private MenuButton shopButton;
    private final List<MenuButton> buttons=new ArrayList<>();
    private final List<Label> dynamic=new ArrayList<>();
    private final MenuButton[] skillNodes=new MenuButton[5];
    private final Label[] skillRankLabels=new Label[4];
    private int selectedClass,selectedSkill=0,selectedTab=1;
    private Label detailTitle,detailBody,points,feedback;
    private Label currencyLabel;

    public GameMenuScreen(
            PlayerCharacter player,
            Runnable back
    ) {
        this(
                player,
                back,
                new Inventory(16),
                inventory -> { }
        );
    }

    public GameMenuScreen(
            PlayerCharacter player,
            Runnable back,
            Consumer<Inventory> openInventory
    ) {
        this(
                player,
                back,
                new Inventory(16),
                openInventory
        );
    }

    public GameMenuScreen(
            PlayerCharacter player,
            Runnable back,
            Inventory inventory,
            Consumer<Inventory> openInventory
    ) {
        this(player,back,inventory,openInventory,new CoinWallet(350));
    }
    public GameMenuScreen(PlayerCharacter player,Runnable back,Inventory inventory,
                          Consumer<Inventory> openInventory,CoinWallet wallet) {
        this.wallet=java.util.Objects.requireNonNull(wallet);
        this.player = player;
        this.back = back;
        this.inventory = inventory;
        this.openInventory = openInventory;

        setPrefSize(W, H);
        setMinSize(W, H);
        setMaxSize(W, H);

        drawBackground();
        drawHeader();
        drawPanels();
        drawTree();
        drawFooter();
        refresh();
    }
    private void drawBackground(){
        Image image=new Image(GameMenuScreen.class.getResource("/assets/references/moonlit_castle_background.jpg").toExternalForm());
        ImageView bg=new ImageView(image);bg.setFitWidth(W);bg.setFitHeight(H);bg.setPreserveRatio(false);getChildren().add(bg);
        Rectangle veil=new Rectangle(W,H,Color.rgb(12,5,22,.56));getChildren().add(veil);
        Rectangle top=new Rectangle(W,100,Color.rgb(9,5,16,.93));getChildren().add(top);
        line(0,99,W,99,"#be7a41",2); line(0,1000,W,1000,"#be7a41",2);
    }
    private void drawHeader(){
        MenuButton backButton=button("←  BACK",34,24,190,58);backButton.setOnAction(back);buttons.add(backButton);
        for(int i=0;i<TABS.length;i++){
            final int tab=i;
            MenuButton b=button(TABS[i],420+i*190,28,175,54);
            if(tab==5) shopButton=b;
            b.setOnAction(()->{
                selectedTab=tab;
                if(tab==2) openInventory();
                if(tab==5) openShop.accept(inventory);
                refresh();
            });
            buttons.add(b);
        }
        currencyLabel=label("◆  "+wallet.getBalance(),30,1620,32,190,48,"#ffd66e");getChildren().add(currencyLabel);
    }
    private void drawPanels(){
        panel(42,135,430,744);panel(1452,135,426,744);
        labelAt("CLASS",76,163,22,"#f5c768"); labelAt(CLASSES[classIndex(player.getCharacterClass())],76,204,36,"#ffe2a1");
        String[] names={"♥  Health","⚔  Strength","➤  Dexterity","✦  Resources"};
        for(int i=0;i<names.length;i++){
            int rank=i;labelAt(names[i],78,285+i*48,23,"#e6c8b7");
            Label value=label("0 / 12",370,281+i*48,20,85,32,"#ffe2a1");dynamic.add(value);
            MenuButton up=button("+",360,280+i*48,60,36);up.setOnAction(()->{String[] keys={"health","attack","speed","resources"};boolean ok=player.upgradeStat(keys[rank]);feedback.setText(ok?"Stat improved":"Need points / branch is full");refresh();});buttons.add(up);
        }
        line(64,488,448,488,"#9b6848",2);labelAt("CLASS SKILLS",80,505,21,"#f4b34e");
        labelAt("Passive skill unlocks at level 4",80,547,17,"#ddc8c4");
        labelAt("Ultimates unlock at level 8",80,578,17,"#ddc8c4");
        labelAt("Level 6 / 12: stat bonuses scale\nby class and branch.",80,628,17,"#c2adbe");
        labelAt("PLAYER",1490,158,20,"#f5c768");
        labelAt(player.getName(),1490,193,28,"#ffe2a1");
        detailTitle=label("",1490,257,26,345,50,"#ffe2a1");getChildren().add(detailTitle);
        detailBody=label("",1490,322,18,340,175,"#dbc7cf");detailBody.setWrapText(true);detailBody.setTextAlignment(TextAlignment.LEFT);getChildren().add(detailBody);
        line(1480,540,1846,540,"#9b6848",2);
        points=label("",1490,565,18,340,40,"#e9bd71");getChildren().add(points);
        MenuButton learn=button("UPGRADE / UNLOCK",1510,727,310,62);learn.setOnAction(this::upgradeSelected);buttons.add(learn);
        feedback=label("Select a node in the tree",1490,809,15,350,32,"#bca9bc");getChildren().add(feedback);
    }
    private void drawTree(){
        int[] x={760,920,1080,1240,1000};int[] y={305,445,585,725,270};
        int[][] links={{0,1},{0,2},{1,2},{2,3},{1,4},{4,2}};
        for(int[] link:links)line(x[link[0]]+46,y[link[0]]+46,x[link[1]]+46,y[link[1]]+46,"#d48a42",3);
        for(int i=0;i<4;i++){
            final int index=i;MenuButton node=button(SKILLS[selectedClass][i],x[i],y[i],96,96);skillNodes[i]=node;
            node.setOnAction(()->{selectedSkill=index;refresh();});buttons.add(node);
            Label rank=label("0/3",x[i]+14,y[i]+99,14,75,24,"#f2c55f");dynamic.add(rank);skillRankLabels[i]=rank;
        }
        MenuButton ultimate=button("ULTIMATE",x[4],y[4],110,110);skillNodes[4]=ultimate;ultimate.setOnAction(()->{selectedSkill=4;refresh();});buttons.add(ultimate);
        labelAt("CLASS SKILL TREE",780,175,22,"#f4c269");
        labelAt("Click a node to inspect it",805,850,16,"#d4bacd");
    }
    private void drawFooter(){
        MenuButton[] classes=new MenuButton[4];for(int i=0;i<4;i++){final int index=i;classes[i]=button(CLASSES[i],574+i*195,922,185,64);classes[i].setOnAction(()->{selectedClass=index;selectedSkill=0;refresh();});buttons.add(classes[i]);}
        labelAt("LMB  Select / Upgrade",46,1022,17,"#cdbac7");labelAt("ESC  Back",1645,1022,17,"#cdbac7");
        setOnKeyPressed(e->{
            if(e.getCode()==javafx.scene.input.KeyCode.ESCAPE) back.run();
            if(e.getCode()==javafx.scene.input.KeyCode.I) openInventory();
        });setFocusTraversable(true);
    }
    private void upgradeSelected(){
        int rank=player.getSkillRank(skillId(selectedSkill));int cost=COSTS[selectedSkill][Math.min(rank,2)];
        boolean ok=player.upgradeSkill(skillId(selectedSkill),3,cost,LEVELS[selectedSkill]);
        feedback.setText(ok?"Skill rank increased":"Locked, max rank, or not enough skill points");refresh();
    }
    private String skillId(int index){return CLASSES[selectedClass].toLowerCase(java.util.Locale.ROOT)+"."+index;}
    private void refresh(){
        if(detailTitle==null)return;
        detailTitle.setText(SKILLS[selectedClass][selectedSkill]);int rank=player.getSkillRank(skillId(selectedSkill));
        int cost=COSTS[selectedSkill][Math.min(rank,2)];
        detailBody.setText("Rank: "+rank+" / 3\n\nUnlock level: "+LEVELS[selectedSkill]+(selectedSkill==4?" (ultimate)":"")+"\n\nEach later rank improves the skill effect by 20% and reduces cooldown by 10%.\n\nNext rank cost: "+cost+" skill points.");
        points.setText("Level "+player.getLevel()+"     Stat points: "+player.getStatPoints()+"     Skill points: "+player.getSkillPoints());
        for(int i=0;i<4;i++)if(dynamic.size()>=4)dynamic.get(i).setText(new int[]{player.getHealthRank(),player.getAttackRank(),player.getSpeedRank(),player.getResourcesRank()}[i]+" / 12");
        for(int i=0;i<4;i++){skillNodes[i].setText(SKILLS[selectedClass][i]);skillRankLabels[i].setText(player.getSkillRank(CLASSES[selectedClass].toLowerCase(java.util.Locale.ROOT)+"."+i)+" / 3");}
        skillNodes[4].setText(SKILLS[selectedClass][4]);
        for(MenuButton b:buttons)b.setSelected(false);
        skillNodes[selectedSkill].setSelected(true);
        for(int i=0;i<TABS.length;i++)buttons.get(i+1).setSelected(selectedTab==i);
    }
    private int classIndex(String name){for(int i=0;i<CLASSES.length;i++)if(name.toUpperCase(java.util.Locale.ROOT).contains(CLASSES[i]))return i;return 0;}
    private MenuButton button(String text,double x,double y,double w,double h){MenuButton b=new MenuButton(text,w,h);b.relocate(x,y);getChildren().add(b);return b;}
    private void panel(double x,double y,double w,double h){Rectangle p=new Rectangle(x,y,w,h);p.setFill(Color.rgb(12,6,19,.91));p.setStroke(Color.web("#d08b4a"));p.setStrokeWidth(3);getChildren().add(p);}
    private void line(double x1,double y1,double x2,double y2,String color,double width){Line l=new Line(x1,y1,x2,y2);l.setStroke(Color.web(color));l.setStrokeWidth(width);getChildren().add(l);}
    private Label label(String text,double x,double y,double size,double w,double h,String color){Label l=new Label(text);l.relocate(x,y);l.setPrefSize(w,h);l.setTextFill(Color.web(color));l.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: "+size+"px; -fx-font-weight: bold;");l.setAlignment(Pos.CENTER_LEFT);return l;}
    private void labelAt(String text,double x,double y,double size,String color){Label l=label(text,x,y,size,390,40,color);getChildren().add(l);}
    public Inventory getInventory(){return inventory;}
    public CoinWallet getWallet(){return wallet;}
    public void refreshWallet(){currencyLabel.setText("◆  "+wallet.getBalance());}
    public void setOnShop(Consumer<Inventory> openShop){this.openShop=openShop==null?inventory->{ }:openShop;}
    public void setShopAvailable(boolean available) { shopButton.setDisable(!available); }
    public void openInventory(){openInventory.accept(inventory);}
}
