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

public class SaveCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(ExamplePlugin plugin) {
        return Commands.literal("save")
            .executes(ctx -> {

                if (ctx.getSource().getSender() instanceof Player player) {
                    
                    plugin.sceneManager.save();
                    player.sendMessage(Component.text("saved" , NamedTextColor.GREEN));
                    
                }
                return Command.SINGLE_SUCCESS;
            });
    }
}
