package net.ericchu.foosapi.graph.player;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public abstract class PlayerPayload {
    abstract Optional<PlayerError> error();

    abstract Optional<Object> result();
}
