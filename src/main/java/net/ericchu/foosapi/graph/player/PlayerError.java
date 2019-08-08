package net.ericchu.foosapi.graph.player;

import org.immutables.value.Value;

@Value.Immutable
public abstract class PlayerError {
    abstract PlayerErrorCode code();

    abstract String message();

    public static PlayerError of(Throwable t) {
        if (t == null)
            return null;

        return ImmutablePlayerError.builder().code(PlayerErrorCode.GENERIC).message(t.getMessage()).build();
    }
}
