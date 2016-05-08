package counter;

import common.Customer;
import common.SyncMessage;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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
        MessageConsumer<String> consumer = eventBus.consumer(Customer.EVENT_CREATED);
        consumer.handler(event -> {
            if (isInitialized.get()) {
                customerCount.incrementAndGet();
            } else {
                unprocessedEvents.add(new Event(1, Long.valueOf(event.headers().get("timestamp"))));

            }
            System.out.println(customerCount.get() + "   # " + LocalDateTime.now().toString() + "   ADD    " + event.body());

        });

        eventBus.consumer(Customer.EVENT_DELETED, event -> {
            if (isInitialized.get()) {
                customerCount.decrementAndGet();
            } else {
                unprocessedEvents.add(new Event(-1, Long.valueOf(event.headers().get("timestamp"))));
            }
            System.out.println(customerCount.get() + "   # " + LocalDateTime.now().toString() + "   DELETE " + event.body());
        });

        eventBus.send(SyncMessage.SYNCHRONIZE, "", new Handler<AsyncResult<Message<String>>>() {
            @Override
            public void handle(AsyncResult<Message<String>> event) {
                //handle error
                SyncMessage syncMessage = SyncMessage.fromString(event.result().body());
                customerCount.set(syncMessage.getCustomerCount());
                System.out.println("########### I'M IN SYNC NOW, SET COUNTER TO " + syncMessage.getCustomerCount() + "############");
                isInitialized.set(true);
                unprocessedEvents.forEach(unprocessedEvent -> {
                    if (unprocessedEvent.getTimestamp() > syncMessage.getTimestamp()) {
                        customerCount.addAndGet(unprocessedEvent.delta);
                    }
                });
            }
        });
    }

    public long getCustomerCount() {
        return customerCount.get();
    }

}
