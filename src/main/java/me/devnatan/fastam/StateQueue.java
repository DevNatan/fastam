package me.devnatan.fastam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * StateQueue lets you compose your states sequentially. It is typical to use a state series as the
 * "main state" of a system.
 * <p>
 * StateQueue will take care of checking whether the current state is over and switch to the next
 * state in its update method. Typically, a state is over when it lasted for more than its duration.
 * <p>
 * Duration is included in State because of how common it is. If your state doesn't need duration,
 * you can override {@link State#isReadyToEnd()} to set up your own ending condition.
 */
public class StateQueue extends StateHolder {

    protected int current;
    protected boolean skipping, backing;

    public StateQueue(State... states) {
        super(new ArrayList<>(Arrays.asList(states)));
    }

    @Override
    public void reset() {
        super.reset();
        current = 0;
        skipping = false;
        backing = false;

        for (final State state : states)
            state.reset();
    }

    public State getCurrent() {
        return current >= states.size() ? null : states.get(current);
    }

    public StateQueue getCurrentAsQueue() {
        final State current = getCurrent();

        if (current instanceof StateQueue)
            return ((StateQueue) current).getCurrentAsQueue();

        return this;
    }

    public State getNext() {
        return current + 1 >= states.size() ? null : states.get(current + 1);
    }

    public void resetCurrent() {
        if (!isFrozen())
            throw new IllegalStateException("State reset cannot be performed while it's running");

        // must ensure the current state is
        current = 0;
    }

    public void addBefore(State state) {
        states.add(0, state);
    }

    public void addNext(State state) {
        states.add(current + 1, state);
    }

    public void addNext(List<State> states) {
        int index = 1;
        for (final State state : states) {
            this.states.add(current + index, state);
            ++index;
        }
    }

    public void addNext(State... states) {
        addNext(Arrays.asList(states));
    }

    public void skip() {
        skipping = true;
    }

    public void back() {
        backing = true;
    }

    @Override
    protected void onStart() {
        if (states.isEmpty()) {
            end();
            return;
        }

        states.get(current).start();
    }

    @Override
    protected void onUpdate() {
        final State currentState = states.get(current);
        currentState.update();

        if (!currentState.isFrozen() && backing) {
            backing = false;
            currentState.end();

            --current;
            if (current < 0) {
                end();
                return;
            }

            final State next = states.get(current);
            next.start();
            return;
        }

        if ((currentState.isReadyToEnd() && !currentState.isFrozen()) || skipping) {
            if (skipping) {
                skipping = false;
            }

            // Concurrent states can have children that are queues,
            // so we need those children that are queues to advance before ending the current state.
            if (currentState instanceof StateGroup && !currentState.isReadyToEnd()) {
                for (final State child : (StateGroup) currentState) {
                    if (!(child instanceof StateQueue))
                        continue;

                    final StateQueue childQueue = (StateQueue) child;
                    final boolean last = childQueue.isLastState();
                    childQueue.skip();

                    // if there are still queued states to be handled we cannot terminate the current concurrent state
                    if (!last) return;
                }
            }

            currentState.end();
            ++current;

            if (current >= states.size()) {
                end();
                return;
            }

            final State next = states.get(current);
            next.start();
        }
    }

    @Override
    protected void onEnd() {
        if (current < states.size())
            states.get(current).end();
    }

    @Override
    public void setPaused(boolean paused) {
        super.setPaused(paused);

        // needed to `getRemainingDuration` works properly
        final State current = getCurrent();
        if (current == null)
            return;

        current.setPaused(paused);
    }

    public boolean isLastState() {
        return current == states.size() - 1;
    }

    @Override
    public boolean isReadyToEnd() {
        return (isLastState() && states.get(current).isReadyToEnd());
    }

}
