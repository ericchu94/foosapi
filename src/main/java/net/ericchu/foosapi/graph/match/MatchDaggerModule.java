package net.ericchu.foosapi.graph.match;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import net.ericchu.foosapi.graph.GraphQLModule;
import org.immutables.mongo.repository.RepositorySetup;

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
    @IntoSet
    static GraphQLModule matchModule(MatchModule module) {
        return module;
    }
}
