package net.ericchu.foosapi.di;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import net.ericchu.foosapi.graph.match.Match;
import net.ericchu.foosapi.graph.match.MatchModule;
import org.bson.Document;

public class Provider {
    private static MongoClient mongoClient() {
        return MongoClients.create("mongodb://localhost");
    }

    private static MongoDatabase mongoDatabase() {
        return mongoClient().getDatabase("foosapi");
    }

    private static MongoCollection<Document> matchCollection() {
        return mongoDatabase().getCollection("match");
    }

    public static MatchModule matchModule() {
        return new MatchModule(matchCollection());
    }
}
