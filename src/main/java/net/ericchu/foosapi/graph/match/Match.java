package net.ericchu.foosapi.graph.match;

import org.bson.types.ObjectId;

public class Match {
    private ObjectId id;
    private String name;

    public Match() {
    }

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
