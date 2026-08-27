package io.github.gegagegagega.werewolf.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.Perm;
import io.github.gegagegagega.werewolf.Role;
import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class BlueyCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("bluey")
            .requires(source -> {
                if( source.getSender() instanceof Player p){
                    if(plugin.permMap.get(p.getName())==Perm.DEV)return true;
                    return plugin.permMap.get(p.getName())==Perm.HOST;
                }
                return false;
        })
                        .then( Commands.argument("value", BoolArgumentType.bool())
                    .executes(ctx -> {

                        boolean value =
                            BoolArgumentType.getBool(ctx, "value");
                            plugin.blueyMode=value;
                                        if (ctx.getSource().getSender() instanceof Player player) {
                                            
                            if(value)
                                            player.sendMessage(Component.text("bluey :)", NamedTextColor.BLUE));
                                        
                            else
                                            player.sendMessage(Component.text("no bluey :(", NamedTextColor.BLUE));
                                        }
                                    

                                    return Command.SINGLE_SUCCESS;
                                }
                            )
                        );
    }
}
