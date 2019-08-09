package net.ericchu.foosapi.mongo;

import dagger.Module;
import dagger.Provides;
import org.immutables.mongo.repository.RepositorySetup;

import javax.inject.Singleton;

@Module
public class MongoModule {
    @Provides
    @Singleton
    static RepositorySetup repositorySetup() {
        return RepositorySetup.forUri("mongodb://localhost/foosapi");
    }
}
