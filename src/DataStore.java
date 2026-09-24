import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataStore {

    private final Map<String, String> store = new ConcurrentHashMap<>();

    private final Map<String, Long> expiryTimes = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();


    public DataStore() {

        scheduler.scheduleAtFixedRate(
                this::removeExpiredKeys,
                1,
                1,
                TimeUnit.SECONDS
        );
    }


    // SET command
    public void set(String key, String value) {

        store.put(key, value);

        // New SET removes any previous expiry
        expiryTimes.remove(key);
    }


    // GET command
    public String get(String key) {

        if (isExpired(key)) {
            delete(key);
            return null;
        }

        return store.get(key);
    }


    // DELETE command
    public boolean delete(String key) {

        boolean removed = store.remove(key) != null;

        expiryTimes.remove(key);

        return removed;
    }


    // EXISTS command
    public boolean exists(String key) {

        if (isExpired(key)) {
            delete(key);
            return false;
        }

        return store.containsKey(key);
    }


    // EXPIRE command
    public boolean expire(String key, long seconds) {

        if (!store.containsKey(key)) {
            return false;
        }

        if (isExpired(key)) {
            delete(key);
            return false;
        }

        long expiryTime =
                System.currentTimeMillis()
                        + (seconds * 1000);

        expiryTimes.put(key, expiryTime);

        return true;
    }


    // TTL command
    public long ttl(String key) {

        if (!store.containsKey(key)) {
            return -2;
        }

        if (isExpired(key)) {
            delete(key);
            return -2;
        }

        Long expiryTime = expiryTimes.get(key);

        // Key exists but has no expiry
        if (expiryTime == null) {
            return -1;
        }

        long remainingMillis =
                expiryTime - System.currentTimeMillis();

        if (remainingMillis <= 0) {
            delete(key);
            return -2;
        }

        return (remainingMillis + 999) / 1000;
    }


    // KEYS command
    public Set<String> keys() {

        removeExpiredKeys();

        return store.keySet();
    }


    // INCR command
    public long incr(String key) {

        if (isExpired(key)) {
            delete(key);
        }

        String value = store.get(key);

        if (value == null) {
            value = "0";
        }

        try {

            long currentValue = Long.parseLong(value);

            long newValue = currentValue + 1;

            store.put(key, String.valueOf(newValue));

            return newValue;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "ERR value is not an integer"
            );
        }
    }


    // DECR command
    public long decr(String key) {

        if (isExpired(key)) {
            delete(key);
        }

        String value = store.get(key);

        if (value == null) {
            value = "0";
        }

        try {

            long currentValue = Long.parseLong(value);

            long newValue = currentValue - 1;

            store.put(key, String.valueOf(newValue));

            return newValue;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "ERR value is not an integer"
            );
        }
    }


    // INCRBY command
    public long incrBy(String key, long amount) {

        if (isExpired(key)) {
            delete(key);
        }

        String value = store.get(key);

        if (value == null) {
            value = "0";
        }

        try {

            long currentValue = Long.parseLong(value);

            long newValue = currentValue + amount;

            store.put(key, String.valueOf(newValue));

            return newValue;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "ERR value is not an integer"
            );
        }
    }


    // DECRBY command
    public long decrBy(String key, long amount) {

        if (isExpired(key)) {
            delete(key);
        }

        String value = store.get(key);

        if (value == null) {
            value = "0";
        }

        try {

            long currentValue = Long.parseLong(value);

            long newValue = currentValue - amount;

            store.put(key, String.valueOf(newValue));

            return newValue;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "ERR value is not an integer"
            );
        }
    }


    // APPEND command
    public int append(String key, String value) {

        if (isExpired(key)) {
            delete(key);
        }

        String existingValue = store.get(key);

        if (existingValue == null) {

            store.put(key, value);

            return value.length();
        }

        String newValue = existingValue + value;

        store.put(key, newValue);

        return newValue.length();
    }


    // MSET command
    public void mset(String[] keyValuePairs) {

        for (int i = 0; i < keyValuePairs.length; i += 2) {

            String key = keyValuePairs[i];

            String value = keyValuePairs[i + 1];

            set(key, value);
        }
    }


    // MGET command
    public String mget(String[] keys) {

        StringBuilder result = new StringBuilder();

        result.append("[");

        for (int i = 0; i < keys.length; i++) {

            String value = get(keys[i]);

            if (value == null) {

                result.append("(nil)");

            } else {

                result.append(value);
            }

            if (i < keys.length - 1) {

                result.append(", ");
            }
        }

        result.append("]");

        return result.toString();
    }


    // FLUSHALL command
    public void flushAll() {

        store.clear();

        expiryTimes.clear();
    }


    // Check whether key is expired
    private boolean isExpired(String key) {

        Long expiryTime = expiryTimes.get(key);

        if (expiryTime == null) {
            return false;
        }

        return System.currentTimeMillis() >= expiryTime;
    }


    // Remove expired keys automatically
    private void removeExpiredKeys() {

        long currentTime = System.currentTimeMillis();

        for (String key : expiryTimes.keySet()) {

            Long expiryTime = expiryTimes.get(key);

            if (expiryTime != null
                    && currentTime >= expiryTime) {

                store.remove(key);

                expiryTimes.remove(key);
            }
        }
    }
}