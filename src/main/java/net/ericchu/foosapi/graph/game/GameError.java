package net.ericchu.foosapi.graph.game;

import org.immutables.value.Value;

@Value.Immutable
public abstract class GameError {
    abstract GameErrorCode code();

    abstract String message();

    public static GameError of(Throwable t) {
        if (t == null)
            return null;

        return ImmutableGameError.builder().code(GameErrorCode.GENERIC).message(t.getMessage()).build();
    }
}
