package net.ericchu.foosapi.graph.match;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import net.ericchu.foosapi.graph.GraphQLModule;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Module
public class MatchDaggerModule {
    @Provides
    static CodecRegistry codecRegistry() {
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClients.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        return pojoCodecRegistry;
    }

    @Provides
    static MongoClientSettings mongoClientSettings(CodecRegistry codecRegistry) {
        return MongoClientSettings.builder().codecRegistry(codecRegistry)
                .applyConnectionString(new ConnectionString("mongodb://localhost")).build();
    }

    @Provides
    static MongoClient mongoClient(MongoClientSettings mongoClientSettings) {
        return MongoClients.create(mongoClientSettings);
    }

    @Provides
    static MongoDatabase mongoDatabase(MongoClient mongoClient) {
        return mongoClient.getDatabase("foosapi");
    }

    @Provides
    static MongoCollection<Match> matchCollection(MongoDatabase mongoDatabase) {
        return mongoDatabase.getCollection("match", Match.class);
    }

    @Provides
    @IntoSet
    static GraphQLModule matchModule(MongoCollection<Match> matchCollection) {
        return new MatchModule(matchCollection);
    }
}
