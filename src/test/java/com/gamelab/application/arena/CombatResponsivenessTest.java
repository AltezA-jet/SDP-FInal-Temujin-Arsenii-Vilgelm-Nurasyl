package com.gamelab.application.arena;

import com.gamelab.pattern.builder.CharacterBuilder;
import com.gamelab.pattern.observer.GameEventBus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CombatResponsivenessTest {
    private ArenaSimulation arena(int stage,boolean boss) {
        var a=new ArenaSimulation(CharacterBuilder.warrior("Hero").damage(20).maxHealth(1000).defense(0).build(),new GameEventBus(),new java.util.Random(17));
        a.start(stage,boss);return a;
    }
    private void advance(ArenaSimulation a,double seconds) { for(int i=0;i<Math.round(seconds/.02);i++)a.update(.02,0,false); }
    @Test void rollingWindupIsExactlyOneAndAHalfTimesTheOriginal() {
        assertEquals(.28*1.5,ArenaSimulation.ROLL_WINDUP,.00001);
        var a=arena(3,true);a.hero().x=200;var boss=a.fighters().get(0);
        for(int i=0;i<200 && boss.windup==0;i++)a.update(.02,0,false);
        assertEquals("transform",boss.animation);assertEquals(.42,boss.windup,.00001);
        advance(a,.4);assertEquals(0,boss.charging);
        advance(a,.04);assertTrue(boss.charging>0);
    }
    @Test void dodgeCancelsAnUnreleasedPlayerHitAndCanChooseDirectionImmediately() {
        var a=arena(1,false);var enemy=a.fighters().get(1);a.hero().x=enemy.body.x-50;
        int before=enemy.enemy.getHealth();assertTrue(a.attack());a.dodge(-1);advance(a,.24);
        assertEquals(before,enemy.enemy.getHealth());assertTrue(a.facingLeft());assertFalse(a.attacking());
        assertTrue(a.hero().x<enemy.body.x-100);
    }
    @Test void movementReturnsDuringAttackRecoveryAndBufferedInputStartsNextSwing() {
        var a=arena(1,false);a.attack();a.update(.02,1,false);assertEquals(350,a.hero().x,.001);
        advance(a,.22);double x=a.hero().x;a.update(.02,1,false);assertTrue(a.hero().x>x);
        advance(a,.04);assertFalse(a.attack());advance(a,.14);assertEquals(2,a.attackSerial());
    }
    @Test void lordQuickAttackIsTelegraphedAndDodgableAfterAReactionDelay() {
        for(boolean dodge:java.util.List.of(false,true)) {
            var a=arena(6,true);a.hero().x=1000;var lord=a.fighters().get(0);
            for(int i=0;i<300 && lord.windup==0;i++)a.update(.02,0,false);
            assertEquals("attack_quick",lord.animation);int health=a.player().getHealth();
            advance(a,.22);assertEquals(health,a.player().getHealth());
            if(dodge)a.dodge(-1);
            advance(a,.3);
            if(dodge) assertEquals(health,a.player().getHealth()); else assertTrue(a.player().getHealth()<health);
            assertEquals(1,lord.attacks);
        }
    }
    @Test void humanoidArtworkUsesTheSameVisibleScaleAsTheHero() throws Exception {
        var hero=javax.imageio.ImageIO.read(getClass().getResource("/assets/male_warrior/body_idle.png"));
        var soldier=javax.imageio.ImageIO.read(getClass().getResource("/assets/enemies/1 Этап/Soldier/swordsman_idle.png"));
        double ratio=(double)height(hero)/height(soldier);
        assertTrue(ratio>.85 && ratio<1.15,"Visible hero/soldier height ratio: "+ratio);
        assertEquals(96,hero.getHeight());assertEquals(96,soldier.getHeight());
    }
    private int height(java.awt.image.BufferedImage image) {
        int min=image.getHeight(),max=0;
        for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if((image.getRGB(x,y)>>>24)>48){min=Math.min(min,y);max=Math.max(max,y);}
        return max-min+1;
    }
}
