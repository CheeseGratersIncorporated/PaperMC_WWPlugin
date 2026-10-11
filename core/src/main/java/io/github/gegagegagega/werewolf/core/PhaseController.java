package io.github.gegagegagega.werewolf.core;

import java.util.Optional;

import io.github.gegagegagega.werewolf.core.night.Night;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * The game's timeline. {@link #enter(GamePhase)} runs a phase's entry rules,
 * tells the listener, and starts its timer; when the timer completes,
 * {@link #complete(GamePhase)} decides what comes next.
 *
 * <p>To add a phase: add it to {@link GamePhase}, give it a case in
 * {@code enter} if it changes state on entry, and route to it from
 * {@code complete}.
 */
final class PhaseController {

    private final Game game;
    private final GameClock clock;
    private boolean stopped = false;
    /** Ticks left on the voting timer when a majority interrupted it, or -1. */
    private int votingTicksLeft = -1;

    PhaseController(Game game, GameClock clock) {
        this.game = game;
        this.clock = clock;
    }

    void countdown(int seconds) {
        clock.start(null, seconds, game::start);
    }

    void stop() {
        stopped = true;
        clock.stop();
    }

    boolean isVotingResumed() {
        return votingTicksLeft >= 0;
    }

    void enter(GamePhase phase) {
        if (stopped || !game.isActive()) {
            return;
        }
        game.setPhase(phase);
        switch (phase) {
            case VOTING -> {
                game.setAccused(null);
                for (WWPlayer player : game.getPlayers()) {
                    player.setVotedFor(null);
                }
            }
            case JUDGMENT -> {
                for (WWPlayer player : game.getPlayers()) {
                    player.setJudgment(Judgment.ABSTAIN);
                }
            }
            case DUSK -> {
                for (WWPlayer player : game.getPlayers()) {
                    if (player.getRole() != null) {
                        player.getRole().onDusk(game);
                    }
                }
                if (endIfWon()) {
                    return;
                }
                for (WWPlayer player : game.getPlayers()) {
                    player.setTarget(null);
                }
            }
            case POST_NIGHT -> Night.resolve(game);
            case DAWN -> {
                if (endIfWon()) {
                    return;
                }
                game.nextDay();
            }
            default -> {
            }
        }
        game.listener().onPhaseStarted(phase);
        game.listener().onStateChanged();
        clock.start(phase, durationOf(phase), () -> complete(phase));
    }

    private int durationOf(GamePhase phase) {
        if (phase == GamePhase.VOTING && votingTicksLeft >= 0) {
            return (int) Math.ceil((double) votingTicksLeft / GameClock.TICKS_PER_SECOND);
        }
        return game.durationOf(phase);
    }

    void complete(GamePhase phase) {
        if (stopped || !game.isActive()) {
            return;
        }
        switch (phase) {
            case FIRST_DAY -> enter(GamePhase.DUSK);
            case DISCUSSION -> {
                votingTicksLeft = -1;
                enter(GamePhase.VOTING);
            }
            case VOTING -> {
                votingTicksLeft = -1;
                enter(GamePhase.DUSK);
            }
            case DEFENSE -> enter(GamePhase.JUDGMENT);
            case JUDGMENT -> resolveJudgment();
            case SENTENCE -> enter(GamePhase.DUSK);
            case DUSK -> enter(GamePhase.NIGHT);
            case NIGHT -> enter(GamePhase.POST_NIGHT);
            case POST_NIGHT -> enter(GamePhase.DAWN);
            case DAWN -> enter(GamePhase.DISCUSSION);
        }
    }

    /** A vote reached a majority: cut voting short and put the target on the stand. */
    void interruptVoting(WWPlayer accused) {
        votingTicksLeft = clock.stop();
        game.setAccused(accused);
        enter(GamePhase.DEFENSE);
    }

    private void resolveJudgment() {
        WWPlayer accused = game.getAccused();
        int verdict = 0;
        for (WWPlayer player : game.getPlayers()) {
            if (!player.isAlive() || player == accused) continue;
            verdict += player.getJudgment().weight();
            game.listener().broadcast(judgmentLine(player));
        }
        if (verdict <= 0 || accused == null) {
            enter(GamePhase.VOTING);
            return;
        }
        accused.setLynched(true);
        game.kill(accused);
        if (accused.getRole() != null && accused.getRole().winsWhenLynched()) {
            game.addResult(accused.getRole().title()
                    .append(Component.text(" " + accused.getName(), NamedTextColor.WHITE)
                            .append(Component.text(" has won!", NamedTextColor.YELLOW))));
        }
        enter(GamePhase.SENTENCE);
        game.listener().onLynched(accused);
    }

    private static Component judgmentLine(WWPlayer player) {
        Component name = Component.text(player.getName(), NamedTextColor.WHITE);
        return switch (player.getJudgment()) {
            case GUILTY -> Component.empty().append(name)
                    .append(Component.text(" voted ", NamedTextColor.YELLOW))
                    .append(Component.text("GUILTY", NamedTextColor.RED));
            case INNOCENT -> Component.empty().append(name)
                    .append(Component.text(" voted ", NamedTextColor.YELLOW))
                    .append(Component.text("INNOCENT", NamedTextColor.GREEN));
            case ABSTAIN -> Component.empty().append(name)
                    .append(Component.text(" ABSTAINED", NamedTextColor.BLUE));
        };
    }

    private boolean endIfWon() {
        Optional<Component> winner = game.checkWinner();
        if (winner.isEmpty()) {
            return false;
        }
        game.addResult(winner.get());
        game.end();
        return true;
    }
}
