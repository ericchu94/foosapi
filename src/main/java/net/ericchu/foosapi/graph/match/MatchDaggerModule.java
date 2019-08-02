package net.ericchu.foosapi.graph.match;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import net.ericchu.foosapi.graph.GraphQLModule;
import org.bson.types.ObjectId;
import org.immutables.mongo.repository.RepositorySetup;

import java.util.ServiceLoader;
import java.util.concurrent.Executors;

@Module
public class MatchDaggerModule {
    @Provides
    static RepositorySetup repositorySetup() {
        return RepositorySetup.forUri("mongodb://localhost/foosapi");
    }

    @Provides
    static MatchRepository matchRepository(RepositorySetup repositorySetup) {
        return new MatchRepository(repositorySetup);
    }

    @Provides
    static MatchService matchService(MatchRepository matchRepository) {
        return new MatchService(matchRepository);
    }

    @Provides
    @IntoSet
    static GraphQLModule matchModule(MatchService matchService) {
        return new MatchModule(matchService);
    }
}
