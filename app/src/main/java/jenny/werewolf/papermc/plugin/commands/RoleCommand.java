package jenny.werewolf.papermc.plugin.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import jenny.werewolf.papermc.plugin.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import jenny.werewolf.papermc.plugin.WerewolfPlugin;
import jenny.werewolf.papermc.plugin.Perm;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class RoleCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("role")
        .requires(source -> {
                if( source.getSender() instanceof Player p){
                    if(plugin.permMap.get(p.getName())==Perm.DEV)return true;
                    
                return plugin.gameInstance.isActive();
                }
                return plugin.gameInstance.isActive();
        })
            
            .executes(ctx -> {
                if (ctx.getSource().getSender() instanceof Player player) {
                    if (plugin.gameInstance.getWWPlayer(player.getName()) == null) {
                        player.sendMessage(Component.text("You are not in the game!", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    if (plugin.gameInstance.getWWPlayerRole(player.getName()) == null) {
                        player.sendMessage(Component.text("You do not have a role assigned!", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    // Additional role command logic here
                    String roleName = plugin.gameInstance.getWWPlayerRole(player.getName()).getName();
                    player.sendMessage(Component.text("Your role is: ", NamedTextColor.YELLOW).append(plugin.gameInstance.getWWPlayerRole(player.getName()).title));
                    player.sendMessage(Component.text("Description: ", NamedTextColor.YELLOW).append(Component.text(plugin.gameInstance.getWWPlayerRole(player.getName()).getName())));
                }
                return Command.SINGLE_SUCCESS;
            });
    }
}
