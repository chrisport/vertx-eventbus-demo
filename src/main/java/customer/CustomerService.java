package customer;

import common.Customer;
import common.DateUtil;
import common.SyncMessage;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ugur on 14.01.2016.
 * <p>
 * Contributor:
 * Christoph Portmann
 */
@Slf4j
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
        log.info(customerStore.size() + "   # " + DateUtil.timeNowFormatted() + "  ADDED    #" + createdCustomer.getId());
        return new Customer(createdCustomer);
    }

    public Customer delete(Long id) {
        Customer deletedCustomer = customerStore.remove(id);
        if (deletedCustomer != null) {
            publish(Customer.EVENT_DELETED, deletedCustomer);
            log.info(customerStore.size() + "   # " + DateUtil.timeNowFormatted() + "  DELETED  #" + id);
        }
        return deletedCustomer;
    }

    private void publish(String event, Customer customer) {
        eventBus.publish(event, customer.toString(), generateTimestampedDeliverOptions());
    }

}
