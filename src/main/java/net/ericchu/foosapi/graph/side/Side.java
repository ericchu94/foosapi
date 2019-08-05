package net.ericchu.foosapi.graph.side;

import org.immutables.mongo.Mongo;
import org.immutables.mongo.types.Id;
import org.immutables.value.Value;

@Value.Immutable
@Mongo.Repository
public abstract class Side {
    @Mongo.Id
    @Value.Default
    public String id() {
        return Id.generate().toString();
    }

    public abstract String gameId();

    @Value.Default
    public int points() {
        return 0;
    }

    public abstract Color color();
}
