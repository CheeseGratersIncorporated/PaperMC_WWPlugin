package io.github.gegagegagega.werewolf.listener;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSpawnChangeEvent;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.github.gegagegagega.werewolf.housing.Door;
import io.github.gegagegagega.werewolf.housing.House;
import io.papermc.paper.event.block.BellRingEvent;

/** Keeps the map intact and players where the game wants them. */
public final class ProtectionListener implements Listener {

    private final WerewolfPlugin plugin;

    public ProtectionListener(WerewolfPlugin plugin) {
        this.plugin = plugin;
    }

    /** House doors are opened and closed by the game, not by players. */
    @EventHandler
    public void onDoorInteract(PlayerInteractEvent event) {
        Game game = plugin.game();
        if (!game.isActive() || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null || !game.hasPlayer(event.getPlayer().getName())) return;
        for (House house : plugin.housing().getHouses()) {
            for (Door door : house.doors) {
                if (clicked.equals(door.getBlock())) {
                    event.setUseInteractedBlock(Event.Result.DENY);
                    event.setUseItemInHand(Event.Result.DENY);
                }
            }
        }
    }

    /** The dead can't touch anything. */
    @EventHandler
    public void onDeadInteract(PlayerInteractEvent event) {
        Game game = plugin.game();
        WWPlayer participant = game.getPlayer(event.getPlayer().getName());
        if (game.isActive() && participant != null && !participant.isAlive()) {
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            event.setCancelled(true);
        }
    }

    /** Nobody moves during the trial and execution, except the executed one's ghost. */
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Game game = plugin.game();
        if (!game.playersFrozen()) return;
        Player player = event.getPlayer();
        WWPlayer participant = game.getPlayer(player.getName());
        if (participant == null) return;
        if (participant == game.getAccused() && participant.isGhost()) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPaintingBreak(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBellRing(BellRingEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onSpawnChange(PlayerSpawnChangeEvent event) {
        if (event.getCause() == PlayerSpawnChangeEvent.Cause.BED) {
            event.setCancelled(true);
        }
    }
}
