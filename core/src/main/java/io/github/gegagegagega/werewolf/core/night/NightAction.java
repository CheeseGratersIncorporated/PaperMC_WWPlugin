package io.github.gegagegagega.werewolf.core.night;

import java.util.Objects;

/** A single queued night effect. See {@link ActionPriority} for ordering. */
record NightAction(int priority, long sequence, Runnable effect) implements Comparable<NightAction> {

    NightAction {
        Objects.requireNonNull(effect, "effect");
    }

    @Override
    public int compareTo(NightAction other) {
        int byPriority = Integer.compare(other.priority, priority);
        return byPriority != 0 ? byPriority : Long.compare(sequence, other.sequence);
    }
}
