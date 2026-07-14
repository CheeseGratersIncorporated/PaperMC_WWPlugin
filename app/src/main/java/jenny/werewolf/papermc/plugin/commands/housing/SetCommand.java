package jenny.werewolf.papermc.plugin.commands.housing;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import jenny.werewolf.papermc.plugin.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import jenny.werewolf.papermc.plugin.ExamplePlugin;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class SetCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(ExamplePlugin plugin) {
        return Commands.literal("set")
        .then(Commands.argument("name", StringArgumentType.word())
                        // .suggests((ctx, builder) -> {
                        //     for (String name : plugin.gameInstance.getPlayerNames()) {
                        //         builder.suggest(name);
                        //     }
                        //     return builder.buildFuture();
                        // })
            .executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "name");

                if (ctx.getSource().getSender() instanceof Player player) {
                    
                    if (plugin.housingManager.getHouseNames().contains(name)) {
                        plugin.housingManager.delete(name);
                    }plugin.housingManager.set(player.getLocation(), name);
                    player.sendMessage(Component.text("Set house " + name, NamedTextColor.GREEN));
                    
                }
                return Command.SINGLE_SUCCESS;
            }));
    }
}
