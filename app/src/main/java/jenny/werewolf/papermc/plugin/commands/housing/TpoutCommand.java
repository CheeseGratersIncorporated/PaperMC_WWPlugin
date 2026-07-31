package jenny.werewolf.papermc.plugin.commands.housing;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import jenny.werewolf.papermc.plugin.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import jenny.werewolf.papermc.plugin.ExamplePlugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class TpoutCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(ExamplePlugin plugin) {
        return Commands.literal("tpout")
        .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (String name : plugin.housingManager.getHouseNames()) {
                                builder.suggest(name);
                            }
                            return builder.buildFuture();
                        })

        .executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "name");

                        CommandSender sender = ctx.getSource().getSender();

                        if (!(sender instanceof Player player)) {
                            sender.sendMessage("This command must be run by a player.");
                           
                            return Command.SINGLE_SUCCESS;
                        }


                                // Save or manipulate the lectern here
                                // plugin.housingManager.setLectern(name, block);
                            if (!plugin.housingManager.getHouseNames().contains(name)) {
                                
                                player.sendMessage(Component.text("House not found.", NamedTextColor.RED));
                                    return Command.SINGLE_SUCCESS;
                            }
                            
                            
                            plugin.housingManager.setTpout(player.getLocation(), name);
                            
                            player.sendMessage(Component.text(
                                    "Registered tpout for " + name
                                , NamedTextColor.GREEN));
                                  

                                return Command.SINGLE_SUCCESS;
                            })
                        );
    }
}
