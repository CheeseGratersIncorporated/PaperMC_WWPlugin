package jenny.werewolf.papermc.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final ExamplePlugin plugin;

    public JoinListener(ExamplePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // player.sendMessage(Component.text("Welcome to Werewolf!"));

        // plugin.getServer().broadcast(
        //     Component.text(player.getName() + " joined the game.")
        // );

        plugin.housingManager.refreshVisibility();
    }
}