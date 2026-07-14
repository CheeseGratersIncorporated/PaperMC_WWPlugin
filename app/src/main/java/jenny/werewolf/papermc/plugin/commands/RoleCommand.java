package jenny.werewolf.papermc.plugin.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import jenny.werewolf.papermc.plugin.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import jenny.werewolf.papermc.plugin.ExamplePlugin;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class RoleCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(ExamplePlugin plugin) {
        return Commands.literal("role")
            .executes(ctx -> {
                if (ctx.getSource().getSender() instanceof Player player) {
                    if (plugin.gameInstance.getPlayer(player.getName()) == null) {
                        player.sendMessage(Component.text("You are not in the game!", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    if (plugin.gameInstance.getPlayerRole(player.getName()) == null) {
                        player.sendMessage(Component.text("You do not have a role assigned!", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    // Additional role command logic here
                    String roleName = plugin.gameInstance.getPlayerRole(player.getName()).getName();
                    player.sendMessage(Component.text("Your role is: " + roleName, NamedTextColor.GREEN));
                }
                return Command.SINGLE_SUCCESS;
            });
    }
}
