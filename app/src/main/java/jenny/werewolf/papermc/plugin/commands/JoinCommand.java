package jenny.werewolf.papermc.plugin.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import jenny.werewolf.papermc.plugin.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import jenny.werewolf.papermc.plugin.ExamplePlugin;
import jenny.werewolf.papermc.plugin.Perm;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class JoinCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(ExamplePlugin plugin) {
        return Commands.literal("join")
        .requires(source -> {
                if( source.getSender() instanceof Player p){
                    if(plugin.permMap.get(p.getName())==Perm.DEV)return true;
                    return !plugin.gameInstance.isActive()&&plugin.gameInstance.getWWPlayer(p.getName())==null&&plugin.gameInstance.getWWPlayerList().size()<20;
                }
                return false;
        })
            .executes(ctx -> {
                if (ctx.getSource().getSender() instanceof Player player) {
                    if (plugin.gameInstance.getWWPlayer(player.getName()) != null) {
                        player.sendMessage(Component.text("You are already in the game!", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    plugin.gameInstance.addWWPlayer(player.getName());
                    plugin.housingManager.addViewer(player);
                    player.updateCommands();
                    player.sendMessage(Component.text("You have joined the game!", NamedTextColor.GREEN));
                }
                return Command.SINGLE_SUCCESS;
            });
    }
}
