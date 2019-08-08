package net.ericchu.foosapi.graph.side;

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
public class SideModule {
    @Provides
    static SideRepository SideRepository(RepositorySetup repositorySetup) {
        return new SideRepository(repositorySetup);
    }

    @Provides
    @Named("sideTypeDefinitionRegistry")
    static TypeDefinitionRegistry sideTypeDefinitionRegistry() {
        try (InputStreamReader reader = new InputStreamReader(SideModule.class.getResourceAsStream("side.graphql"))) {
            return new SchemaParser().parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Named("sideTypeRuntimeWirings")
    static Collection<TypeRuntimeWiring> sideTypeRuntimeWirings(SideTypeRuntimeWirings sideTypeRuntimeWirings) {
        return sideTypeRuntimeWirings.getTypeRuntimeWirings();
    }

    @Provides
    @IntoSet
    static GraphQLModule sideModule(
            @Named("sideTypeDefinitionRegistry") TypeDefinitionRegistry sideTypeDefinitionRegistry,
            @Named("sideTypeRuntimeWirings") Collection<TypeRuntimeWiring> sideTypeRuntimeWirings) {
        return new BaseGraphQLModule(sideTypeDefinitionRegistry, sideTypeRuntimeWirings);
    }
}
