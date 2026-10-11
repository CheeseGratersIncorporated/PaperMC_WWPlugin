package io.github.gegagegagega.werewolf.commands;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/** {@code /ww claim}: claim the house whose beam you're standing in. */
public class ClaimCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("claim")
                .requires(Access.participant(plugin, participant -> !Access.active(plugin)))
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        if (!plugin.game().hasPlayer(player.getName())) {
                            player.sendMessage(Component.text("You are not in the game.", NamedTextColor.RED));
                        } else if (!plugin.housing().claim(player)) {
                            player.sendMessage(Component.text("Claim failed.", NamedTextColor.RED));
                        } else {
                            player.sendMessage(Component.text("You have claimed " + plugin.housing().getPlayerClaim(player), NamedTextColor.GREEN));
                        }
                    }
                    return Command.SINGLE_SUCCESS;
                });
    }
}
