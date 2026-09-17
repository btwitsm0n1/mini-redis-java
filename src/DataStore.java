import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataStore {

    private final Map<String, String> store =
            new ConcurrentHashMap<>();

    private final Map<String, Long> expiryTimes =
            new ConcurrentHashMap<>();

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

        // A new SET removes any previous expiry
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
    public void delete(String key) {

        store.remove(key);
        expiryTimes.remove(key);
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

        // Round up to the next second
        return (remainingMillis + 999) / 1000;
    }

    // KEYS command
    public Set<String> keys() {

        // Remove expired keys before returning keys
        removeExpiredKeys();

        return store.keySet();
    }

    // Return total number of keys
    public int size() {

        // Remove expired keys before counting
        removeExpiredKeys();

        return store.size();
    }

    // INCR command
    public long incr(String key) {

        // Remove key if it has expired
        if (isExpired(key)) {
            delete(key);
        }

        String value = store.get(key);

        // If key does not exist, start from 1
        if (value == null) {
            store.put(key, "1");
            return 1;
        }

        try {
            long number = Long.parseLong(value);

            number++;

            store.put(key, String.valueOf(number));

            return number;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "value is not an integer"
            );
        }
    }

    // DECR command
    public long decr(String key) {

        // Remove key if it has expired
        if (isExpired(key)) {
            delete(key);
        }

        String value = store.get(key);

        // If key does not exist, start from -1
        if (value == null) {
            store.put(key, "-1");
            return -1;
        }

        try {
            long number = Long.parseLong(value);

            number--;

            store.put(key, String.valueOf(number));

            return number;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "value is not an integer"
            );
        }
    }

    // FLUSHALL command
    public void flushAll() {

        store.clear();
        expiryTimes.clear();
    }

    // Check whether a key is expired
    private boolean isExpired(String key) {

        Long expiryTime = expiryTimes.get(key);

        if (expiryTime == null) {
            return false;
        }

        return System.currentTimeMillis() >= expiryTime;
    }

    // Automatically remove expired keys
    private void removeExpiredKeys() {

        long currentTime = System.currentTimeMillis();

        for (Map.Entry<String, Long> entry :
                expiryTimes.entrySet()) {

            String key = entry.getKey();
            long expiryTime = entry.getValue();

            if (currentTime >= expiryTime) {

                store.remove(key);
                expiryTimes.remove(key);
            }
        }
    }
}