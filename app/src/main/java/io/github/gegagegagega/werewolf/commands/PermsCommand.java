package io.github.gegagegagega.werewolf.commands;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.Perm;
import io.github.gegagegagega.werewolf.Role;
import io.github.gegagegagega.werewolf.WWPlayer;
import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashMap;

public class PermsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("perms")
        .requires(source ->
                source.getSender() instanceof ConsoleCommandSender
        )
        .then(Commands.argument("perm", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("dev");
                            builder.suggest("host");
                            builder.suggest("none");
                            return builder.buildFuture();
                        })
        .then(Commands.argument("player", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for(Player p : plugin.getServer().getOnlinePlayers()){
                                builder.suggest(p.getName());
                            }
                            return builder.buildFuture();
                        })
            .executes(ctx -> {
                String perm =
                                        StringArgumentType.getString(ctx, "perm");
                String player =
                                        StringArgumentType.getString(ctx, "player");
                for(Player p : plugin.getServer().getOnlinePlayers()){
                    if(p.getName().equals(player)){
                        if(perm.equals("dev")){
                            plugin.permMap.put(p.getName(), Perm.DEV);
                            p.sendMessage(Component.text("You are now a dev!", NamedTextColor.GREEN));
                        }else if(perm.equals("host")){
                            plugin.permMap.put(p.getName(), Perm.HOST);
                            p.sendMessage(Component.text("You are now a host!", NamedTextColor.GREEN));
                        }else if(perm.equals("none")){
                            plugin.permMap.put(p.getName(), Perm.NONE);
                            p.sendMessage(Component.text("You are now a none!", NamedTextColor.GREEN));
                        }else{
                            p.sendMessage(Component.text("Invalid perm!", NamedTextColor.RED));
                        }
                        p.updateCommands();
                    }
                }

                return Command.SINGLE_SUCCESS;
            })));
    }
}
