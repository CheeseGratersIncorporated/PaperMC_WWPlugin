package io.github.gegagegagega.werewolf.commands.housing;

import org.bukkit.entity.Player;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import com.mojang.brigadier.Command;

public class SaveCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("save")
            .executes(ctx -> {
                if (ctx.getSource().getSender() instanceof Player player) {
                    plugin.housingStore().houses=plugin.housing().getHouses();
                    plugin.housingStore().save();
                    player.sendMessage(Component.text("Saved", NamedTextColor.GREEN));
                }
                return Command.SINGLE_SUCCESS;
            });
    }
}
