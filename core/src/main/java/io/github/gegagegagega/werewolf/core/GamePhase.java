package io.github.gegagegagega.werewolf.core;

/**
 * The phases a game moves through. Durations are the defaults in seconds;
 * {@link PhaseController} decides which phase follows which.
 */
public enum GamePhase {
    FIRST_DAY(60),
    DISCUSSION(120),
    VOTING(60),
    DEFENSE(20),
    JUDGMENT(20),
    SENTENCE(10),
    DUSK(15),       // doors open, everyone heads home
    NIGHT(60),      // night abilities are chosen
    POST_NIGHT(15), // night resolves, werewolves hunt their victim
    DAWN(15);       // doors open again

    private final int defaultSeconds;

    GamePhase(int defaultSeconds) {
        this.defaultSeconds = defaultSeconds;
    }

    public int defaultSeconds() {
        return defaultSeconds;
    }

    public boolean isNight() {
        return this == NIGHT || this == POST_NIGHT;
    }

    /** Phases where a trial is on stage and the accused is the only one allowed to talk. */
    public boolean isTrial() {
        return this == DEFENSE || this == JUDGMENT;
    }
}
