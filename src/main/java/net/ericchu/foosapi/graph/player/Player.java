package net.ericchu.foosapi.graph.player;

import org.immutables.mongo.Mongo;
import org.immutables.mongo.types.Id;
import org.immutables.value.Value;

@Value.Immutable
@Mongo.Repository
public abstract class Player {
    @Mongo.Id
    @Value.Default
    public String id() {
        return Id.generate().toString();
    }

    public abstract String name();
}
