package jenny.werewolf.papermc.plugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import jenny.werewolf.papermc.plugin.ExamplePlugin;
import jenny.werewolf.papermc.plugin.Perm;
import jenny.werewolf.papermc.plugin.WWPlayer;
import jenny.werewolf.papermc.plugin.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SpectateCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(ExamplePlugin plugin) {
        return Commands.literal("spectate")
        .requires(source -> {
                if( source.getSender() instanceof Player p){
                    if(plugin.permMap.get(p.getName())==Perm.DEV)return true;
                    return plugin.gameInstance.getWWPlayer(p.getName()) != null && !plugin.gameInstance.getWWPlayer(p.getName()).isAlive()&&plugin.gameInstance.isActive();
                }
                return false;
        })
        .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            if (ctx.getSource().getSender() instanceof Player player) {
                                for (String name : plugin.gameInstance.getWWPlayerNames()) {
                                    if(plugin.gameInstance.getWWPlayer(name).isAlive())
                                    builder.suggest(name);
                                }
                                
                            }
                            return builder.buildFuture();
                        })
            .executes(ctx -> {
                String name =
                                        StringArgumentType.getString(ctx, "name");

                if (ctx.getSource().getSender() instanceof Player player) {
                    if (plugin.gameInstance.getWWPlayer(player.getName()) == null) {
                        player.sendMessage(Component.text("You are not in the game!", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    if (!plugin.gameInstance.getWWPlayerNames().contains(name)) {
                        player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }

                    // if(plugin.gameInstance.setWWPlayerTarget(plugin.gameInstance.getWWPlayer(player.getName()), plugin.gameInstance.getWWPlayer(name))) {
                    //     player.sendMessage(Component.text("You are now targeting " + name, NamedTextColor.GREEN));
                    // }else {
                    //     player.sendMessage(Component.text("You are already targeting " + name, NamedTextColor.RED));
                    // }
                    for(Player p: Bukkit.getOnlinePlayers()){
                        if(p.getName().equals(name)){
                            player.teleport(p);
                            player.sendMessage(Component.text("You are now spectating " + name, NamedTextColor.GREEN));
                            return Command.SINGLE_SUCCESS;
                        }
                    }
                    // player.sendMessage(plugin.gameInstance.setWWPlayerTarget(plugin.gameInstance.getWWPlayer(player.getName()), plugin.gameInstance.getWWPlayer(name)));
                }
                return Command.SINGLE_SUCCESS;
            }));
    }
}
