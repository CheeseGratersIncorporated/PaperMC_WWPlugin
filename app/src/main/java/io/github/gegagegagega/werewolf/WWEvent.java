package io.github.gegagegagega.werewolf;

import java.util.Objects;

public interface WWEvent extends Comparable<WWEvent> {

    int priority();

    void run();

    static WWEvent of(int priority, Runnable callback) {
        Objects.requireNonNull(callback, "callback cannot be null");

        return new WWEvent() {
            @Override
            public int priority() {
                return priority;
            }

            @Override
            public void run() {
                callback.run();
            }
        };
    }

    @Override
    default int compareTo(WWEvent other) {
        // Higher-priority events come first.
        return Integer.compare(other.priority(), this.priority());
    }
}