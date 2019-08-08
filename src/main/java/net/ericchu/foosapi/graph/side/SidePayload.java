package net.ericchu.foosapi.graph.side;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public abstract class SidePayload {
    abstract Optional<SideError> error();

    abstract Optional<Object> result();
}
