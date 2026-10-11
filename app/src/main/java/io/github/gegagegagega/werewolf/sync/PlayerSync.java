package io.github.gegagegagega.werewolf.sync;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.github.gegagegagega.werewolf.core.view.PlayerView;
import io.github.gegagegagega.werewolf.core.view.Views;
import io.github.gegagegagega.werewolf.housing.House;
import io.github.gegagegagega.werewolf.scene.SceneKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Makes Minecraft match the game. Everything that should always follow from
 * game state (who sees whom, ghost flags, name tags, judgment buttons, skins,
 * the tab list, the vote sidebar and house doors) is applied here and only
 * here. Game code changes state and then calls {@link #syncAll()}.
 *
 * <p>What a player should look like is decided by {@link Views} in the core;
 * this class only applies it. It remembers what it last applied to each
 * player so repeated syncs don't resend anything that hasn't changed.
 * One-shot effects (teleports, titles, sounds) live in
 * {@link io.github.gegagegagega.werewolf.game.GameEffects} instead.
 */
public final class PlayerSync {

    private static final int TAB_PING = 42;

    private final WerewolfPlugin plugin;
    private final Map<UUID, PlayerView> applied = new HashMap<>();
    private final Map<String, Component> tabNames = new HashMap<>();

    public PlayerSync(WerewolfPlugin plugin) {
        this.plugin = plugin;
    }

    /** Re-syncs every online player and the shared world state. */
    public void syncAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyBody(player);
        }
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                applyVisibility(viewer, target);
            }
        }
        syncTabList();
        syncVotingBoard();
        syncDoors();
    }

    /** Re-syncs one player: their own state, who they see, and who sees them. */
    public void update(Player player) {
        applyBody(player);
        for (Player other : Bukkit.getOnlinePlayers()) {
            applyVisibility(player, other);
            applyVisibility(other, player);
        }
    }

    /**
     * Forgets what was applied to this player, so the next sync applies
     * everything again. Call after they join or respawn, since Minecraft
     * resets some of these flags.
     */
    public void forget(Player player) {
        applied.remove(player.getUniqueId());
    }

    // ------------------------------------------------------------------
    // Per player
    // ------------------------------------------------------------------

    private void applyBody(Player player) {
        Game game = plugin.game();
        PlayerView view = Views.of(game, game.getPlayer(player.getName()));
        PlayerView before = applied.put(player.getUniqueId(), view);

        // Flight is only touched on a change, so builders in creative aren't grounded by every sync.
        boolean ghostChanged = before == null
                ? view.ghost() || game.isActive()
                : before.ghost() != view.ghost();
        if (ghostChanged) {
            applyGhost(player, view.ghost());
        }
        if (player.isInvisible() != view.invisible()) {
            player.setInvisible(view.invisible());
        }
        if (player.isCollidable() != view.collidable()) {
            player.setCollidable(view.collidable());
        }
        if (plugin.nameTags().isNameTagHidden(player) == view.nameTagVisible()) {
            if (view.nameTagVisible()) {
                plugin.nameTags().showNameTag(player);
            } else {
                plugin.nameTags().hideNameTag(player);
            }
        }
        boolean participant = game.isActive() && game.hasPlayer(player.getName());
        if (participant || view.judgmentButtons() || (before != null && before.judgmentButtons())) {
            plugin.inventories().applyJudgmentButtons(player, view.judgmentButtons(), view.judgment());
        }
        plugin.skins().apply(player, view.wolfDisguise() ? (plugin.isBlueyMode() ? "bluey" : "werewolf") : null);
    }

    private static void applyGhost(Player player, boolean ghost) {
        player.setAllowFlight(ghost);
        if (ghost) {
            player.setFlying(true);
        }
        player.setInvulnerable(ghost);
        player.setCanPickupItems(!ghost);
        player.setSilent(ghost);
    }

    private void applyVisibility(Player viewer, Player target) {
        if (viewer.equals(target)) {
            return;
        }
        Game game = plugin.game();
        boolean visible = Views.canSee(game,
                game.getPlayer(viewer.getName()),
                game.getPlayer(target.getName()),
                plugin.scene().hasLocation(SceneKeys.WOLF_DEN));
        if (visible && !viewer.canSee(target)) {
            viewer.showPlayer(plugin, target);
        } else if (!visible && viewer.canSee(target)) {
            viewer.hidePlayer(plugin, target);
        }
    }

    // ------------------------------------------------------------------
    // Shared state
    // ------------------------------------------------------------------

    /** One fake tab entry per participant, greyed out once their death is known. */
    private void syncTabList() {
        Game game = plugin.game();
        Set<String> participants = game.getPlayerNames();
        for (String key : Set.copyOf(tabNames.keySet())) {
            if (!participants.contains(key)) {
                plugin.fakeTab().removeFakePlayerForEveryone(key);
                tabNames.remove(key);
            }
        }
        for (WWPlayer player : game.getPlayers()) {
            Component name = Views.of(game, player).shownAsDead()
                    ? Component.text(player.getName(), NamedTextColor.GRAY)
                    : Component.text(player.getName());
            if (!Objects.equals(tabNames.get(player.getName()), name)) {
                tabNames.put(player.getName(), name);
                plugin.fakeTab().addFakePlayerForEveryone(player.getName(), player.getName(), name, TAB_PING);
            }
        }
    }

    private void syncVotingBoard() {
        Game game = plugin.game();
        Map<String, Integer> votes = new HashMap<>();
        for (WWPlayer player : game.getPlayers()) {
            votes.put(player.getName(), game.votesFor(player));
        }
        plugin.votingDisplay().sync(Views.votingBoardVisible(game), votes);
    }

    private void syncDoors() {
        Game game = plugin.game();
        for (House house : plugin.housing().getHouses()) {
            Boolean open = Views.doorsOpen(game, game.getPlayer(house.owner));
            if (open != null) {
                house.setDoorsOpen(open);
            }
        }
    }
}
