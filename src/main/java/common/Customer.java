package common;

import lombok.Data;
import lombok.ToString;

/**
 * Created by ugur on 14.01.2016.
 * <p>
 * Contributor:
 * Christoph Portmann
 */
@Data
@ToString
public class Customer {

    public static final String EVENT_CREATED = "customer_created_event";
    public static final String EVENT_DELETED = "customer_deleted_event";
    public static final String EVENT_PUBLISHER_JOIN = "customer_publisher_join_event";

    private final Long id;
    private final String name;

    public Customer(Customer other) {
        this.id = other.id;
        this.name = other.name;
    }

    public Customer(String name) {
        this.id = null;
        this.name = name;

    }

    public Customer(Long id, Customer other) {
        this.id = id;
        this.name = other.name;
    }
}
