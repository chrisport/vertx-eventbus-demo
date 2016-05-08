package counter;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;

/**
 * Created by ugur on 14.01.2016.
 * <p/>
 * Contributor:
 * Christoph Portmann
 */
public class ConsumerDemoApplication {

    public ConsumerDemoApplication() {
        VertxOptions options = new VertxOptions();
        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                EventBus eventBus = vertx.eventBus();

                System.out.println("We now have a clustered event bus: " + eventBus);
                new CustomerCounter(eventBus);
            } else {
                System.out.println("Failed: " + res.cause());
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        new ConsumerDemoApplication();
    }
}
