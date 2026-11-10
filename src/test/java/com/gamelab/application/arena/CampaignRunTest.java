package com.gamelab.application.arena;

import com.gamelab.pattern.builder.CharacterBuilder;
import com.gamelab.domain.*;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.item.*;
import com.gamelab.domain.rune.*;
import com.gamelab.domain.shop.ShadowfallShopCatalog;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CampaignRunTest {
    private final PlayerCharacter player=CharacterBuilder.warrior("Campaign Hero").build();
    private final Inventory inventory=new Inventory(16);
    private final CoinWallet wallet=new CoinWallet();
    private final RuneLoadout loadout=new RuneLoadout(WeaponCatalog.createStarterSword());
    private CampaignRun run() {
        inventory.addItem(loadout.getWeapon());inventory.equipWeapon(loadout.getWeapon(),"KNIGHT");
        var run=new CampaignRun(player,inventory,wallet,loadout);run.simulation().setInvincible(true);return run;
    }
    private void clear(CampaignRun run) {
        var arena=run.simulation();
        for(int wave=1;wave<=(run.activeStage()==6?1:3);wave++) {
            assertEquals(wave,arena.wave());
            arena.fighters().forEach(f -> f.enemy.takeDamage(f.enemy.getMaxHealth()));
            for(int i=0;i<105;i++) arena.update(.02,0,false);
        }
        assertTrue(arena.exitReady());arena.hero().x=1880;arena.update(.02,0,false);
        assertTrue(arena.completed());
    }
    @Test void playsAllSixStagesWithProgressionRewardsAndSharedHistory() {
        var run=run();var simulation=run.simulation();
        for(int stage=1;stage<=6;stage++) {
            run.enterNextStage();assertEquals(stage,run.activeStage());assertSame(simulation,run.simulation());
            assertFalse(run.completeStage());clear(run);
            int coins=wallet.getBalance();assertTrue(run.completeStage());assertTrue(wallet.getBalance()>coins);
            int after=wallet.getBalance();assertFalse(run.completeStage());assertEquals(after,wallet.getBalance());
        }
        assertTrue(run.finished());assertTrue(player.getLevel()>1);assertTrue(player.getStatPoints()>0);
        assertEquals(5,inventory.getItems().stream().filter(i -> i instanceof Rune).count());
        assertTrue(simulation.defeatedTypes().containsAll(java.util.List.of("soldier","archer","ghost-ranged")));
        assertThrows(IllegalStateException.class,run::enterNextStage);
    }
    @Test void newStageUsesPurchasedEquipmentAndInstalledRunes() {
        var run=run();var sword=WeaponCatalog.createSwiftBlade();inventory.addItem(sword);
        inventory.equipWeapon(sword,"Воин");run.syncEquipment();loadout.installRune(RuneCatalog.create(RuneType.HASTE));
        run.enterNextStage();assertSame(sword,run.simulation().loadout().getWeapon());
        assertEquals(RuneType.HASTE,run.simulation().loadout().getInstalledRunes().get(0).getRuneType());
        assertThrows(IllegalStateException.class,run::enterNextStage);
    }
    @Test void potionsConsumeOnlyWhenAnAlivePlayerNeedsHealing() {
        var run=run();Potion potion=ShadowfallShopCatalog.createHealthPotion();inventory.addItem(potion);
        run.useHealthPotion();assertTrue(inventory.getItems().contains(potion));
        player.takeDamage(40+player.getDefense());int health=player.getHealth();run.useHealthPotion();
        assertTrue(player.getHealth()>health);assertFalse(inventory.getItems().contains(potion));
    }
    @Test void runeRewardWaitsForAnInventorySlotWithoutBeingLostOrDuplicated() {
        var small=new Inventory(1);small.addItem(loadout.getWeapon());
        var run=new CampaignRun(player,small,wallet,loadout);run.simulation().setInvincible(true);run.enterNextStage();clear(run);
        assertTrue(run.completeStage());assertEquals(1,small.getSize());
        small.removeItem(loadout.getWeapon());run.claimPendingRunes();run.claimPendingRunes();
        assertEquals(1,small.getSize());assertEquals(RuneType.FIRE,((Rune)small.getItems().get(0)).getRuneType());
    }
}
