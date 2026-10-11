package io.github.gegagegagega.werewolf.commands;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/** {@code /ww target [player]}: choose who your night ability is used on. */
public class TargetCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("target")
                .requires(Access.player(plugin, p -> Access.active(plugin)))
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        WWPlayer participant = plugin.game().getPlayer(player.getName());
                        if (participant == null) {
                            player.sendMessage(Component.text("You are not in the game!", NamedTextColor.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        plugin.game().setTarget(participant, null);
                        player.sendMessage(Component.text("You are no longer targeting anyone", NamedTextColor.GREEN));
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            if (ctx.getSource().getSender() instanceof Player player) {
                                Game game = plugin.game();
                                WWPlayer participant = game.getPlayer(player.getName());
                                if (participant != null && participant.getRole() != null) {
                                    for (WWPlayer candidate : game.getPlayers()) {
                                        if (participant.getRole().canTarget(game, candidate)) {
                                            builder.suggest(candidate.getName());
                                        }
                                    }
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            if (ctx.getSource().getSender() instanceof Player player) {
                                Game game = plugin.game();
                                WWPlayer participant = game.getPlayer(player.getName());
                                if (participant == null) {
                                    player.sendMessage(Component.text("You are not in the game!", NamedTextColor.RED));
                                    return Command.SINGLE_SUCCESS;
                                }
                                WWPlayer target = game.getPlayer(name);
                                if (target == null) {
                                    player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                                    return Command.SINGLE_SUCCESS;
                                }
                                player.sendMessage(game.setTarget(participant, target));
                            }
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}
