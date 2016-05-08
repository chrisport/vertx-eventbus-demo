package common;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;
@Slf4j
public class ClusteredEventBus {

    public static void applyOnClusteredEventBus(Consumer<EventBus> vertxConsumer){
        VertxOptions options = new VertxOptions();
        options.setClusterHost("127.0.0.1");
        options.setClustered(true);
        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                EventBus eventBus = vertx.eventBus();

                log.info("We now have a clustered event bus: {}", eventBus);
                vertxConsumer.accept(eventBus);
            } else {
                log.info("Failed: {}", res.cause());
            }
        });
    }
}
