package jenny.werewolf.papermc.plugin;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class TestCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        // Send a message to whoever ran the command (Player or Console)
        source.getSender().sendMessage(Component.text("Hello from the server!", NamedTextColor.GREEN));

        // Optional: Check if the sender is specifically a player
        if (source.getSender() instanceof Player player) {
            player.sendMessage(Component.text("Nice to meet you, " + player.getName() + "!", NamedTextColor.AQUA));
        }
    }

    @Override
    public boolean canUse(org.bukkit.command.CommandSender sender) {
        // Enforce a permission requirement (returns true if they have it)
        return sender.hasPermission("myplugin.hello");
    }
}