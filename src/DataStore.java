import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
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


    // SET
    public void set(String key, String value) {

        store.put(key, value);

        // SET removes previous expiry
        expiryTimes.remove(key);
    }


    // GET
    public String get(String key) {

        if (isExpired(key)) {

            delete(key);

            return null;
        }

        return store.get(key);
    }


    // DELETE
    // Supports one or multiple keys
    public int delete(String... keys) {

        int deletedCount = 0;

        for (String key : keys) {

            if (store.remove(key) != null) {

                deletedCount++;
            }

            expiryTimes.remove(key);
        }

        return deletedCount;
    }


    // EXISTS
    public boolean exists(String key) {

        if (isExpired(key)) {

            delete(key);

            return false;
        }

        return store.containsKey(key);
    }


    // EXPIRE
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


    // TTL
    public long ttl(String key) {

        if (!store.containsKey(key)) {

            return -2;
        }

        if (isExpired(key)) {

            delete(key);

            return -2;
        }

        Long expiryTime =
                expiryTimes.get(key);

        // Key has no expiry
        if (expiryTime == null) {

            return -1;
        }

        long remainingMillis =
                expiryTime
                        - System.currentTimeMillis();

        if (remainingMillis <= 0) {

            delete(key);

            return -2;
        }

        return (remainingMillis + 999) / 1000;
    }


    // KEYS
    public Set<String> keys() {

        removeExpiredKeys();

        return store.keySet();
    }


    // INCR
    public long incr(String key) {

        if (isExpired(key)) {

            delete(key);
        }

        String value =
                store.get(key);

        if (value == null) {

            value = "0";
        }

        try {

            long currentValue =
                    Long.parseLong(value);

            long newValue =
                    currentValue + 1;

            store.put(
                    key,
                    String.valueOf(newValue)
            );

            return newValue;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "ERR value is not an integer"
            );
        }
    }


    // DECR
    public long decr(String key) {

        if (isExpired(key)) {

            delete(key);
        }

        String value =
                store.get(key);

        if (value == null) {

            value = "0";
        }

        try {

            long currentValue =
                    Long.parseLong(value);

            long newValue =
                    currentValue - 1;

            store.put(
                    key,
                    String.valueOf(newValue)
            );

            return newValue;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "ERR value is not an integer"
            );
        }
    }


    // INCRBY
    public long incrBy(
            String key,
            long amount) {

        if (isExpired(key)) {

            delete(key);
        }

        String value =
                store.get(key);

        if (value == null) {

            value = "0";
        }

        try {

            long currentValue =
                    Long.parseLong(value);

            long newValue =
                    currentValue + amount;

            store.put(
                    key,
                    String.valueOf(newValue)
            );

            return newValue;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "ERR value is not an integer"
            );
        }
    }


    // DECRBY
    public long decrBy(
            String key,
            long amount) {

        if (isExpired(key)) {

            delete(key);
        }

        String value =
                store.get(key);

        if (value == null) {

            value = "0";
        }

        try {

            long currentValue =
                    Long.parseLong(value);

            long newValue =
                    currentValue - amount;

            store.put(
                    key,
                    String.valueOf(newValue)
            );

            return newValue;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "ERR value is not an integer"
            );
        }
    }


    // APPEND
    public int append(
            String key,
            String value) {

        if (isExpired(key)) {

            delete(key);
        }

        String existingValue =
                store.get(key);

        if (existingValue == null) {

            store.put(key, value);

            return value.length();
        }

        String newValue =
                existingValue + value;

        store.put(key, newValue);

        return newValue.length();
    }


    // MSET
    public void mset(
            String[] keyValuePairs) {

        for (int i = 0;
             i < keyValuePairs.length;
             i += 2) {

            String key =
                    keyValuePairs[i];

            String value =
                    keyValuePairs[i + 1];

            set(key, value);
        }
    }


    // MGET
    public String mget(String[] keys) {

        StringBuilder result =
                new StringBuilder();

        result.append("[");

        for (int i = 0;
             i < keys.length;
             i++) {

            String value =
                    get(keys[i]);

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


    // SETNX
    public boolean setNx(
            String key,
            String value) {

        if (isExpired(key)) {

            delete(key);
        }

        if (store.containsKey(key)) {

            return false;
        }

        store.put(key, value);

        return true;
    }


    // GETSET
    public String getSet(
            String key,
            String newValue) {

        if (isExpired(key)) {

            delete(key);
        }

        String oldValue =
                store.get(key);

        store.put(key, newValue);

        if (oldValue == null) {

            return "(nil)";
        }

        return oldValue;
    }


    // FLUSHALL
    public void flushAll() {

        store.clear();

        expiryTimes.clear();
    }


    // Check expiry
    private boolean isExpired(String key) {

        Long expiryTime =
                expiryTimes.get(key);

        if (expiryTime == null) {

            return false;
        }

        return System.currentTimeMillis()
                >= expiryTime;
    }


    // Automatically remove expired keys
    private void removeExpiredKeys() {

        long currentTime =
                System.currentTimeMillis();

        for (String key :
                expiryTimes.keySet()) {

            Long expiryTime =
                    expiryTimes.get(key);

            if (expiryTime != null
                    && currentTime >= expiryTime) {

                store.remove(key);

                expiryTimes.remove(key);
            }
        }
    }
}