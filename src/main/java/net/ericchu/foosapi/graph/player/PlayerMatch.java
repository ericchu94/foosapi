package net.ericchu.foosapi.graph.player;

import org.immutables.mongo.Mongo;
import org.immutables.mongo.types.Id;
import org.immutables.value.Value;

@Value.Immutable
@Mongo.Repository
public abstract class PlayerMatch {
    @Mongo.Id
    @Value.Default
    public String id() {
        return Id.generate().toString();
    }

    public abstract String playerId();

    public abstract String matchId();

    public abstract Spot spot();
}
