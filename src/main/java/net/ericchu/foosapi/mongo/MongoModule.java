package net.ericchu.foosapi.mongo;

import dagger.Module;
import dagger.Provides;
import org.immutables.mongo.repository.RepositorySetup;

@Module
public class MongoModule {
    @Provides
    static RepositorySetup repositorySetup() {
        return RepositorySetup.forUri("mongodb://localhost/foosapi");
    }
}
