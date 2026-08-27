package io.github.gegagegagega.werewolf.commands.housing;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;

import io.github.gegagegagega.werewolf.Role;
import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class ClaimCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("claim")
            .executes(ctx -> {
                if (ctx.getSource().getSender() instanceof Player player) {
                    if(plugin.gameInstance.getWWPlayer(player.getName())==null){
                        player.sendMessage(Component.text("You are not in the game.", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    if (!plugin.housingManager.claim(player)) {
                        player.sendMessage(Component.text("Claim failed.", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }else player.sendMessage(Component.text("You have claimed " + plugin.housingManager.getPlayerClaim(player), NamedTextColor.GREEN));
                }
                return Command.SINGLE_SUCCESS;
            })
            
            .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (String name : plugin.housingManager.getHouseNames()) {
                                builder.suggest(name);
                            }
                            return builder.buildFuture();
                        })
            .executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "name");
                
                if (ctx.getSource().getSender() instanceof Player player) {
                    if(plugin.gameInstance.getWWPlayer(player.getName())==null){
                        player.sendMessage(Component.text("You are not in the game.", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    if(!plugin.housingManager.getHouseNames().contains(name)){
                    player.sendMessage(Component.text("House not found!", NamedTextColor.RED));
                    
                    return Command.SINGLE_SUCCESS;
                    }
                    if (plugin.housingManager.claim(player,name)) {
                        player.sendMessage(Component.text("You have claimed " + plugin.housingManager.getPlayerClaim(player), NamedTextColor.GREEN));
                    } else {
                        player.sendMessage(Component.text("Claim failed.", NamedTextColor.RED));
                    }
                }
                return Command.SINGLE_SUCCESS;
            }));
    }
}
