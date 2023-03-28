package me.devnatan.fastam;

import java.util.List;
import java.util.Objects;

/**
 * In some cases, you can't know all the states which are going to be needed at initialization ahead of time in a
 * StateQueue.
 * <p>
 * For example, when modeling Build Battle, the game starts with 12 players all building at the same time for 5 minutes.
 * After the build time, players are teleported to each build for 30 seconds one at a time for judging.
 * Builds of players who left aren't available for judging. This situation can modeled like so:
 * <pre>
 * {@code
 * StateSeries:
 *     1. StateGroup(12 x BuildState)
 *     2. PlayerCheckStateProxy => Creates 1 VoteState for each player in the game
 *     3. AnnounceWinnerState
 * }
 * </pre>
 * <p>
 * A StateProxy may be implemented like this:
 * <pre>
 * {@code
 * public final class TwelveYearsAState extends StateProxy {
 *
 *     public TwelveYearsAState(@NotNull StateQueue queue) {
 *         super(queue);
 *     }
 *
 *     public List<State> createStates() {
 *         return IntStream.range(1, 12)
 *              .mapToObj(value -> new PrintState("Proxied state of " + value))
 *              .collect(Collectors.toList());
 *     }
 * }
 * }
 * </pre>
 */
public abstract class StateProxy extends State {

    private final StateQueue queue;

    public StateProxy(StateQueue queue) {
        this.queue = Objects.requireNonNull(queue);
    }

    public final StateQueue getQueue() {
        return queue;
    }

    @Override
    protected void onStart() {
        queue.addNext(createStates());
    }

    public abstract List<State> createStates();

}
