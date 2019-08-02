package net.ericchu.foosapi.graph.match;

public class Match {
    private final String _id;
    private final String name;

    public Match(String id, String name) {
        this._id = id;
        this.name = name;
    }

    public String get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }
}
