package jenny.werewolf.papermc.plugin.commands.housing;

import org.bukkit.entity.Player;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import jenny.werewolf.papermc.plugin.ExamplePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import com.mojang.brigadier.Command;

public class ViewCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(ExamplePlugin plugin) {
        return Commands.literal("view")
            .executes(ctx -> {
                if (ctx.getSource().getSender() instanceof Player player) {
                    plugin.housingManager.toggleViewer(player);
                    player.sendMessage(Component.text("Toggled Housing View", NamedTextColor.GREEN));
                }
                return Command.SINGLE_SUCCESS;
            });
    }
}
