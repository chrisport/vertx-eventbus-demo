package common;

import lombok.Data;
import lombok.ToString;

/**
 * Created by ugur on 14.01.2016.
 * <p/>
 * Contributor:
 * Christoph Portmann
 */
@Data
@ToString
public class Customer {

    public static final String EVENT_ADDRESS = "customer_address";
    public static final String EVENT_CREATED = "customer_created_event";
    public static final String EVENT_DELETED = "customer_deleted_event";
    public static final String EVENT_STATUS_CHANGED = "customer_status_change_event";

    private final Long id;
    private final String name;
    private Status status = Status.ACTIVE;

    public Customer(Customer other) {
        this.id = other.id;
        this.name = other.name;
        this.status = other.status;
    }

    public Customer(String name) {
        this.id = null;
        this.name = name;

    }

    public Customer(Long id, Customer other) {
        this.id = id;
        this.name = other.name;
    }

    public Customer(Long id, String name, Status status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public static String fromCustomer(Customer customer) {
        // life is boring
        return customer.getId() + "#" + customer.getName() + "#" + customer.getStatus();
    }

    public static Customer fromString(String customerString) {
        String[] properties = customerString.split("#");
        Long id = Long.parseLong(properties[0]);
        String name = properties[1];
        Customer.Status status = Customer.Status.valueOf(properties[2]);
        return new Customer(id, name, status);
    }

    public enum Status {
        ACTIVE, PASSIVE, BLOCKED
    }
}
