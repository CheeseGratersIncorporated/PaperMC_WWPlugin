package io.github.gegagegagega.werewolf.commands;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class JoinCommand {
    private static final int MAX_PLAYERS = 20;

    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("join")
                .requires(Access.player(plugin, p -> !Access.active(plugin) && !plugin.game().hasPlayer(p.getName())))
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        if (plugin.game().hasPlayer(player.getName())) {
                            player.sendMessage(Component.text("You are already in the game!", NamedTextColor.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        if (plugin.game().getPlayers().size() >= MAX_PLAYERS) {
                            player.sendMessage(Component.text("Player count capped at " + MAX_PLAYERS, NamedTextColor.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        plugin.game().addPlayer(player.getName());
                        plugin.housing().addViewer(player);
                        player.updateCommands();
                        player.sendMessage(Component.text("You have joined the game!", NamedTextColor.GREEN));
                    }
                    return Command.SINGLE_SUCCESS;
                });
    }
}
