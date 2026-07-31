package jenny.werewolf.papermc.plugin.commands.housing;

import com.mojang.brigadier.tree.LiteralCommandNode;
import jenny.werewolf.papermc.plugin.ExamplePlugin;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class HousingCommand {
    public static LiteralCommandNode<CommandSourceStack> create(ExamplePlugin plugin) {
        return Commands.literal("housing")
            .then(DeleteCommand.create(plugin))
            .then(SetCommand.create(plugin))
            .then(ViewCommand.create(plugin))
            .then(ClaimCommand.create(plugin))
            .then(LecternCommand.create(plugin))
            .then(InteriorCommand.create(plugin))
            .then(DoorCommand.create(plugin))
            .then(TpoutCommand.create(plugin))
            .then(SaveCommand.create(plugin))
            .then(LoadCommand.create(plugin))
            // .then(VoteCommand.create(plugin))
            // .then(TargetCommand.create(plugin))
            .build();
    }
}
