package jenny.werewolf.papermc.plugin.commands.scene;

import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import jenny.werewolf.papermc.plugin.ExamplePlugin;
import jenny.werewolf.papermc.plugin.Perm;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class SceneCommand {
    public static LiteralCommandNode<CommandSourceStack> create(ExamplePlugin plugin) {
        return Commands.literal("scene")
        .requires(source -> {
                if( source.getSender() instanceof Player p){
                    if(plugin.permMap.get(p.getName())==Perm.DEV)return true;
                    }
                else return source.getSender() instanceof ConsoleCommandSender;
                return false;
        })
            .then(SetCommand.create(plugin))
            .then(LoadCommand.create(plugin))
            .then(SaveCommand.create(plugin))
            .then(TpCommand.create(plugin))
            .build();
    }
}
