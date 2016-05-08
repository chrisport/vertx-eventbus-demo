package customer;

import common.Customer;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;

import java.util.Random;

/**
 * Created by ugur on 14.01.2016.
 * <p/>
 * Contributor:
 * Christoph Portmann
 */
public class ProducerDemoApplication {

    private static Random random = new Random(System.currentTimeMillis());

    public ProducerDemoApplication() {
        VertxOptions options = new VertxOptions();
        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                EventBus eventBus = vertx.eventBus();

                System.out.println("We now have a clustered event bus: " + eventBus);
                register(eventBus);
            } else {
                System.out.println("Failed: " + res.cause());
            }
        });
    }

    private void register(EventBus eventBus) {
        CustomerService customerService = new CustomerService(eventBus);

        Customer josh = customerService.create(new Customer("Josh"));
        customerService.create(new Customer("David"));

        customerService.delete(josh.getId());

        Vertx.currentContext().owner().setPeriodic(1100, event -> {
            int result = random.nextInt(100);
            if (result% 2 == 0) {
                customerService.create(new Customer("Customer"));
            } else {
                Customer customer = customerService.randomCustomer();
                if (customer != null) {
                    customerService.delete(customer.getId());
                }
            }
        });

    }

    public static void main(String[] args) throws InterruptedException {
        new ProducerDemoApplication();
    }

}
