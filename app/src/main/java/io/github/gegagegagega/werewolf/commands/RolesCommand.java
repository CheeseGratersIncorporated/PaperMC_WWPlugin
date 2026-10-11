package io.github.gegagegagega.werewolf.commands;

import java.util.Map;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.github.gegagegagega.werewolf.core.role.Roles;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/** {@code /ww roles list} and {@code /ww roles set <role> <count>}. */
public class RolesCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("roles")
                .requires(Access.host(plugin))
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            StringBuilder sb = new StringBuilder("Current roles:\n");
                            for (Map.Entry<String, Integer> entry : plugin.game().getRoleCounts().entrySet()) {
                                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                            }
                            if (ctx.getSource().getSender() instanceof Player player) {
                                player.sendMessage(Component.text(sb.toString(), NamedTextColor.GREEN));
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("set")
                        .requires(Access.player(plugin, p -> plugin.isHost(p) && !Access.active(plugin)))
                        .then(Commands.argument("role", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (String role : Roles.ids()) {
                                        builder.suggest(role);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("count", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            String role = StringArgumentType.getString(ctx, "role");
                                            int count = IntegerArgumentType.getInteger(ctx, "count");
                                            boolean valid = Roles.exists(role);
                                            if (valid) {
                                                plugin.game().setRoleCount(role, count);
                                            }
                                            if (ctx.getSource().getSender() instanceof Player player) {
                                                player.sendMessage(valid
                                                        ? Component.text("Set role " + role + " to count " + count, NamedTextColor.GREEN)
                                                        : Component.text("Invalid role: " + role, NamedTextColor.RED));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))));
    }
}
