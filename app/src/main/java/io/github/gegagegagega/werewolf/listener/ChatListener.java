package io.github.gegagegagega.werewolf.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.github.gegagegagega.werewolf.core.ChatChannel;
import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.GamePhase;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Applies each player's {@link ChatChannel} to chat, and polices whispers.
 * Which channel a player is in is decided by {@link Game#chatChannel}.
 */
public final class ChatListener implements Listener {

    private final WerewolfPlugin plugin;

    public ChatListener(WerewolfPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Game game = plugin.game();
        if (!game.isActive()) return;
        WWPlayer sender = game.getPlayer(event.getPlayer().getName());
        if (sender == null) return;

        ChatChannel channel = game.chatChannel(sender);
        if (channel == ChatChannel.PUBLIC) return;

        event.viewers().removeIf(audience -> {
            if (!(audience instanceof Player recipient)) {
                return true;
            }
            WWPlayer recipientWW = game.getPlayer(recipient.getName());
            return recipientWW == null || !canHear(channel, recipientWW);
        });

        Component prefix = prefix(channel);
        event.renderer((source, sourceName, message, viewer) -> Component.empty()
                .append(prefix)
                .append(sourceName)
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(message));
    }

    private static boolean canHear(ChatChannel channel, WWPlayer recipient) {
        return switch (channel) {
            case PUBLIC -> true;
            case MUTED -> false;
            case DEAD -> !recipient.isAlive();
            case WEREWOLVES -> recipient.isWerewolf();
        };
    }

    private static Component prefix(ChatChannel channel) {
        return switch (channel) {
            case DEAD -> Component.text("[DEAD]", NamedTextColor.GRAY);
            case WEREWOLVES -> Component.text("[WEREWOLF]", NamedTextColor.RED);
            case MUTED -> Component.text("[ERROR]", NamedTextColor.RED);
            case PUBLIC -> Component.empty();
        };
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().split(" ");
        String command = args[0].toLowerCase();
        if (args.length < 2) return;
        Game game = plugin.game();
        Player player = event.getPlayer();

        if (command.equals("/w") || command.equals("/msg")) {
            GamePhase phase = game.getPhase();
            if (game.isActive() && phase != null && phase.isNight()) {
                event.setCancelled(true);
                player.sendMessage(Component.text("You cannot whisper at night.", NamedTextColor.RED));
                return;
            }
            WWPlayer participant = game.getPlayer(player.getName());
            if (game.isActive() && participant != null && !participant.isAlive()) {
                event.setCancelled(true);
                player.sendMessage(Component.text("You're dead.", NamedTextColor.RED));
                return;
            }
            if (Bukkit.getPlayerExact(args[1]) == null) {
                return;
            }
            Bukkit.getServer().broadcast(Component.text(
                    player.getName() + " is whispering to " + args[1], NamedTextColor.GRAY));
        }
        if (command.equals("/tell") || command.equals("/teammsg") || command.equals("/tm")) {
            event.setCancelled(true);
            player.sendMessage(Component.text("stop.", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        event.getCommands().remove("tm");
        event.getCommands().remove("tell");
        event.getCommands().remove("teammsg");
    }
}
