package com.gamelab.application.arena;

import com.gamelab.application.MinibossRuneRewardService;
import com.gamelab.domain.PlayerCharacter;
import com.gamelab.domain.economy.CoinWallet;
import com.gamelab.domain.inventory.Inventory;
import com.gamelab.domain.progression.*;
import com.gamelab.domain.rune.RuneLoadout;
import com.gamelab.pattern.observer.GameEventBus;
import java.util.*;

/** One in-memory New Game run. The same simulation preserves the Lord's history between stages. */
public final class CampaignRun {
    private final PlayerCharacter player;
    private final Inventory inventory;
    private final CoinWallet wallet;
    private final RuneLoadout loadout;
    private final Difficulty difficulty;
    private final GameEventBus events=new GameEventBus();
    private final ArenaSimulation simulation;
    private int completedStage, activeStage;
    private final Set<Integer> pendingRunes=new LinkedHashSet<>();

    public CampaignRun(PlayerCharacter player,Inventory inventory,CoinWallet wallet,RuneLoadout loadout) {
        this(player,inventory,wallet,loadout,Difficulty.NORMAL);
    }
    public CampaignRun(PlayerCharacter player,Inventory inventory,CoinWallet wallet,RuneLoadout loadout,Difficulty difficulty) {
        this.player=Objects.requireNonNull(player);this.inventory=Objects.requireNonNull(inventory);
        this.wallet=Objects.requireNonNull(wallet);this.loadout=Objects.requireNonNull(loadout);
        this.difficulty=Objects.requireNonNull(difficulty);
        simulation=new ArenaSimulation(player,events,difficulty); simulation.setLoadout(loadout);
    }
    public ArenaSimulation simulation() { return simulation; }
    public GameEventBus events() { return events; }
    public int activeStage() { return activeStage; }
    public int completedStage() { return completedStage; }
    public Difficulty difficulty() { return difficulty; }
    public boolean finished() { return completedStage==6; }
    public void enterNextStage() {
        if(finished()) throw new IllegalStateException("Campaign already completed");
        if(activeStage>completedStage) throw new IllegalStateException("Finish the current stage first");
        syncEquipment(); claimPendingRunes();
        activeStage=completedStage+1; simulation.startStage(activeStage);
    }
    public void syncEquipment() {
        inventory.getEquippedWeapon().ifPresent(weapon -> {
            if(weapon!=loadout.getWeapon()) loadout.changeWeapon(weapon);
        });
        inventory.getItems().stream()
                .filter(com.gamelab.domain.item.QuiverUpgrade.class::isInstance)
                .map(com.gamelab.domain.item.QuiverUpgrade.class::cast)
                .max(java.util.Comparator.comparingInt(com.gamelab.domain.item.QuiverUpgrade::level))
                .ifPresent(upgrade -> simulation.configureArcherQuiver(upgrade.capacity(), upgrade.refillSeconds()));
    }
    /** Rewards are awarded once, after actually reaching the exit, never for retrying a wave. */
    public boolean completeStage() {
        if(!simulation.completed() || activeStage<=completedStage) return false;
        int coins=0;
        for(int wave=1;wave<=(activeStage==6?1:3);wave++)
            for(String id:ArenaLayouts.wave(activeStage,wave)) coins+=ArenaCatalog.byId(id).type().coins;
        wallet.addCoins(coins);
        int previousLevel=player.getLevel();
        player.completeStage(CampaignBalance.stage(activeStage),difficulty);
        if(player.getLevel()>previousLevel) events.publish("PLAYER_LEVEL_UP","Новый уровень: "+player.getLevel(),player.getLevel());
        completedStage=activeStage;
        if(completedStage<=5) pendingRunes.add(completedStage);
        claimPendingRunes();
        events.publish("CAMPAIGN_REWARD","Награда: "+coins+" монет. Уровень "+player.getLevel());
        return true;
    }
    public void claimPendingRunes() {
        var rewards=new MinibossRuneRewardService(events);
        var iterator=pendingRunes.iterator();
        while(iterator.hasNext()) if(rewards.grantStageRune(iterator.next(),inventory).success()) iterator.remove();
    }
    public void useHealthPotion() {
        if(!player.isAlive() || player.getHealth()>=player.getMaxHealth()) return;
        inventory.getItems().stream().filter(item -> item instanceof com.gamelab.domain.item.Potion potion &&
                potion.getPotionType()==com.gamelab.domain.item.PotionType.HEALTH).findFirst().ifPresent(item -> {
                    player.heal(((com.gamelab.domain.item.Potion)item).getEffectAmount()); inventory.removeItem(item);
                    events.publish("POTION_USED","Использовано зелье здоровья");
                });
    }
    public int healthPotionCount() {
        return (int)inventory.getItems().stream().filter(item -> item instanceof com.gamelab.domain.item.Potion potion &&
                potion.getPotionType()==com.gamelab.domain.item.PotionType.HEALTH).count();
    }
}
