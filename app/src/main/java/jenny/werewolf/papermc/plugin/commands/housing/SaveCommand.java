package jenny.werewolf.papermc.plugin.commands.housing;

import org.bukkit.entity.Player;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import jenny.werewolf.papermc.plugin.WerewolfPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import com.mojang.brigadier.Command;

public class SaveCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("save")
            .executes(ctx -> {
                if (ctx.getSource().getSender() instanceof Player player) {
                    plugin.housingDataManager.houses=plugin.housingManager.getHouses();
                    plugin.housingDataManager.save();
                    player.sendMessage(Component.text("Saved", NamedTextColor.GREEN));
                }
                return Command.SINGLE_SUCCESS;
            });
    }
}
