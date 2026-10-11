package io.github.gegagegagega.werewolf.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.Judgment;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.github.gegagegagega.werewolf.inventory.PlayerInventories;
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;

/** The judgment buttons, and keeping everyone's verdict secret by hiding what they hold. */
public final class TrialListener implements Listener {

    private final WerewolfPlugin plugin;

    public TrialListener(WerewolfPlugin plugin) {
        this.plugin = plugin;
    }

    /** Clicking with a judgment button picks that verdict; clicking again abstains. */
    @EventHandler
    public void onJudgmentClick(PlayerInteractEvent event) {
        // Each click fires once per hand; only count the main hand.
        if (event.getHand() != EquipmentSlot.HAND) return;
        Game game = plugin.game();
        WWPlayer participant = game.getPlayer(event.getPlayer().getName());
        if (participant == null || !game.canJudge(participant)) return;
        Judgment judgment = PlayerInventories.judgmentForSlot(event.getPlayer().getInventory().getHeldItemSlot());
        if (judgment != null) {
            game.toggleJudgment(participant, judgment);
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        hideHandsLater(event.getPlayer());
    }

    @EventHandler
    public void onEquipmentChange(EntityEquipmentChangedEvent event) {
        if (event.getEntity() instanceof Player target) {
            hideHandsLater(target);
        }
    }

    /** While someone is on trial, nobody can see what anyone else is holding. */
    private void hideHandsLater(Player target) {
        Game game = plugin.game();
        if (!game.isActive() || game.getAccused() == null) return;
        // Run after Minecraft sends the real held-item update.
        Bukkit.getScheduler().runTask(plugin, () -> {
            hideHand(target);
            plugin.itemHider().hideHeldItemFromEveryone(target);
        });
    }

    private static void hideHand(Player target) {
        ItemStack air = new ItemStack(Material.AIR);
        for (Player viewer : target.getWorld().getPlayers()) {
            if (viewer.equals(target)) continue;
            viewer.sendEquipmentChange(target, EquipmentSlot.HAND, air);
            viewer.sendEquipmentChange(target, EquipmentSlot.OFF_HAND, air);
        }
    }
}
