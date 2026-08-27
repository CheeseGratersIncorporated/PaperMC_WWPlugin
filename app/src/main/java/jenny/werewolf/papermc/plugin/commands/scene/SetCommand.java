package jenny.werewolf.papermc.plugin.commands.scene;

import org.bukkit.entity.Player;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import jenny.werewolf.papermc.plugin.WerewolfPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;

public class SetCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("set")
        .then(Commands.argument("name", StringArgumentType.word())

            .executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "name");

                if (ctx.getSource().getSender() instanceof Player player) {
                    
                    plugin.sceneManager.setLocation(name, player.getLocation());
                    player.sendMessage(Component.text("Set scene location " + name, NamedTextColor.GREEN));
                    
                }
                return Command.SINGLE_SUCCESS;
            }));
    }
}
