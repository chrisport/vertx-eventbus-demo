package customer;

import common.Customer;
import common.SyncMessage;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ugur on 14.01.2016.
 * <p/>
 * Contributor:
 * Christoph Portmann
 */
public class CustomerService {

    private final Map<Long, Customer> customerStore = Collections.synchronizedMap(new HashMap<>());
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final EventBus eventBus;

    public CustomerService(EventBus eventBus) {
        this.eventBus = eventBus;
        registerConsumers(eventBus);
    }

    private DeliveryOptions generateTimestampedDeliverOptions() {
        DeliveryOptions options = new DeliveryOptions();
        options.addHeader("timestamp", String.valueOf(System.currentTimeMillis()));
        return options;
    }

    private void registerConsumers(EventBus eventBus) {
        eventBus.consumer(SyncMessage.SYNCHRONIZE, event -> event.reply(SyncMessage.toString(customerStore.size())));
    }

    public Customer randomCustomer() {
        Iterator<Customer> iterator = customerStore.values().iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    public Customer create(Customer customer) {
        Customer createdCustomer = new Customer(idGenerator.getAndIncrement(), customer);
        customerStore.put(createdCustomer.getId(), createdCustomer);
        publish(Customer.EVENT_CREATED, createdCustomer);
        System.out.println(customerStore.size() + "   # " + LocalDateTime.now().toString() + "  ADDED  " + createdCustomer.getId());
        return new Customer(createdCustomer);
    }

    public Customer delete(Long id) {
        Customer deletedCustomer = customerStore.remove(id);
        if (deletedCustomer != null) {
            publish(Customer.EVENT_DELETED, deletedCustomer);
            System.out.println(customerStore.size() + "   # " + LocalDateTime.now().toString() + "  DELETED    " + id);
        }
        return deletedCustomer;
    }

    private void publish(String event, Customer customer) {
        eventBus.publish(event, Customer.fromCustomer(customer), generateTimestampedDeliverOptions());
    }

}
