package jenny.werewolf.papermc.plugin.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import jenny.werewolf.papermc.plugin.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import jenny.werewolf.papermc.plugin.ExamplePlugin;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class VoteCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(ExamplePlugin plugin) {
        return Commands.literal("vote")
            .executes(ctx -> {
                if (ctx.getSource().getSender() instanceof Player player) {
                    if (plugin.gameInstance.getWWPlayer(player.getName()) == null) {
                        player.sendMessage(Component.text("You are not in the game!", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    if (!plugin.gameInstance.canVote()) {
                        player.sendMessage(Component.text("You cannot vote at this time!", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    plugin.gameInstance.setWWPlayerVote(plugin.gameInstance.getWWPlayer(player.getName()), null);
                    player.sendMessage(Component.text("You are no longer voting for anyone", NamedTextColor.GREEN));
                }
                return Command.SINGLE_SUCCESS;
            })
        .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (String name : plugin.gameInstance.getWWPlayerNames()) {
                                builder.suggest(name);
                            }
                            return builder.buildFuture();
                        })
            .executes(ctx -> {
                String name =
                                        StringArgumentType.getString(ctx, "name");

                if (ctx.getSource().getSender() instanceof Player player) {
                    if (plugin.gameInstance.getWWPlayer(player.getName()) == null) {
                        player.sendMessage(Component.text("You are not in the game!", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    if (!plugin.gameInstance.canVote()) {
                        player.sendMessage(Component.text("You cannot vote at this time!", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    if (!plugin.gameInstance.getWWPlayerNames().contains(name)) {
                        player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    if(plugin.gameInstance.setWWPlayerVote(plugin.gameInstance.getWWPlayer(player.getName()), plugin.gameInstance.getWWPlayer(name))) {
                        player.sendMessage(Component.text("You are now voting for " + name, NamedTextColor.GREEN));
                    }else {
                        player.sendMessage(Component.text("You are already voting for " + name, NamedTextColor.RED));
                    }
                }
                return Command.SINGLE_SUCCESS;
            }));
    }
}
