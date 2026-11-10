package com.gamelab.ui.menu;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import com.gamelab.ui.components.MenuButton;

/** Settings that are currently supported by the game runtime. */
public final class SettingsScreen extends Pane {
    public static final double W=1920, H=1080;
    private final Slider effectsVolume = new Slider(0, 100, 65);
    private final MenuButton fullscreen;
    private final java.util.function.DoubleConsumer onVolume;
    private final java.util.function.Consumer<Boolean> onFullscreen;
    private boolean fullscreenValue;

    public SettingsScreen(double volume, boolean fullscreenValue, java.util.function.DoubleConsumer onVolume,
                          java.util.function.Consumer<Boolean> onFullscreen, Runnable back) {
        this.onVolume=onVolume; this.onFullscreen=onFullscreen; this.fullscreenValue=fullscreenValue;
        setPrefSize(W,H); setMinSize(W,H); setMaxSize(W,H);
        setStyle("-fx-background-color: #100d18;");
        Rectangle panel=new Rectangle(560,220,800,640); panel.setArcWidth(12);panel.setArcHeight(12);
        panel.setFill(Color.rgb(24,18,31,.98));panel.setStroke(Color.web("#9b7040"));panel.setStrokeWidth(3);
        Label title=label("SETTINGS",48,"#f2d08a");title.relocate(560,270);title.setPrefWidth(800);title.setAlignment(Pos.CENTER);
        Label effects=label("Sound Effects Volume",25,"#eadbc2");effects.relocate(680,410);
        effectsVolume.setValue(Math.max(0,Math.min(100,volume*100)));effectsVolume.setPrefWidth(560);effectsVolume.relocate(680,460);
        effectsVolume.valueProperty().addListener((o,a,b)->onVolume.accept(b.doubleValue()/100));
        fullscreen=new MenuButton("",330,58);fullscreen.relocate(795,560);fullscreen.setOnAction(()->{this.fullscreenValue=!this.fullscreenValue;onFullscreen.accept(this.fullscreenValue);updateFullscreen();});updateFullscreen();
        MenuButton backButton=new MenuButton("BACK",250,58);backButton.relocate(835,740);backButton.setOnAction(back);
        getChildren().addAll(panel,title,effects,effectsVolume,fullscreen,backButton);
    }
    private void updateFullscreen(){fullscreen.setText("Fullscreen: "+(fullscreenValue?"On":"Off"));}
    private static Label label(String text,int size,String color){Label l=new Label(text);l.setTextFill(Color.web(color));l.setStyle("-fx-font-family: 'Serif'; -fx-font-size: "+size+"px; -fx-font-weight: bold;");return l;}
}
