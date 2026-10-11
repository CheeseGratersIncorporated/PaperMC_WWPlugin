package io.github.gegagegagega.werewolf.commands;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.Perm;
import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/** {@code /ww perms <dev|host|none> <player>}, console only. */
public class PermsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("perms")
                .requires(source -> source.getSender() instanceof ConsoleCommandSender)
                .then(Commands.argument("perm", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("dev");
                            builder.suggest("host");
                            builder.suggest("none");
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (Player p : plugin.getServer().getOnlinePlayers()) {
                                        builder.suggest(p.getName());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    String perm = StringArgumentType.getString(ctx, "perm");
                                    Player target = plugin.getServer().getPlayerExact(StringArgumentType.getString(ctx, "player"));
                                    if (target == null) {
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    switch (perm) {
                                        case "dev" -> {
                                            plugin.setPerm(target, Perm.DEV);
                                            target.sendMessage(Component.text("You are now a dev!", NamedTextColor.GREEN));
                                        }
                                        case "host" -> {
                                            plugin.setPerm(target, Perm.HOST);
                                            target.sendMessage(Component.text("You are now a host!", NamedTextColor.GREEN));
                                        }
                                        case "none" -> {
                                            plugin.setPerm(target, Perm.NONE);
                                            target.sendMessage(Component.text("You are now a none!", NamedTextColor.GREEN));
                                        }
                                        default -> target.sendMessage(Component.text("Invalid perm!", NamedTextColor.RED));
                                    }
                                    target.updateCommands();
                                    return Command.SINGLE_SUCCESS;
                                })));
    }
}
