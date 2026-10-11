package io.github.gegagegagega.werewolf.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.github.gegagegagega.werewolf.housing.House;
import io.github.gegagegagega.werewolf.scene.SceneKeys;
import net.kyori.adventure.text.Component;

/** Joining and leaving the server, including rejoining in the middle of a game. */
public final class ConnectionListener implements Listener {

    private final WerewolfPlugin plugin;

    public ConnectionListener(WerewolfPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.game();

        player.sendMessage(Component.text("Hello, " + player.getName() + "!"));
        Bukkit.getScheduler().runTaskLater(plugin, plugin.housing()::refreshVisibility, 5);
        Bukkit.getScheduler().runTask(plugin, ConnectionListener::shuffleTabList);
        player.updateCommands();

        // Real tab entries are hidden; the fake tab list shows participants instead.
        for (Player other : Bukkit.getOnlinePlayers()) {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                viewer.unlistPlayer(other);
            }
        }
        plugin.skins().forget(player);
        plugin.sync().forget(player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.fakeTab().showAllTo(player);
            }
            plugin.fakeTab().refreshAll();
        }, 5L);

        if (!game.isActive()) {
            player.getWorld().setGameRule(GameRules.PVP, false);
            player.getInventory().clear();
            player.setGameMode(GameMode.ADVENTURE);
            plugin.sync().syncAll();
            return;
        }

        event.joinMessage(null);
        WWPlayer participant = game.getPlayer(player.getName());
        if (participant == null) {
            // Joined mid-game: watch as a spectator.
            Location spawn = plugin.scene().getLocation(SceneKeys.DEFAULT_SPAWN);
            if (spawn != null) {
                player.setRespawnLocation(spawn, true);
            }
            game.addPlayer(player.getName());
            plugin.inventories().prepareInventory(player);
            player.kill();
            return;
        }

        plugin.inventories().prepareInventory(player);
        House house = plugin.housing().houseOf(player.getName());
        if (house != null) {
            player.setRespawnLocation(house.location, true);
        }
        if (!participant.isAlive()) {
            if (!participant.isGhost()) {
                // Died while offline.
                player.kill();
            }
        } else {
            plugin.effects().placement().placeRejoining(player, participant);
        }
        plugin.sync().syncAll();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!plugin.game().isActive()) {
            Bukkit.getServer().broadcast(Component.text("We didn't like " + event.getPlayer().getName() + " anyways"));
        } else {
            event.quitMessage(null);
        }
        plugin.sync().forget(event.getPlayer());
        Bukkit.getScheduler().runTask(plugin, ConnectionListener::shuffleTabList);
    }

    public static void shuffleTabList() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(players);
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setPlayerListOrder(i + 1);
        }
    }
}
