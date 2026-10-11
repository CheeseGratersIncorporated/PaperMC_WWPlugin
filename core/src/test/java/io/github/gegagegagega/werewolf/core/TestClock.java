package io.github.gegagegagega.werewolf.core;

/** A clock that only advances when a test says so. */
final class TestClock implements GameClock {
    GamePhase phase;
    int seconds;
    private Runnable onComplete;
    int ticksLeftOnStop = 0;

    @Override
    public void start(GamePhase phase, int seconds, Runnable onComplete) {
        this.phase = phase;
        this.seconds = seconds;
        this.onComplete = onComplete;
    }

    @Override
    public int stop() {
        onComplete = null;
        return ticksLeftOnStop;
    }

    /** Finishes the running countdown. */
    void finish() {
        Runnable callback = onComplete;
        onComplete = null;
        if (callback != null) {
            callback.run();
        }
    }
}
