package io.github.gegagegagega.werewolf.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;

import io.github.gegagegagega.werewolf.Perm;
import io.github.gegagegagega.werewolf.Role;
import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class VoteCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("vote")
        .requires(source -> {
                if( source.getSender() instanceof Player p){
                    if(plugin.permMap.get(p.getName())==Perm.DEV)return true;
                    return plugin.gameInstance.getWWPlayer(p.getName()) != null && plugin.gameInstance.getWWPlayer(p.getName()).isAlive() && plugin.gameInstance.isActive()&&plugin.gameInstance.canVote();
                }
                return false;
        })
            .executes(ctx -> {
                if (ctx.getSource().getSender() instanceof Player player) {
                    if (plugin.gameInstance.getWWPlayer(player.getName()) == null) {
                        player.sendMessage(Component.text("You are not in the game!", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    
                    if(plugin.gameInstance.canVote()){
                        plugin.gameInstance.setWWPlayerVote(plugin.gameInstance.getWWPlayer(player.getName()), null);
                        player.sendMessage(Component.text("You have cancelled your vote", NamedTextColor.GREEN));
                        
                        return Command.SINGLE_SUCCESS;
                    }
                    // if (plugin.gameInstance.canJudge()) {
                    //     plugin.gameInstance.setWWPlayerJudgment(plugin.gameInstance.getWWPlayer(player.getName()), 0);
                    //     player.sendMessage(Component.text("You are abstaining", NamedTextColor.BLUE));
                        
                    //     return Command.SINGLE_SUCCESS;
                    // }
                    // if (!plugin.gameInstance.canVote()) {
                        player.sendMessage(Component.text("You cannot vote at this time!", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    // }
                    }
                return Command.SINGLE_SUCCESS;
            })
        .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            if(plugin.gameInstance.canVote()){
                                for (String name : plugin.gameInstance.getWWPlayerNames()) {
                                    builder.suggest(name);
                                }
                            }
                            // else if(plugin.gameInstance.canJudge()){
                            //     builder.suggest("INNOCENT");
                            //     builder.suggest("GUILTY");
                            //     builder.suggest("ABSTAIN");
                            // }
                            
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
                    if (plugin.gameInstance.canVote()) {
                        if (!plugin.gameInstance.getWWPlayerNames().contains(name)) {
                            player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        if (!plugin.gameInstance.getWWPlayer(player.getName()).isAlive()) {
                            player.sendMessage(Component.text("Dead people have no voting rights", NamedTextColor.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        if (!plugin.gameInstance.getWWPlayer(name).isAlive()) {
                            player.sendMessage(Component.text("You cannot vote for dead people.", NamedTextColor.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        if(plugin.gameInstance.setWWPlayerVote(plugin.gameInstance.getWWPlayer(player.getName()), plugin.gameInstance.getWWPlayer(name))) {
                            player.sendMessage(Component.text("You are now voting for " + name, NamedTextColor.GREEN));
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.playSound(
                                    p.getLocation(),
                                    Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                                    1.0f, // volume
                                    1.0f  // pitch
                                );
                            }
                            return Command.SINGLE_SUCCESS;
                        }else {
                            player.sendMessage(Component.text("You are already voting for " + name, NamedTextColor.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                    }
                    // else if(plugin.gameInstance.canJudge()){
                        
                    //     if (!plugin.gameInstance.getWWPlayer(player.getName()).isAlive()) {
                    //         player.sendMessage(Component.text("Dead people have no voting rights", NamedTextColor.RED));
                    //         return Command.SINGLE_SUCCESS;
                    //     }
                    //     if (plugin.gameInstance.getSentenced()!=null&&plugin.gameInstance.getSentenced().getName().equals(player.getName())) {
                    //         player.sendMessage(Component.text("You're on trial!", NamedTextColor.RED));
                    //         return Command.SINGLE_SUCCESS;
                    //     }
                    //     if (name.toLowerCase().equals("innocent")){
                    //         plugin.gameInstance.setWWPlayerJudgment(plugin.gameInstance.getWWPlayer(player.getName()),-1);
                    //         player.sendMessage(Component.text("You have judged ", NamedTextColor.YELLOW).append(Component.text("INNOCENT",NamedTextColor.GREEN)));
                    //          return Command.SINGLE_SUCCESS;
                    //     }
                    //     if(name.toLowerCase().equals("guilty")){
                    //         plugin.gameInstance.setWWPlayerJudgment(plugin.gameInstance.getWWPlayer(player.getName()),1);
                    //         player.sendMessage(Component.text("You have judged ", NamedTextColor.YELLOW).append(Component.text("GUILTY",NamedTextColor.RED)));
                    //          return Command.SINGLE_SUCCESS;
                    //     }
                    //     if(name.toLowerCase().equals("abstain")) {
                    //     plugin.gameInstance.setWWPlayerJudgment(plugin.gameInstance.getWWPlayer(player.getName()), 0);
                    //     player.sendMessage(Component.text("You are abstaining", NamedTextColor.BLUE));
                    //          return Command.SINGLE_SUCCESS;
                    //     }
                        
                    //     player.sendMessage(Component.text("Invalid choice", NamedTextColor.RED));
                    //         return Command.SINGLE_SUCCESS;
                    // }
                    
                    player.sendMessage(Component.text("You cannot vote at this time!", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                }
                return Command.SINGLE_SUCCESS;
            }));
    }
}
