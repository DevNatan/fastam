package me.devnatan.fastam;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * StateGroup lets you compose your states concurrently.
 * <p>
 * "concurrently" doesn't mean they'll be executed on different threads. All the states within a
 * StateGroup will be started on {@link StateGroup#start()}, similarly with {@link StateGroup#end()}.
 */
public class StateGroup extends StateHolder {

    public StateGroup(State... states) {
        super(new ArrayList<>(Arrays.asList(states)));
    }

    @Override
    protected void onStart() {
        states.forEach(State::start);
    }

    @Override
    protected void onUpdate() {
        states.forEach(State::update);
        if (states.stream().allMatch(State::isEnded))
            end();
    }

    @Override
    protected void onEnd() {
        states.forEach(State::end);
    }

    @Override
    public boolean isReadyToEnd() {
        return states.stream().allMatch(State::isReadyToEnd);
    }

    @Override
    public void setPaused(boolean paused) {
        super.setPaused(paused);

        // needed to `getRemainingDuration` works properly
        for (final State child : states)
            child.setPaused(paused);
    }

}
