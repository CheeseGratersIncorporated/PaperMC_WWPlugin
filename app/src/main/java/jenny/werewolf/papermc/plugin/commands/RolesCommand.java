package jenny.werewolf.papermc.plugin.commands;

import jenny.werewolf.papermc.plugin.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import jenny.werewolf.papermc.plugin.ExamplePlugin;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class RolesCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(ExamplePlugin plugin) {
        return Commands.literal("roles")
            
        
        
        
        .then(Commands.literal("list")
            .executes(ctx -> {
                StringBuilder sb = new StringBuilder();
                sb.append("Current roles:\n");
                for (String role : plugin.gameInstance.getRoles().keySet()) {
                    int count = plugin.gameInstance.getRoles().get(role);
                    sb.append(role).append(": ").append(count).append("\n");
                }
                if (ctx.getSource().getSender() instanceof Player player) {
                    player.sendMessage(Component.text(sb.toString(), NamedTextColor.GREEN));
                }
                return Command.SINGLE_SUCCESS;
            })
        )
        //SET COMMAND
        .then(
                Commands.literal("set")
                    .then(Commands.argument("role", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (String role : Role.names()) {
                                builder.suggest(role);
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument(
                                "count",
                                IntegerArgumentType.integer(0))
                            .executes(ctx -> {

                                    String role =
                                        StringArgumentType.getString(ctx, "role");

                                    int count =
                                        IntegerArgumentType.getInteger(ctx, "count");
                                    if(Role.names().contains(role)) {
                                        plugin.gameInstance.setRole(role, count);
                                        if (ctx.getSource().getSender() instanceof Player player) {
                                            player.sendMessage(Component.text("Set role " + role + " to count " + count, NamedTextColor.GREEN));
                                        }
                                    } else {
                                        if (ctx.getSource().getSender() instanceof Player player) {
                                            player.sendMessage(Component.text("Invalid role: " + role, NamedTextColor.RED));
                                        }
                                    }

                                    return Command.SINGLE_SUCCESS;
                                }
                            )
                        )
                    )
            );
    }
}
