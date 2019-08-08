package net.ericchu.foosapi.graph.player;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import net.ericchu.foosapi.graph.BaseGraphQLModule;
import net.ericchu.foosapi.graph.GraphQLModule;
import net.ericchu.foosapi.mongo.MongoModule;
import org.immutables.mongo.repository.RepositorySetup;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

@Module(includes = MongoModule.class)
public class PlayerModule {
    @Provides
    static PlayerRepository playerRepository(RepositorySetup repositorySetup) {
        return new PlayerRepository(repositorySetup);
    }

    @Provides
    static PlayerSideRepository playerSideRepository(RepositorySetup repositorySetup) {
        return new PlayerSideRepository(repositorySetup);
    }

    @Provides
    @Named("playerTypeDefinitionRegistry")
    static TypeDefinitionRegistry playerTypeDefinitionRegistry() {
        try (InputStreamReader reader = new InputStreamReader(
                PlayerModule.class.getResourceAsStream("player.graphql"))) {
            return new SchemaParser().parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Named("playerTypeRuntimeWirings")
    static Collection<TypeRuntimeWiring> playerTypeRuntimeWirings(PlayerTypeRuntimeWirings playerTypeRuntimeWirings) {
        return playerTypeRuntimeWirings.getTypeRuntimeWirings();
    }

    @Provides
    @IntoSet
    static GraphQLModule playerModule(
            @Named("playerTypeDefinitionRegistry") TypeDefinitionRegistry playerTypeDefinitionRegistry,
            @Named("playerTypeRuntimeWirings") Collection<TypeRuntimeWiring> playerTypeRuntimeWirings) {
        return new BaseGraphQLModule(playerTypeDefinitionRegistry, playerTypeRuntimeWirings);
    }
}
