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
                screen.stop();
                verifyNewGameRoute();
            } catch (Throwable exception) { failure.set(exception); }
            finally { if (screen!=null) screen.stop(); done.countDown(); }
        });
        assertTrue(done.await(45,TimeUnit.SECONDS),"JavaFX rendering timed out");
        Platform.exit();
        if (failure.get()!=null) throw new AssertionError("Arena failed to render",failure.get());
    }

    private void verifyNewGameRoute() throws Exception {
        var app=new com.gamelab.app.Main();var window=new javafx.stage.Stage();
        try {
            app.start(window);window.hide();
            invoke(app,"onNewGame");
            var creation=(com.gamelab.ui.character.CharacterCreationScreen)content(window);
            creation.lookupAll(".text-field").stream().map(n -> (javafx.scene.control.TextField)n).findFirst().orElseThrow().setText("Campaign UI Hero");
            invoke(creation,"createCharacter");
            var tutorial=(TutorialLevelScreen)content(window);
            ((Runnable)field(tutorial,"onComplete")).run();
            var market=(com.gamelab.ui.shop.StageOneMarketScreen)content(window);
            ((Runnable)field(market,"onContinue")).run();
            var arena=(EnemyArenaScreen)content(window);
            assertTrue(arena.lookupAll(".combo-box").isEmpty(),"New Game must not expose sandbox selectors");
            assertTrue(arena.lookupAll(".check-box").isEmpty(),"New Game must not expose immortality or debug controls");
            assertTrue(arena.lookupAll(".button").isEmpty(),"Active gameplay must not contain permanent menu buttons");
            saveCampaignSnapshot(arena,"campaign-clean");
            verifyHeroFeetStayOnTheSurface(arena);
            var run=(com.gamelab.application.arena.CampaignRun)field(app,"campaignRun");
            assertEquals(1,run.activeStage());
            assertSame(run.simulation(),field(arena,"simulation"));
            run.simulation().hero().x=600;
            Event.fireEvent(arena,new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_PRESSED,"","",javafx.scene.input.KeyCode.ESCAPE,false,false,false,false));
            var hub=(com.gamelab.ui.menu.GameMenuScreen)content(window);
            assertSame(field(app,"campaignWallet"),hub.getWallet());
            hub.openInventory();assertInstanceOf(com.gamelab.ui.inventory.InventoryScreen.class,content(window));
            Event.fireEvent(content(window),new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_PRESSED,"","",javafx.scene.input.KeyCode.ESCAPE,false,false,false,false));
            hub=(com.gamelab.ui.menu.GameMenuScreen)content(window);
            ((Runnable)field(hub,"back")).run();
            assertSame(arena,content(window));assertEquals(600,run.simulation().hero().x);
            arena.suspend();run.simulation().setInvincible(true);
            for(int wave=1;wave<=3;wave++) {
                if(wave==3) saveCampaignSnapshot(arena,"campaign-clean-boss");
                run.simulation().fighters().forEach(f -> f.enemy.takeDamage(f.enemy.getMaxHealth()));
                for(int tick=0;tick<105;tick++) run.simulation().update(.02,0,false);
            }
            run.simulation().hero().x=1880;run.simulation().update(.02,0,false);
            invoke(app,"onCampaignStageCompleted");
            assertInstanceOf(com.gamelab.ui.shop.StageOneMarketScreen.class,content(window));
            assertEquals(1,run.completedStage());assertTrue(run.simulation().player().getLevel()>1);
            market=(com.gamelab.ui.shop.StageOneMarketScreen)content(window);
            ((Runnable)field(market,"onContinue")).run();assertEquals(2,run.activeStage());
            assertInstanceOf(EnemyArenaScreen.class,content(window));
        } finally {
            Object campaign=field(app,"campaignArena");if(campaign instanceof EnemyArenaScreen arena)arena.stop();
            Object audio=field(app,"campaignAudio");if(audio instanceof com.gamelab.ui.audio.CombatAudio sound)sound.close();window.close();
        }
    }
    private void saveCampaignSnapshot(EnemyArenaScreen arena,String name) throws Exception {
        invoke(arena,"render");
        double sx=arena.getScaleX(),sy=arena.getScaleY();arena.setScaleX(1);arena.setScaleY(1);
        arena.applyCss();arena.layout();var snapshot=arena.snapshot(null,null);
        BufferedImage output=new BufferedImage((int)snapshot.getWidth(),(int)snapshot.getHeight(),BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<output.getHeight();y++)for(int x=0;x<output.getWidth();x++)output.setRGB(x,y,snapshot.getPixelReader().getArgb(x,y));
        assertTrue(ImageIO.write(output,"png",Path.of("target/arena-preview/"+name+".png").toFile()));
        arena.setScaleX(sx);arena.setScaleY(sy);
    }
    private void verifyHeroFeetStayOnTheSurface(EnemyArenaScreen arena) throws Exception {
        var hero=(MaleWarriorSprite)arena.getChildren().stream().filter(n -> n instanceof MaleWarriorSprite).findFirst().orElseThrow();
        var view=(javafx.scene.image.ImageView)hero.getChildren().get(0);
        var simulation=(com.gamelab.application.arena.ArenaSimulation)field(arena,"simulation");
        for(boolean crouch:java.util.List.of(false,true)) for(boolean walking:java.util.List.of(false,true)) for(int frame=0;frame<4;frame++) {
            hero.setWalking(walking,crouch);hero.update(.12);invoke(arena,"render");
            var image=view.getImage();int last=-1;
            for(int y=0;y<image.getHeight();y++) for(int x=0;x<image.getWidth();x++)
                if((image.getPixelReader().getArgb(x,y)>>>24)>=128) last=y;
            var bounds=view.getBoundsInLocal();
            var pointInHero=view.localToParent(bounds.getCenterX(),bounds.getMinY()+bounds.getHeight()*(last+1)/image.getHeight());
            double visibleFeet=hero.localToParent(pointInHero).getY();
            assertEquals(simulation.hero().y+6,visibleFeet,.01,"Hero must retain the 6px visual offset: crouch="+crouch+", walking="+walking+", frame="+frame);
        }
        hero.setWalking(false,false);invoke(arena,"render");
    }
    private javafx.scene.layout.Pane content(javafx.stage.Stage window) {
        return (javafx.scene.layout.Pane)((javafx.scene.layout.Pane)window.getScene().getRoot()).getChildren().get(0);
    }
    private Object field(Object object,String name) throws Exception {
        var field=object.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(object);
    }
    private void invoke(Object object,String name) throws Exception {
        var method=object.getClass().getDeclaredMethod(name);method.setAccessible(true);method.invoke(object);
    }
}
