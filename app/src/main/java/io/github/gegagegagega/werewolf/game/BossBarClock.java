package io.github.gegagegagega.werewolf.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.gegagegagega.werewolf.core.GameClock;
import io.github.gegagegagega.werewolf.core.GamePhase;
import net.kyori.adventure.bossbar.BossBar;

/**
 * Times phases on the server tick loop and shows the countdown as a boss bar.
 * Each tick is also passed to {@link GameEffects#onTick} for animations.
 */
public final class BossBarClock implements GameClock {

    private final JavaPlugin plugin;
    private final GameEffects effects;
    private BukkitRunnable task;
    private BossBar bossBar;
    private int ticksLeft;

    public BossBarClock(JavaPlugin plugin, GameEffects effects) {
        this.plugin = plugin;
        this.effects = effects;
    }

    @Override
    public void start(GamePhase phase, int seconds, Runnable onComplete) {
        stop();
        int totalTicks = seconds * TICKS_PER_SECOND;
        BossBar bar = effects.bossBarFor(phase);
        bossBar = bar;
        ticksLeft = totalTicks;
        showToAll(bar);

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (ticksLeft <= 0) {
                    cancel();
                    if (task == this) {
                        task = null;
                    }
                    hideFromAll(bar);
                    onComplete.run();
                    return;
                }
                bar.progress(Math.max(0f, Math.min(1f, (float) ticksLeft / Math.max(1, totalTicks))));
                // Re-show every tick so players who join mid-phase see it too.
                showToAll(bar);
                effects.onTick(phase, ticksLeft);
                ticksLeft--;
            }
        };
        task.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public int stop() {
        int left = task == null ? 0 : ticksLeft;
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (bossBar != null) {
            hideFromAll(bossBar);
            bossBar = null;
        }
        return left;
    }

    private static void showToAll(BossBar bar) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showBossBar(bar);
        }
    }

    private static void hideFromAll(BossBar bar) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.hideBossBar(bar);
        }
    }
}
