package net.ericchu.foosapi.graph.match;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public abstract class MatchPayload {
    abstract Optional<MatchError> error();

    abstract Optional<Object> result();
}
