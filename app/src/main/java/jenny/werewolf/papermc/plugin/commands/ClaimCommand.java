package jenny.werewolf.papermc.plugin.commands;


import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import jenny.werewolf.papermc.plugin.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import jenny.werewolf.papermc.plugin.ExamplePlugin;
import jenny.werewolf.papermc.plugin.Perm;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;


public class ClaimCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(ExamplePlugin plugin) {
        return Commands.literal("claim")
        .requires(source -> {
                if( source.getSender() instanceof Player p){
                    if(plugin.permMap.get(p)==Perm.DEV)return true;
                    return !plugin.gameInstance.isActive()&&plugin.gameInstance.getWWPlayer(p.getName())!=null;
                }
                return false;
        })
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
            });
    }
}
