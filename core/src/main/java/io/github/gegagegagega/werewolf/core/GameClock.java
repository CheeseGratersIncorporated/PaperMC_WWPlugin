package io.github.gegagegagega.werewolf.core;

/**
 * Counts down a phase. The Minecraft implementation shows a boss bar and runs
 * per-tick animations; tests can complete phases by hand.
 */
public interface GameClock {

    int TICKS_PER_SECOND = 20;

    /**
     * Starts a countdown, replacing any running one.
     *
     * @param phase the phase being timed, or null for the pre-game countdown
     */
    void start(GamePhase phase, int seconds, Runnable onComplete);

    /** Stops the countdown without completing it. Returns the ticks that were left, or 0. */
    int stop();
}
