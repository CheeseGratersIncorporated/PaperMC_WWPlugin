package jenny.werewolf.papermc.plugin;


import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;
import net.kyori.adventure.bossbar.BossBar;

import org.bukkit.entity.Player;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.Component;
import java.util.function.Consumer;


import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


import net.kyori.adventure.text.Component;

public class Timer {

    private final JavaPlugin plugin;
    private BukkitRunnable task;
    private BossBar bossBar;

    public Timer(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void start(int seconds, BossBar bb, Game game, Runnable onComplete, Consumer<Integer> onTick) {

        
        // bossBar= BossBar.bossBar(
        //     Component.text("Starting in " + seconds + " seconds", NamedTextColor.YELLOW),
        //     1.0f,
        //     BossBar.Color.RED,
        //     BossBar.Overlay.PROGRESS
        // );
        bossBar = bb;

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showBossBar(bossBar);
        }


        task = new BukkitRunnable() {
            int timeLeft = seconds*20;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    cancel();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.hideBossBar(bossBar);
                    }
                    onComplete.run();
                    return;
                }

                float progress = (float) timeLeft / (seconds * 20);

                // bossBar.name(
                //     Component.text("Starting in " + timeLeft + " seconds", NamedTextColor.YELLOW)
                // );

                bossBar.progress(progress);

                onTick.accept(timeLeft);
                timeLeft--;
            }
        };
        task.runTaskTimer(plugin, 0L, 1L);
    }
    public void stop(BossBar bossBar) {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.hideBossBar(bossBar);
        }
    }
}