package io.github.gegagegagega.werewolf.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/** {@code /ww spectate <player>}: the dead can teleport to a living player. */
public class SpectateCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("spectate")
                .requires(Access.participant(plugin, participant -> !participant.isAlive() && Access.active(plugin)))
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (WWPlayer participant : plugin.game().getPlayers()) {
                                if (participant.isAlive()) {
                                    builder.suggest(participant.getName());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            if (ctx.getSource().getSender() instanceof Player player) {
                                if (!plugin.game().hasPlayer(player.getName())) {
                                    player.sendMessage(Component.text("You are not in the game!", NamedTextColor.RED));
                                    return Command.SINGLE_SUCCESS;
                                }
                                if (!plugin.game().hasPlayer(name)) {
                                    player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                                    return Command.SINGLE_SUCCESS;
                                }
                                Player target = Bukkit.getPlayerExact(name);
                                if (target != null) {
                                    player.teleport(target);
                                    player.sendMessage(Component.text("You are now spectating " + name, NamedTextColor.GREEN));
                                }
                            }
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}
