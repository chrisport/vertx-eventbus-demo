package counter;

import common.ClusteredEventBus;

/**
 * Created by ugur on 14.01.2016.
 * <p/>
 * Contributor:
 * Christoph Portmann
 */
public class ConsumerDemoApplication {

    public static void main(String[] args) throws InterruptedException {
        ClusteredEventBus.applyOnClusteredEventBus(CustomerCounter::new);
    }
}
