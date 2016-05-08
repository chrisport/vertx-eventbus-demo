package counter;

import common.Customer;
import common.DateUtil;
import common.SyncMessage;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class CustomerCounter {

    @Data
    @AllArgsConstructor
    private class Event {
        private int delta;
        private long timestamp;
    }

    private final AtomicLong customerCount = new AtomicLong(0);
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final List<Event> unprocessedEvents = Collections.synchronizedList(new ArrayList<>());

    public CustomerCounter(EventBus eventBus) {
        this.registerOnEvents(eventBus);
    }

    private void registerOnEvents(EventBus eventBus) {
        eventBus.consumer(Customer.EVENT_CREATED, this::processCustomerDeletionEvent);
        eventBus.consumer(Customer.EVENT_DELETED, this::processCustomerCreationEvent);
        eventBus.consumer(Customer.EVENT_PUBLISHER_JOIN, event -> this.syncWithProducer(eventBus));
        this.syncWithProducer(eventBus);
    }

    private void processCustomerDeletionEvent(Message<String> event) {
        if (isInitialized.get()) {
            customerCount.incrementAndGet();
        } else {
            unprocessedEvents.add(new Event(1, Long.valueOf(event.headers().get("timestamp"))));

        }
        log.info(customerCount.get() + "   # " + DateUtil.timeNowFormatted() + "   ADD    " + event.body());
    }

    private void processCustomerCreationEvent(Message<Object> event) {
        if (isInitialized.get()) {
            customerCount.decrementAndGet();
        } else {
            unprocessedEvents.add(new Event(-1, Long.valueOf(event.headers().get("timestamp"))));
        }
        log.info(customerCount.get() + "   # " + DateUtil.timeNowFormatted() + "   DELETE " + event.body());
    }

    private void syncWithProducer(EventBus eventBus) {
        eventBus.send(SyncMessage.SYNCHRONIZE, "", event -> {
            //handle error
            SyncMessage syncMessage = SyncMessage.fromString((String) event.result().body());
            customerCount.set(syncMessage.getCustomerCount());
            log.info("########### I'M IN SYNC NOW, SETTING COUNTER TO " + syncMessage.getCustomerCount() + " ############");
            isInitialized.set(true);
            unprocessedEvents.forEach(unprocessedEvent -> {
                if (unprocessedEvent.getTimestamp() >= syncMessage.getTimestamp()) {
                    customerCount.addAndGet(unprocessedEvent.delta);
                }
            });
        });
    }
}
