package io.github.gegagegagega.werewolf.commands;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BlueyCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("bluey")
                .requires(Access.host(plugin))
                .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(ctx -> {
                            boolean value = BoolArgumentType.getBool(ctx, "value");
                            plugin.setBlueyMode(value);
                            if (ctx.getSource().getSender() instanceof Player player) {
                                player.sendMessage(Component.text(value ? "bluey :)" : "no bluey :(", NamedTextColor.BLUE));
                            }
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}
