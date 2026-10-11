package io.github.gegagegagega.werewolf.core.night;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.github.gegagegagega.werewolf.core.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Resolves one night. Every role queues its actions with
 * {@link Role#onNight(Night)}, then the queue runs in {@link ActionPriority}
 * order so interdependent abilities (protect, attack, investigate) resolve the
 * same way every time regardless of who typed their command first.
 */
public final class Night {

    private final Game game;
    private final PriorityQueue<NightAction> queue = new PriorityQueue<>();
    private final Map<WWPlayer, Integer> wolfVotes = new HashMap<>();
    private long sequence = 0;

    private Night(Game game) {
        this.game = game;
    }

    /** Runs a full night for the game. Returns the werewolves' victim, or null. */
    public static WWPlayer resolve(Game game) {
        Night night = new Night(game);
        game.setWolfVictim(null);
        for (WWPlayer player : game.getPlayers()) {
            if (player.getRole() != null) {
                player.getRole().onNight(night);
            }
        }
        night.schedule(ActionPriority.WEREWOLVES_PICK_VICTIM, night::pickWolfVictim);
        night.schedule(ActionPriority.RESET_NIGHT_STATE, game::resetNightState);
        night.run();
        return game.getWolfVictim();
    }

    public Game game() {
        return game;
    }

    public void schedule(int priority, Runnable effect) {
        queue.add(new NightAction(priority, sequence++, effect));
    }

    /** Sends a private message to a player (e.g. an investigation result). */
    public void tell(WWPlayer player, Component message) {
        game.listener().tell(player, message);
    }

    public void addWolfVote(WWPlayer target) {
        wolfVotes.merge(target, 1, Integer::sum);
    }

    public int wolfVotesFor(WWPlayer target) {
        return wolfVotes.getOrDefault(target, 0);
    }

    private void run() {
        while (!queue.isEmpty()) {
            queue.poll().effect().run();
        }
    }

    /**
     * The living player with the most werewolf votes is attacked. Ties are
     * broken randomly. A doctored victim is only knocked out.
     */
    private void pickWolfVictim() {
        int maxVotes = wolfVotes.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        if (maxVotes <= 0) {
            return;
        }
        List<WWPlayer> candidates = new ArrayList<>(game.getPlayers());
        Collections.shuffle(candidates, game.random());
        for (WWPlayer candidate : candidates) {
            if (!candidate.isAlive() || wolfVotesFor(candidate) != maxVotes) {
                continue;
            }
            candidate.setAttacked(true);
            Role role = candidate.getRole();
            if (role != null && role.defense() >= 1) {
                continue;
            }
            if (candidate.isDoctored()) {
                game.knockOut(candidate);
            } else {
                candidate.addCauseOfDeath(Component.text("was mauled by werewolves", NamedTextColor.RED));
                game.kill(candidate);
            }
            game.setWolfVictim(candidate);
            return;
        }
    }
}
