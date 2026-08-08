package jenny.werewolf.papermc.plugin.commands.scene;

import org.bukkit.entity.Player;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import jenny.werewolf.papermc.plugin.ExamplePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;

public class TpCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(ExamplePlugin plugin) {
        return Commands.literal("tp")
        .then(Commands.argument("name", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                            for (String name : plugin.sceneManager.getLocations().keySet()) {
                                builder.suggest(name);
                            }
                            return builder.buildFuture();
                        })
            .executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "name");

                if (ctx.getSource().getSender() instanceof Player player) {
                    
                    if (plugin.sceneManager.getLocations().keySet().contains(name)) {
                        player.setAllowFlight(true);
                        player.setFlying(true);
                        player.teleport(plugin.sceneManager.getLocation(name));
                    }
                    
                    player.sendMessage(Component.text("tp go :)", NamedTextColor.YELLOW));
                    
                }
                return Command.SINGLE_SUCCESS;
            }));
    }
}
