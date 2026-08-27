package io.github.gegagegagega.werewolf.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;

import io.github.gegagegagega.werewolf.Perm;
import io.github.gegagegagega.werewolf.Role;
import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class LeaveCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("leave")
        .requires(source -> {
                if( source.getSender() instanceof Player p){
                    if(plugin.permMap.get(p.getName())==Perm.DEV)return true;
                    return plugin.gameInstance.getWWPlayer(p.getName()) != null;
                }
                return false;
        })
            .executes(ctx -> {
                if (ctx.getSource().getSender() instanceof Player player) {
                    if (plugin.gameInstance.getWWPlayer(player.getName()) == null) {
                        player.sendMessage(Component.text("You are not in the game!", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    plugin.gameInstance.removeWWPlayer(player.getName());
                    plugin.housingManager.removeViewer(player);
                    player.updateCommands();
                    player.sendMessage(Component.text("You have left the game!", NamedTextColor.GREEN));
                }
                return Command.SINGLE_SUCCESS;
            });
    }
}
