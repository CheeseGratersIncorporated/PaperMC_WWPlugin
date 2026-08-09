package jenny.werewolf.papermc.plugin.commands;

import jenny.werewolf.papermc.plugin.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import jenny.werewolf.papermc.plugin.ExamplePlugin;
import jenny.werewolf.papermc.plugin.Perm;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class BlueyCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(ExamplePlugin plugin) {
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
                            plugin.gameInstance.blueyMode=value;
                                        if (ctx.getSource().getSender() instanceof Player player) {
                                            player.sendMessage(Component.text("bluey :)", NamedTextColor.BLUE));
                                        }
                                    

                                    return Command.SINGLE_SUCCESS;
                                }
                            )
                        );
    }
}
