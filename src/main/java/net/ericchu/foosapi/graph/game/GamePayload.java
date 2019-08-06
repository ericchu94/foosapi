package net.ericchu.foosapi.graph.game;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public abstract class GamePayload {
    abstract Optional<GameError> error();

    abstract Optional<Object> result();
}
