package net.ericchu.foosapi;

import dagger.Component;
import io.undertow.Undertow;
import net.ericchu.foosapi.graph.GraphQLModule;

import java.util.Set;

@Component(modules = FoosApiModule.class)
abstract class FoosApi {
    abstract Undertow undertow();

    abstract Set<GraphQLModule> graphQLModules();

    public static void main(String[] args) {
        DaggerFoosApi.create().undertow().start();
    }
}
