package net.ericchu.foosapi;

import dagger.Component;
import io.undertow.Undertow;

import javax.inject.Singleton;

@Component(modules = FoosApiModule.class)
@Singleton
abstract class FoosApi {
    abstract Undertow undertow();

    public static void main(String[] args) {
        DaggerFoosApi.create().undertow().start();
    }
}
