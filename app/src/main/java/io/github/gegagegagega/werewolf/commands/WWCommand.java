package io.github.gegagegagega.werewolf.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.github.gegagegagega.werewolf.commands.housing.HousingCommand;
import io.github.gegagegagega.werewolf.commands.scene.SceneCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class WWCommand {
    public static LiteralCommandNode<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("ww")
        
            .then(PermsCommand.create(plugin))
            .then(RolesCommand.create(plugin))
            .then(StartCommand.create(plugin))
            .then(JoinCommand.create(plugin))
            .then(LeaveCommand.create(plugin))
            .then(RoleCommand.create(plugin))
            .then(HousingCommand.create(plugin))
            .then(VoteCommand.create(plugin))
            .then(TargetCommand.create(plugin))
            .then(SceneCommand.create(plugin))
            .then(SpectateCommand.create(plugin))
            .then(ClaimCommand.create(plugin))
            .then(BlueyCommand.create(plugin))
            .then(ResetCommand.create(plugin))
            .build();
    }
}
