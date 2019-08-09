package net.ericchu.foosapi.pubsub;

import com.google.common.eventbus.EventBus;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class PubSubModule {
    @Provides
    @Singleton
    static EventBus eventBus() {
        return new EventBus();
    }
}
