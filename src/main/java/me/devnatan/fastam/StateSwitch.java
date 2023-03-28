package me.devnatan.fastam;

import com.sun.istack.internal.NotNull;

/**
 * Not all situations can be easily modeled using a StateSeries, for example a game's menus.
 * The player's navigation through the menus could go as such:
 *
 * <pre>
 * {@code
 * MainMenuState => OptionMenuState => MainMenuState => StartGameState
 * }
 * </pre>
 * <p>
 * This is where StateSwitch comes into play. It's a simple class which can be used as such:
 * <pre>
 * {@code
 * final StateSwitch state = new StateSwitch();
 * state.changeState(MyState("First"))
 * state.changeState(MyState("Second"))
 * }
 * </pre>
 * <p>
 * {@link StateSwitch#update} is provided as a convenience method to update the underlying state.
 */
public final class StateSwitch {

    private State state;

    public State getState() {
        return state;
    }

    public void changeState(@NotNull State next) {
        if (state != null)
            state.end();
        state = next;
        next.start();
    }

    public void update() {
        if (state == null)
            return;

        state.update();
    }

}
