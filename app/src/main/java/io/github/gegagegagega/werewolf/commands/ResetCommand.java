package io.github.gegagegagega.werewolf.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;

import io.github.gegagegagega.werewolf.Perm;
import io.github.gegagegagega.werewolf.Role;
import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class ResetCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("reset")
        .requires(source -> {
                if( source.getSender() instanceof Player p){
                    if(plugin.permMap.get(p.getName())==Perm.DEV)return true;
                    return plugin.permMap.get(p.getName())==Perm.HOST;
                }
                return false;
        })
            .executes(ctx -> {
                    
                
                plugin.reset();
                return Command.SINGLE_SUCCESS;
            });
    }
}
