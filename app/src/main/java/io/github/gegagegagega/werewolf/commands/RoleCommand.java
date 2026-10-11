package io.github.gegagegagega.werewolf.commands;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.github.gegagegagega.werewolf.core.role.Role;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/** {@code /ww role}: reminds you of your role. */
public class RoleCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("role")
                .requires(source -> Access.active(plugin)
                        || (source.getSender() instanceof Player p && plugin.isDev(p)))
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        WWPlayer participant = plugin.game().getPlayer(player.getName());
                        if (participant == null) {
                            player.sendMessage(Component.text("You are not in the game!", NamedTextColor.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        Role role = participant.getRole();
                        if (role == null) {
                            player.sendMessage(Component.text("You do not have a role assigned!", NamedTextColor.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        player.sendMessage(Component.text("Your role is: ", NamedTextColor.YELLOW).append(role.title()));
                        player.sendMessage(Component.text("Description: ", NamedTextColor.YELLOW).append(Component.text(role.description())));
                    }
                    return Command.SINGLE_SUCCESS;
                });
    }
}
