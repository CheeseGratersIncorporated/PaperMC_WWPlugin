package jenny.werewolf.papermc.plugin.commands.scene;

import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import jenny.werewolf.papermc.plugin.ExamplePlugin;

public class SceneCommand {
    public static LiteralCommandNode<CommandSourceStack> create(ExamplePlugin plugin) {
        return Commands.literal("scene")
            .then(SetCommand.create(plugin))
            .then(LoadCommand.create(plugin))
            .then(SaveCommand.create(plugin))
            .then(TpCommand.create(plugin))
            .build();
    }
}
