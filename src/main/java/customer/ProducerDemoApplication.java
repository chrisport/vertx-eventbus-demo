package customer;

import common.ClusteredEventBus;
import common.Customer;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.UUID;

/**
 * Created by ugur on 14.01.2016.
 * <p/>
 * Contributor:
 * Christoph Portmann
 */
@Slf4j
public class ProducerDemoApplication {

    private static Random random = new Random(System.currentTimeMillis());


    public static void main(String[] args) throws InterruptedException {
        new ProducerDemoApplication().start();
    }

    public void start() {
        ClusteredEventBus.applyOnClusteredEventBus(eventBus -> {
            EventBus eventBus1 = Vertx.currentContext().owner().eventBus();
            markTerritory(eventBus1);

            CustomerService customerService = new CustomerService(eventBus);
            Vertx.currentContext().owner().setPeriodic(1100, event -> {
                int result = random.nextInt(100);
                if (result % 2 == 0) {
                    customerService.create(new Customer("Customer"));
                } else {
                    Customer customer = customerService.randomCustomer();
                    if (customer != null) {
                        customerService.delete(customer.getId());
                    }
                }
            });

        });
    }

    private static void markTerritory(EventBus eventBus) {
        String uuid = UUID.randomUUID().toString();
        eventBus.consumer("customer_producer_territory", event -> event.reply(uuid));
        eventBus.send("customer_producer_territory", uuid, event -> {
            if (uuid != event.result().body()) {
                log.info("Another Producer is already active in this territory.");
                Vertx.currentContext().owner().close();
            }
        });
    }

}
