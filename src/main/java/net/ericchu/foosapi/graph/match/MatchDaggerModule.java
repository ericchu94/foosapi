package net.ericchu.foosapi.graph.match;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import net.ericchu.foosapi.graph.GraphQLModule;
import org.bson.Document;

@Module
public class MatchDaggerModule {
    @Provides
    static MongoClient mongoClient() {
        return MongoClients.create("mongodb://localhost");
    }

    @Provides
    static MongoDatabase mongoDatabase(MongoClient mongoClient) {
        return mongoClient.getDatabase("foosapi");
    }

    @Provides
    static MongoCollection<Document> matchCollection(MongoDatabase mongoDatabase) {
        return mongoDatabase.getCollection("match");
    }

    @Provides @IntoSet
    static GraphQLModule matchModule(MongoCollection<Document> matchCollection) {
        return new MatchModule(matchCollection);
    }
}
