package jenny.werewolf.papermc.plugin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.Command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;

public class TestCommandTree {
    public static LiteralCommandNode<CommandSourceStack> createCommand(final String commandName) {
    return Commands.literal(commandName)
        .then(Commands.argument("target", ArgumentTypes.player())
            .executes(ctx -> {
                final PlayerSelectorArgumentResolver playerSelector = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                final Player targetPlayer = playerSelector.resolve(ctx.getSource()).getFirst();
                final CommandSender sender = ctx.getSource().getSender();

                targetPlayer.sendPlainMessage(sender.getName() + " started partying with you!");
                sender.sendPlainMessage("You are now partying with " + targetPlayer.getName() + "!");

                return Command.SINGLE_SUCCESS;
            }))
        .build();
}
}
