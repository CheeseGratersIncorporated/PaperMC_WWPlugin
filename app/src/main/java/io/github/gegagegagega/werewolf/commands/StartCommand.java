package io.github.gegagegagega.werewolf.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class StartCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("start")
                .requires(Access.player(plugin, p -> plugin.isHost(p) && !Access.active(plugin)))
                .executes(ctx -> {
                    plugin.effects().beginStart();
                    return Command.SINGLE_SUCCESS;
                });
    }
}
