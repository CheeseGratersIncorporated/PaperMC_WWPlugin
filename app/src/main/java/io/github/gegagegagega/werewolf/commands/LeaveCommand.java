package io.github.gegagegagega.werewolf.commands;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LeaveCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("leave")
                .requires(Access.participant(plugin, participant -> true))
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        if (!plugin.game().hasPlayer(player.getName())) {
                            player.sendMessage(Component.text("You are not in the game!", NamedTextColor.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        plugin.housing().release(player.getName());
                        plugin.game().removePlayer(player.getName());
                        plugin.housing().removeViewer(player);
                        player.updateCommands();
                        player.sendMessage(Component.text("You have left the game!", NamedTextColor.GREEN));
                    }
                    return Command.SINGLE_SUCCESS;
                });
    }
}
