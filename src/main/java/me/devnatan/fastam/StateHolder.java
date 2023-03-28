package me.devnatan.fastam;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class StateHolder extends State implements Iterable<State> {

    protected final List<State> states;

    public StateHolder() {
        this(new ArrayList<>());
    }

    public StateHolder(List<State> states) {
        this.states = states;
    }

    public void add(State state) {
        states.add(state);
    }

    public void addAll(Collection<State> states) {
        this.states.addAll(states);
    }

    @Override
    public void setFrozen(boolean frozen) {
        for (final State state : states)
            state.setFrozen(frozen);
        super.setFrozen(frozen);
    }

    @Override
    public Iterator<State> iterator() {
        return states.iterator();
    }

    public List<State> getStates() {
        return states;
    }

    @Override
    public Duration getDuration() {
        Duration duration = Duration.ZERO;
        for (final State state : states) {
            final Duration stateDuration = state.getDuration();
            if (stateDuration == null)
                continue;

            duration = duration.plus(stateDuration);
        }

        return duration;
    }

    @Override
    public Duration getRemainingDuration() {
        Duration duration = Duration.ZERO;
        for (final State state : states) {
            final Duration stateDuration = state.getRemainingDuration();
            if (stateDuration == null)
                continue;

            duration = duration.plus(stateDuration);
        }

        return duration;
    }

}

