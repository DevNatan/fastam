package me.devnatan.fastam;

import java.time.Duration;
import java.time.Instant;

public abstract class State {

    private final Object lock = new Object();

    private boolean started, ended, updating, frozen, paused;
    private Instant startedAt, endedAt, pausedAt, initiallyStartedAt;
    private Duration pauseDuration;

    protected void onStart() {}

    protected void onEnd() {}

    protected void onUpdate() {}

    protected void onPause(boolean paused) {}

    public final void start() {
        synchronized (lock) {
            if (isStarted())
                return;
            started = true;
        }

        initiallyStartedAt = Instant.now();
        startedAt = Instant.now();
        onStart();
    }

    public final void end() {
        synchronized (lock) {
            if (!isStarted() || isEnded())
                return;
            ended = true;
            endedAt = Instant.now();
        }

        setPaused(false);
        onEnd();
    }

    public final void update() {
        synchronized (lock) {
            if (isPaused() || !isStarted() || isEnded() || isUpdating()) return;
            updating = true;
        }

        if (isReadyToEnd() && !isFrozen()) {
            updating = false;
            end();
            return;
        }

        onUpdate();
        updating = false;
    }

    public void reset() {
        ended = false;
        paused = false;
        updating = false;
        frozen = false;
        started = false;
        endedAt = null;
        startedAt = null;
        pausedAt = null;
    }

    public boolean isReadyToEnd() {
        if (isEnded()) return true;

        final Duration remaining = getRemainingDuration();

        // infinite state duration
        if (remaining == null)
            return false;

        return remaining == Duration.ZERO;
    }

    public Duration getDuration() {
        return Duration.ZERO;
    }

    public Duration getRemainingDuration() {
        // fast path -- check if it's ended
        if (isEnded())
            return Duration.ZERO;

        final Duration duration = getDuration();

        // fast path -- check if it's a endless state
        if (duration == null)
            return null;

        final Instant startedAt = getStartedAt();

        // fallback to `getDuration` if state was not started yet
        if (startedAt == null)
            return duration;

        Instant now = Instant.now();

        // must use the start value after a pause to ensure that the actual start value is not modified
        // and the remaining duration of the state after a pause does not get out of sync
        if (getPauseDuration() != null)
            now = now.minus(getPauseDuration());

        final Duration start = Duration.between(getStartedAt(), getPausedAt() != null ? getPausedAt() : now);
        final Duration remaining = duration.minus(start);
        if (remaining.isNegative())
            return Duration.ZERO;

        return remaining;
    }

    public final boolean isStarted() {
        return started;
    }

    public final boolean isEnded() {
        return ended;
    }

    public final boolean isUpdating() {
        return updating;
    }

    public final boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public final boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        if (isPaused() && !paused)
            pauseDuration = Duration.between(getPausedAt(), Instant.now());

        this.paused = paused;
        this.pausedAt = paused ? Instant.now() : null;
        onPause(paused);
    }

    public final Instant getStartedAt() {
        return startedAt;
    }

    public final Instant getEndedAt() {
        return endedAt;
    }

    public final Instant getPausedAt() {
        return pausedAt;
    }

    public final Instant getInitiallyStartedAt() {
        return initiallyStartedAt;
    }

    public final Duration getPauseDuration() {
        return pauseDuration;
    }
}
