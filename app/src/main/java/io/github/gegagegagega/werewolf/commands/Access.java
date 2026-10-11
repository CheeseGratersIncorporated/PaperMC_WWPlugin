package io.github.gegagegagega.werewolf.commands;

import java.util.function.Predicate;

import org.bukkit.entity.Player;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.papermc.paper.command.brigadier.CommandSourceStack;

/** Who may see and run each {@code /ww} subcommand. Devs can always run everything. */
final class Access {
    private Access() {
    }

    /** Players matching {@code rule}, plus devs. */
    static Predicate<CommandSourceStack> player(WerewolfPlugin plugin, Predicate<Player> rule) {
        return source -> source.getSender() instanceof Player player
                && (plugin.isDev(player) || rule.test(player));
    }

    static Predicate<CommandSourceStack> host(WerewolfPlugin plugin) {
        return player(plugin, plugin::isHost);
    }

    /** Participants of the game, and devs. */
    static Predicate<CommandSourceStack> participant(WerewolfPlugin plugin, Predicate<WWPlayer> rule) {
        return player(plugin, player -> {
            WWPlayer participant = plugin.game().getPlayer(player.getName());
            return participant != null && rule.test(participant);
        });
    }

    static boolean active(WerewolfPlugin plugin) {
        Game game = plugin.game();
        return game.isActive();
    }
}
