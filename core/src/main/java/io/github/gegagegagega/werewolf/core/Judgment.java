package io.github.gegagegagega.werewolf.core;

/** A player's verdict during {@link GamePhase#JUDGMENT}. */
public enum Judgment {
    INNOCENT(-1),
    ABSTAIN(0),
    GUILTY(1);

    private final int weight;

    Judgment(int weight) {
        this.weight = weight;
    }

    /** Guilty counts +1, innocent -1. The accused is executed when the sum is positive. */
    public int weight() {
        return weight;
    }
}
