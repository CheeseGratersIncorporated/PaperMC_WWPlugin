package io.github.gegagegagega.werewolf.commands.housing;

import com.mojang.brigadier.tree.LiteralCommandNode;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class HousingCommand {
    public static LiteralCommandNode<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("housing")
        .requires(source -> {
                
                if( source.getSender() instanceof Player p){
                    if(plugin.isDev(p))return true;
                }
                else return source.getSender() instanceof ConsoleCommandSender;
                return false;
        })
            .then(DeleteCommand.create(plugin))
            .then(SetCommand.create(plugin))
            .then(ViewCommand.create(plugin))
            .then(ClaimCommand.create(plugin))
            .then(LecternCommand.create(plugin))
            .then(InteriorCommand.create(plugin))
            .then(DoorCommand.create(plugin))
            .then(TpoutCommand.create(plugin))
            .then(SaveCommand.create(plugin))
            .then(LoadCommand.create(plugin))
            // .then(VoteCommand.create(plugin))
            // .then(TargetCommand.create(plugin))
            .build();
    }
}
