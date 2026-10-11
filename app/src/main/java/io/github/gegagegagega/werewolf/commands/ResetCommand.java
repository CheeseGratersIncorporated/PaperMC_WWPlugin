package io.github.gegagegagega.werewolf.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class ResetCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("reset")
                .requires(Access.host(plugin))
                .executes(ctx -> {
                    plugin.resetGame();
                    return Command.SINGLE_SUCCESS;
                });
    }
}
