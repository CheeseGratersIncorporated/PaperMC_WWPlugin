package io.github.gegagegagega.werewolf.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import io.github.gegagegagega.werewolf.core.role.Role;
import io.github.gegagegagega.werewolf.core.role.Roles;
import io.github.gegagegagega.werewolf.core.role.Spectator;
import io.github.gegagegagega.werewolf.core.role.Townie;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * The werewolf game itself: players, roles, phase, votes, the trial and the
 * night. This class never touches Minecraft. Everything visible happens
 * through the {@link GameListener}, and time passes through the
 * {@link GameClock}.
 *
 * <p>Typical flow for an action: a command calls a method here, the method
 * changes state, and the listener's {@link GameListener#onStateChanged()} lets
 * the Minecraft layer reconcile every player with the new state.
 */
public final class Game {

    public enum VoteResult {
        /** The vote was recorded (or cleared). */
        CHANGED,
        /** The voter was already voting for that player. */
        UNCHANGED,
        /** The vote gave the target a majority and the trial has started. */
        MAJORITY,
        /** Voting is not open, or the voter or target is not eligible. */
        NOT_ALLOWED
    }

    private final Map<String, WWPlayer> players = new LinkedHashMap<>();
    private final Map<String, Integer> roleCounts = new LinkedHashMap<>();
    private final Map<GamePhase, Integer> durations = new EnumMap<>(GamePhase.class);
    private final Random random;
    private final GameListener listener;
    private final PhaseController phases;

    private boolean active = false;
    private boolean starting = false;
    private GamePhase phase = null;
    private int day = 1;
    private WWPlayer accused = null;
    private WWPlayer wolfVictim = null;
    private final Set<WWPlayer> knockedOut = new LinkedHashSet<>();
    private final List<WWPlayer> pendingDeathAnnouncements = new ArrayList<>();
    private final List<Component> results = new ArrayList<>();

    public Game(GameClock clock, GameListener listener, Random random) {
        this.listener = Objects.requireNonNull(listener, "listener");
        this.random = Objects.requireNonNull(random, "random");
        this.phases = new PhaseController(this, Objects.requireNonNull(clock, "clock"));
    }

    public Game(GameClock clock, GameListener listener) {
        this(clock, listener, new Random());
    }

    // ------------------------------------------------------------------
    // Lobby
    // ------------------------------------------------------------------

    /**
     * Adds a player. Anyone added after the game started becomes a dead
     * spectator. Returns the existing player if the name is already in.
     */
    public WWPlayer addPlayer(String name) {
        WWPlayer existing = players.get(name);
        if (existing != null) {
            return existing;
        }
        WWPlayer player = new WWPlayer(name);
        players.put(name, player);
        if (active) {
            makeSpectator(player);
        }
        listener.onStateChanged();
        return player;
    }

    /** Removes a player. During a game they are killed first. */
    public boolean removePlayer(String name) {
        WWPlayer player = players.get(name);
        if (player == null) {
            return false;
        }
        if (active) {
            kill(player);
        }
        players.remove(name);
        listener.onStateChanged();
        return true;
    }

    public void setRoleCount(String roleId, int count) {
        if (count <= 0) {
            roleCounts.remove(roleId);
        } else {
            roleCounts.put(roleId, count);
        }
    }

    public Map<String, Integer> getRoleCounts() {
        return Collections.unmodifiableMap(roleCounts);
    }

    public void setDuration(GamePhase phase, int seconds) {
        durations.put(phase, seconds);
    }

