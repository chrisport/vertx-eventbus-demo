package common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SyncMessage {
    public static final String SYNCHRONIZE = "sync";

    private long customerCount;
    private long timestamp;

    public static String toString(long customerCount) {
        // life is boring
        return customerCount + "#" + System.currentTimeMillis();
    }

    public static SyncMessage fromString(String customerString) {
        String[] properties = customerString.split("#");
        Long count = Long.parseLong(properties[0]);
        Long timeStamp = Long.parseLong(properties[1]);
        return new SyncMessage(count, timeStamp);
    }
}
