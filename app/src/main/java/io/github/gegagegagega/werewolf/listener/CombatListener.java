package io.github.gegagegagega.werewolf.listener;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.GamePhase;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Damage and death. The only fight allowed is werewolves attacking a
 * non-werewolf during {@link GamePhase#POST_NIGHT}, and a participant's
 * character only dies once the game has already marked them dead (or the
 * doctor's patient gets knocked out).
 */
public final class CombatListener implements Listener {

    private final WerewolfPlugin plugin;

    public CombatListener(WerewolfPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        Game game = plugin.game();
        if (!game.isActive()) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = attackingPlayer(event.getDamager());
        if (attacker == null) return;

        WWPlayer attackerWW = game.getPlayer(attacker.getName());
        WWPlayer victimWW = game.getPlayer(victim.getName());
        if (attackerWW == null || victimWW == null) return;

        boolean wolvesHunting = game.getPhase() == GamePhase.POST_NIGHT
                && attackerWW.isAlive() && attackerWW.isWerewolf() && !victimWW.isWerewolf();
        if (!wolvesHunting) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setShowDeathMessages(false);
        Game game = plugin.game();
        if (!game.isActive()) return;
        WWPlayer participant = game.getPlayer(event.getPlayer().getName());
        if (participant == null) return;

        if (!participant.isAlive() && !participant.isGhost()) {
            participant.setGhost(true);
        } else if (game.getKnockedOut().contains(participant)) {
            event.setDeathSound(Sound.ENTITY_WANDERING_TRADER_DRINK_POTION);
            event.getPlayer().sendMessage(Component.text("You were saved!", NamedTextColor.GREEN));
            game.wakeUp(participant);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPostRespawn(PlayerPostRespawnEvent event) {
        // Respawning resets some flags, so re-apply everything.
        plugin.sync().forget(event.getPlayer());
        plugin.sync().update(event.getPlayer());
    }

    private static Player attackingPlayer(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }
        return null;
    }
}