    public int durationOf(GamePhase phase) {
        return durations.getOrDefault(phase, phase.defaultSeconds());
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    /** Starts the pre-game countdown; the game begins when it finishes. */
    public boolean startCountdown(int seconds) {
        if (active || starting) {
            return false;
        }
        starting = true;
        phases.countdown(seconds);
        return true;
    }

    /** Assigns roles and begins the first day immediately. */
    public void start() {
        starting = false;
        if (active) {
            return;
        }
        active = true;
        day = 1;
        assignRoles();
        listener.onGameStarted();
        phases.enter(GamePhase.FIRST_DAY);
    }

    /** Ends the game with whatever results have been recorded. */
    public void end() {
        if (!active) {
            return;
        }
        active = false;
        phases.stop();
        listener.onGameEnded(List.copyOf(results));
    }

    /** Stops the game without announcing anything. */
    public void abort() {
        active = false;
        starting = false;
        phases.stop();
    }

    private void assignRoles() {
        List<Role> roles = new ArrayList<>();
        roleCounts.forEach((id, count) -> {
            for (int i = 0; i < count; i++) {
                roles.add(Roles.create(id));
            }
        });
        Collections.shuffle(roles, random);
        List<WWPlayer> order = new ArrayList<>(players.values());
        Collections.shuffle(order, random);
        for (int i = 0; i < order.size(); i++) {
            WWPlayer player = order.get(i);
            player.setAlive(true);
            player.setRole(i < roles.size() ? roles.get(i) : new Townie());
        }
    }

    private void makeSpectator(WWPlayer player) {
        player.setRole(new Spectator());
        player.setAlive(false);
        player.setDeathRevealed(true);
    }

    // ------------------------------------------------------------------
    // Deaths
    // ------------------------------------------------------------------

    /**
     * Marks a player dead. Deaths during the day are public immediately;
     * deaths at night are announced at the next discussion.
     */
    public void kill(WWPlayer player) {
        if (!player.isAlive()) {
            return;
        }
        player.setAlive(false);
        if (phase != null && phase.isNight()) {
            pendingDeathAnnouncements.add(player);
        } else {
            player.setDeathRevealed(true);
        }
        listener.onPlayerKilled(player);
        listener.onStateChanged();
    }

    /** The doctor saved this player: they get knocked out instead of dying. */
    public void knockOut(WWPlayer player) {
        knockedOut.add(player);
    }

    public Set<WWPlayer> getKnockedOut() {
        return Collections.unmodifiableSet(knockedOut);
    }

    /** Called once the knocked-out player has actually been knocked down and got back up. */
    public void wakeUp(WWPlayer player) {
        knockedOut.remove(player);
    }

    /** Night deaths waiting to be announced. Clears the list. */
    public List<WWPlayer> takePendingDeathAnnouncements() {
        List<WWPlayer> deaths = List.copyOf(pendingDeathAnnouncements);
        pendingDeathAnnouncements.clear();
        return deaths;
    }

    public void revealDeath(WWPlayer player) {
        player.setDeathRevealed(true);
        listener.onStateChanged();
    }

    /** The announcement line for a death, e.g. "Steve(Bakery) was mauled by werewolves". */
    public static Component deathMessage(WWPlayer player, String houseName) {
        Component message = Component.text(
                player.getName() + (houseName == null ? "" : "(" + houseName + ")"),
                NamedTextColor.RED);
        List<Component> causes = player.getCausesOfDeath();
        for (int i = 0; i < causes.size(); i++) {
            if (i == 0) {
                message = message.append(Component.text(" ", NamedTextColor.RED));
            } else if (i == causes.size() - 1) {
                message = message.append(Component.text(i == 1 ? " and " : ", and ", NamedTextColor.RED));
            } else {
                message = message.append(Component.text(", ", NamedTextColor.RED));
            }
            message = message.append(causes.get(i));
        }
        return message;
    }

    // ------------------------------------------------------------------
    // Day: voting and the trial
    // ------------------------------------------------------------------

    public boolean canVote() {
        return active && phase == GamePhase.VOTING;
    }

    public boolean canJudge() {
        return active && phase == GamePhase.JUDGMENT;
    }

    /** Players can't move while the town watches a trial or execution. */
    public boolean playersFrozen() {
        return active && (phase == GamePhase.DEFENSE || phase == GamePhase.JUDGMENT || phase == GamePhase.SENTENCE);
    }

    /**
     * Votes to put {@code target} on trial. A null target withdraws the vote.
     * When a target gets more than half the living players' votes, the
     * defense starts immediately.
     */
    public VoteResult vote(WWPlayer voter, WWPlayer target) {
        if (!canVote() || !voter.isAlive() || (target != null && !target.isAlive())) {
            return VoteResult.NOT_ALLOWED;
        }
        if (voter.getVotedFor() == target) {
            return VoteResult.UNCHANGED;
        }
        voter.setVotedFor(target);
        if (target != null && votesFor(target) > aliveCount() / 2) {
            phases.interruptVoting(target);
            return VoteResult.MAJORITY;
        }
        listener.onStateChanged();
        return VoteResult.CHANGED;
    }

    public int votesFor(WWPlayer target) {
        int votes = 0;
        for (WWPlayer player : players.values()) {
            if (player.isAlive() && player.getVotedFor() == target) {
                votes++;
            }
        }
        return votes;
    }

    /** Whether this player may cast a guilty/innocent verdict right now. */
    public boolean canJudge(WWPlayer player) {
        return canJudge() && player.isAlive() && player != accused;
    }

    public boolean judge(WWPlayer player, Judgment judgment) {
        if (!canJudge(player)) {
            return false;
        }
        player.setJudgment(judgment);
        listener.onStateChanged();
        return true;
    }

    /** Picks {@code judgment}, or goes back to abstaining if it was already picked. */
    public boolean toggleJudgment(WWPlayer player, Judgment judgment) {
        return judge(player, player.getJudgment() == judgment ? Judgment.ABSTAIN : judgment);
    }

    // ------------------------------------------------------------------
    // Night
    // ------------------------------------------------------------------

    /** Sets the player's ability target if their role allows it, and returns the role's response. */
    public Component setTarget(WWPlayer player, WWPlayer target) {
        Role role = player.getRole();
        if (role == null) {
            return Component.text("You do not have a role assigned!", NamedTextColor.RED);
        }
        if (role.canTarget(this, target)) {
            player.setTarget(target);
        }
        return role.targetResponse(this, target);
    }

    public WWPlayer getWolfVictim() {
        return wolfVictim;
    }

    public void setWolfVictim(WWPlayer victim) {
        this.wolfVictim = victim;
    }

    public void resetNightState() {
        for (WWPlayer player : players.values()) {
            player.resetNightFlags();
        }
    }

    // ------------------------------------------------------------------
    // Rules
    // ------------------------------------------------------------------

    public int aliveCount() {
        int count = 0;
        for (WWPlayer player : players.values()) {
            if (player.isAlive() && !player.isSpectator()) {
                count++;
            }
        }
        return count;
    }

    /** The winning side's announcement, if one side has won. */
    public Optional<Component> checkWinner() {
        int wolves = 0;
        int others = 0;
        for (WWPlayer player : players.values()) {
            if (!player.isAlive() || player.isSpectator()) continue;
            if (player.isWerewolf()) wolves++;
            else others++;
        }
        if (wolves <= 0) {
            return Optional.of(Component.text("THE TOWN", NamedTextColor.GREEN)
                    .append(Component.text(" has won!", NamedTextColor.YELLOW)));
        }
        if (others <= wolves) {
            return Optional.of(Component.text("WEREWOLVES", NamedTextColor.RED)
                    .append(Component.text(" have won!", NamedTextColor.YELLOW)));
        }
        return Optional.empty();
    }

    /** Who this player's chat reaches right now. */
    public ChatChannel chatChannel(WWPlayer player) {
        if (!active) {
            return ChatChannel.PUBLIC;
        }
        if (!player.isAlive()) {
            return ChatChannel.DEAD;
        }
        if (phase == null) {
            return ChatChannel.PUBLIC;
        }
        return switch (phase) {
            case DEFENSE, SENTENCE -> player == accused ? ChatChannel.PUBLIC : ChatChannel.MUTED;
            case NIGHT, POST_NIGHT -> player.isWerewolf() ? ChatChannel.WEREWOLVES : ChatChannel.MUTED;
            default -> ChatChannel.PUBLIC;
        };
    }

    void addResult(Component line) {
        results.add(line);
    }

    // ------------------------------------------------------------------
    // State accessors
    // ------------------------------------------------------------------

    public boolean isActive() {
        return active;
    }

    public boolean isStarting() {
        return starting;
    }

    public GamePhase getPhase() {
        return phase;
    }

    void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public int getDay() {
        return day;
    }

    void nextDay() {
        day++;
    }

    /** The player on trial (voted up to the stand), or null. */
    public WWPlayer getAccused() {
        return accused;
    }

    void setAccused(WWPlayer accused) {
        this.accused = accused;
    }

    /** True while voting continues after an innocent verdict (no "VOTING" title, shorter timer). */
    public boolean isVotingResumed() {
        return phases.isVotingResumed();
    }

    public Collection<WWPlayer> getPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    public WWPlayer getPlayer(String name) {
        return name == null ? null : players.get(name);
    }

    public boolean hasPlayer(String name) {
        return players.containsKey(name);
    }

    public Set<String> getPlayerNames() {
        return Collections.unmodifiableSet(players.keySet());
    }

    public Random random() {
        return random;
    }

    public GameListener listener() {
        return listener;
    }
}
