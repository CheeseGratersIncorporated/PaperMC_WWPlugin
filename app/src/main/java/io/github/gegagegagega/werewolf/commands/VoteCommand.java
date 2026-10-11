package io.github.gegagegagega.werewolf.commands;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/** {@code /ww vote [player]}: vote someone up to the stand, or withdraw your vote. */
public class VoteCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("vote")
                .requires(Access.participant(plugin, participant -> participant.isAlive() && plugin.game().canVote()))
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        Game game = plugin.game();
                        WWPlayer voter = game.getPlayer(player.getName());
                        if (voter == null) {
                            player.sendMessage(Component.text("You are not in the game!", NamedTextColor.RED));
                        } else if (game.canVote()) {
                            game.vote(voter, null);
                            player.sendMessage(Component.text("You have cancelled your vote", NamedTextColor.GREEN));
                        } else {
                            player.sendMessage(Component.text("You cannot vote at this time!", NamedTextColor.RED));
                        }
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            if (plugin.game().canVote()) {
                                for (String name : plugin.game().getPlayerNames()) {
                                    builder.suggest(name);
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            if (ctx.getSource().getSender() instanceof Player player) {
                                vote(plugin, player, name);
                            }
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private static void vote(WerewolfPlugin plugin, Player player, String name) {
        Game game = plugin.game();
        WWPlayer voter = game.getPlayer(player.getName());
        if (voter == null) {
            player.sendMessage(Component.text("You are not in the game!", NamedTextColor.RED));
            return;
        }
        if (!game.canVote()) {
            player.sendMessage(Component.text("You cannot vote at this time!", NamedTextColor.RED));
            return;
        }
        WWPlayer target = game.getPlayer(name);
        if (target == null) {
            player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return;
        }
        if (!voter.isAlive()) {
            player.sendMessage(Component.text("Dead people have no voting rights", NamedTextColor.RED));
            return;
        }
        if (!target.isAlive()) {
            player.sendMessage(Component.text("You cannot vote for dead people.", NamedTextColor.RED));
            return;
        }
        switch (game.vote(voter, target)) {
            case CHANGED, MAJORITY -> {
                player.sendMessage(Component.text("You are now voting for " + name, NamedTextColor.GREEN));
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.playSound(online.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }
            }
            case UNCHANGED -> player.sendMessage(Component.text("You are already voting for " + name, NamedTextColor.RED));
            case NOT_ALLOWED -> player.sendMessage(Component.text("You cannot vote at this time!", NamedTextColor.RED));
        }
    }
}
