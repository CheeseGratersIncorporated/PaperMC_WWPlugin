package jenny.werewolf.papermc.plugin.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import jenny.werewolf.papermc.plugin.ExamplePlugin;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import jenny.werewolf.papermc.plugin.commands.housing.HousingCommand;

public class WWCommand {
    public static LiteralCommandNode<CommandSourceStack> create(ExamplePlugin plugin) {
        return Commands.literal("ww")
            .then(RolesCommand.create(plugin))
            .then(StartCommand.create(plugin))
            .then(JoinCommand.create(plugin))
            .then(LeaveCommand.create(plugin))
            .then(RoleCommand.create(plugin))
            .then(HousingCommand.create(plugin))
            .then(VoteCommand.create(plugin))
            .then(TargetCommand.create(plugin))
            .build();
    }
}
