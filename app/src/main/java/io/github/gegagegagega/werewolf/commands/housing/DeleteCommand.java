package io.github.gegagegagega.werewolf.commands.housing;

import org.bukkit.entity.Player;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import com.mojang.brigadier.Command;

public class DeleteCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("delete")
        .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (String name : plugin.housing().getHouseNames()) {
                                builder.suggest(name);
                            }
                            return builder.buildFuture();
                        })
            .executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "name");

                if (ctx.getSource().getSender() instanceof Player player) {
                    
                    if (plugin.housing().getHouseNames().contains(name)) {
                        plugin.housing().delete(name);
                        player.sendMessage(Component.text("Deleted house " + name, NamedTextColor.GREEN));
                    } else {
                        player.sendMessage(Component.text("House not found!", NamedTextColor.RED));
                    }
                }
                return Command.SINGLE_SUCCESS;
            }));
    }
}
