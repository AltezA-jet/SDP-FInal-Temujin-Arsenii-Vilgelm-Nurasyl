package com.gamelab.ui.level;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.event.Event;
import com.gamelab.pattern.observer.GameEventBus;
import com.gamelab.ui.components.MaleWarriorSprite;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in real JavaFX rendering check; writes snapshots for reviewing sprite and placeholder layouts. */
@EnabledIfSystemProperty(named="arena.uiSmoke",matches="true")
class EnemyArenaScreenSmokeTest {
    @Test void allEncountersRenderAndCanBeRestarted() throws Exception {
        CountDownLatch done=new CountDownLatch(1);
        AtomicReference<Throwable> failure=new AtomicReference<>();
        Platform.startup(() -> {
            EnemyArenaScreen screen=null;
            try {
                GameEventBus events=new GameEventBus();
                java.util.List<String> actions=new java.util.ArrayList<>();
                events.subscribe(event -> actions.add(event.type()));
                screen=new EnemyArenaScreen(() -> { },events,cue -> { });
                new Scene(screen,1920,1080);
                screen.applyCss(); screen.layout();
                CheckBox pause=(CheckBox)screen.lookupAll(".check-box").stream()
                        .filter(n -> ((CheckBox)n).getText().equals("Пауза")).findFirst().orElseThrow();
                MaleWarriorSprite hero=(MaleWarriorSprite)screen.getChildren().stream()
                        .filter(n -> n instanceof MaleWarriorSprite).findFirst().orElseThrow();
                assertTrue(hero.isMouseTransparent(),"Hero bounds must not swallow arena clicks");
                assertEquals(96,hero.prefWidth(-1),.001);
                assertEquals(144,hero.prefHeight(-1),.001);
                Event.fireEvent(hero,new MouseEvent(MouseEvent.MOUSE_PRESSED,350,820,350,820,MouseButton.PRIMARY,1,
                        false,false,false,false,true,false,false,false,false,true,null));
                assertEquals(1,actions.stream().filter(type -> type.equals("PLAYER_ATTACK")).count(),
                        "Click on the hero must reach the arena attack handler");
                pause.fire();
                var boxes=screen.lookupAll(".combo-box").stream()
                        .map(n -> (ComboBox<?>)n).toList();
                ComboBox<?> stage=boxes.stream().filter(b -> b.getItems().size()==6).findFirst().orElseThrow();
                ComboBox<?> encounter=boxes.stream().filter(b -> b.getItems().contains("Босс этапа")).findFirst().orElseThrow();
                Button spawn=(Button)screen.lookupAll(".button").stream()
                        .filter(n -> ((Button)n).getText().equals("Создать")).findFirst().orElseThrow();
                Files.createDirectories(Path.of("target/arena-preview"));
                for (int number=0;number<6;number++) {
                    stage.getSelectionModel().select(number);
                    for (int boss=number==5 ? 1 : 0;boss<2;boss++) {
                        encounter.getSelectionModel().select(boss); spawn.fire();
                        screen.applyCss(); screen.layout();
                        WritableImage snapshot=screen.snapshot(null,null);
                        assertNotEquals(snapshot.getPixelReader().getArgb(1900,200),
                                snapshot.getPixelReader().getArgb(1900,920),"Arena and floor must actually render");
                        BufferedImage output=new BufferedImage(1920,1080,BufferedImage.TYPE_INT_ARGB);
                        for (int y=0;y<1080;y++) for (int x=0;x<1920;x++)
                            output.setRGB(x,y,snapshot.getPixelReader().getArgb(x,y));
                        assertTrue(ImageIO.write(output,"png",Path.of("target/arena-preview/stage-"+(number+1)+(boss==1 ? "-boss" : "-regular")+".png").toFile()));
                    }
                }
            } catch (Throwable exception) { failure.set(exception); }
            finally { if (screen!=null) screen.stop(); done.countDown(); }
        });
        assertTrue(done.await(45,TimeUnit.SECONDS),"JavaFX rendering timed out");
        Platform.exit();
        if (failure.get()!=null) throw new AssertionError("Arena failed to render",failure.get());
    }
}
