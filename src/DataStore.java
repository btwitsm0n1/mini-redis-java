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

    private final Map<String, java.util.List<String>> lists =
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

    // ---------------- SET ----------------

    public void set(String key, String value) {

        store.put(key, value);
        expiryTimes.remove(key);
    }

    // ---------------- GET ----------------

    public String get(String key) {

        if (isExpired(key)) {
            delete(key);
            return "(nil)";
        }

        String value = store.get(key);

        if (value == null) {
            return "(nil)";
        }

        return value;
    }

    // ---------------- DELETE ----------------

    public int delete(String... keys) {

        int deleted = 0;

        for (String key : keys) {

            if (isExpired(key)) {
                expiryTimes.remove(key);
            }

            if (store.remove(key) != null) {
                deleted++;
            }

            expiryTimes.remove(key);
        }

        return deleted;
    }

    // ---------------- EXISTS ----------------

    public boolean exists(String key) {

        if (isExpired(key)) {
            delete(key);
            return false;
        }

        return store.containsKey(key);
    }

    // ---------------- EXPIRE ----------------

    public boolean expire(String key, long seconds) {

        if (isExpired(key)) {
            delete(key);
            return false;
        }

        if (!store.containsKey(key)) {
            return false;
        }

        long expiryTime =
                System.currentTimeMillis()
                        + (seconds * 1000);

        expiryTimes.put(key, expiryTime);

        return true;
    }

    // ---------------- TTL ----------------

    public long ttl(String key) {

        if (isExpired(key)) {
            delete(key);
            return -2;
        }

        if (!store.containsKey(key)) {
            return -2;
        }

        Long expiryTime =
                expiryTimes.get(key);

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

    // ---------------- KEYS ----------------

    public Set<String> keys() {

        removeExpiredKeys();

        return store.keySet();
    }

    // ---------------- INCR ----------------

    public long incr(String key) {

        return incrBy(key, 1);
    }

    // ---------------- DECR ----------------

    public long decr(String key) {

        return decrBy(key, 1);
    }

    // ---------------- INCRBY ----------------

    public long incrBy(
            String key,
            long amount
    ) {

        if (isExpired(key)) {
            delete(key);
        }

        String value = store.get(key);

        long currentValue = 0;

        if (value != null) {

            try {

                currentValue =
                        Long.parseLong(value);

            } catch (NumberFormatException e) {

                throw new IllegalArgumentException(
                        "ERR value is not an integer"
                );
            }
        }

        long newValue =
                currentValue + amount;

        store.put(
                key,
                String.valueOf(newValue)
        );

        expiryTimes.remove(key);

        return newValue;
    }

    // ---------------- DECRBY ----------------

    public long decrBy(
            String key,
            long amount
    ) {

        return incrBy(key, -amount);
    }

    // ---------------- APPEND ----------------

    public int append(
            String key,
            String value
    ) {

        if (isExpired(key)) {
            delete(key);
        }

        String oldValue =
                store.get(key);

        if (oldValue == null) {
            oldValue = "";
        }

        String newValue =
                oldValue + value;

        store.put(
                key,
                newValue
        );

        expiryTimes.remove(key);

        return newValue.length();
    }

    // ---------------- MSET ----------------

    public void mset(
            String[] keyValuePairs
    ) {

        for (
                int i = 0;
                i < keyValuePairs.length;
                i += 2
        ) {

            String key =
                    keyValuePairs[i];

            String value =
                    keyValuePairs[i + 1];

            set(key, value);
        }
    }

    // ---------------- MGET ----------------

    public String mget(String[] keys) {

        StringBuilder result =
                new StringBuilder();

        result.append("[");

        for (
                int i = 0;
                i < keys.length;
                i++
        ) {

            if (i > 0) {
                result.append(", ");
            }

            result.append(
                    get(keys[i])
            );
        }

        result.append("]");

        return result.toString();
    }

    // ---------------- SETNX ----------------

    public boolean setNx(
            String key,
            String value
    ) {

        if (isExpired(key)) {
            delete(key);
        }

        if (store.containsKey(key)) {
            return false;
        }

        store.put(key, value);

        expiryTimes.remove(key);

        return true;
    }

    // ---------------- GETSET ----------------

    public String getSet(
            String key,
            String newValue
    ) {

        if (isExpired(key)) {
            delete(key);
        }

        String oldValue =
                store.get(key);

        store.put(
                key,
                newValue
        );

        expiryTimes.remove(key);

        if (oldValue == null) {
            return "(nil)";
        }

        return oldValue;
    }

    // ---------------- LPUSH ----------------

    public int lpush(
            String key,
            String value
    ) {

        lists.putIfAbsent(
                key,
                java.util.Collections.synchronizedList(
                        new java.util.LinkedList<>()
                )
        );

        java.util.List<String> list =
                lists.get(key);

        synchronized (list) {

            list.add(0, value);

            return list.size();
        }
    }

    // ---------------- LRANGE ----------------

    public String lrange(
            String key,
            int start,
            int end
    ) {

        java.util.List<String> list =
                lists.get(key);

        if (list == null) {
            return "[]";
        }

        synchronized (list) {

            int size =
                    list.size();

            if (size == 0) {
                return "[]";
            }

            // Convert negative start index
            if (start < 0) {
                start = size + start;
            }

            // Convert negative end index
            if (end < 0) {
                end = size + end;
            }

            // Start cannot be less than 0
            if (start < 0) {
                start = 0;
            }

            // End cannot exceed last index
            if (end >= size) {
                end = size - 1;
            }

            // Invalid range
            if (
                    start > end ||
                    start >= size
            ) {

                return "[]";
            }

            StringBuilder result =
                    new StringBuilder();

            result.append("[");

            for (
                    int i = start;
                    i <= end;
                    i++
            ) {

                if (i > start) {
                    result.append(", ");
                }

                result.append(
                        list.get(i)
                );
            }

            result.append("]");

            return result.toString();
        }
    }

    // ---------------- RPUSH ----------------

    public int rpush(
            String key,
            String value
    ) {

        lists.putIfAbsent(
                key,
                java.util.Collections.synchronizedList(
                        new java.util.LinkedList<>()
                )
        );

        java.util.List<String> list =
                lists.get(key);

        synchronized (list) {

            list.add(value);

            return list.size();
        }
    }

    // ---------------- LPOP ----------------

    public String lpop(String key) {

        java.util.List<String> list =
                lists.get(key);

        if (list == null) {
            return "(nil)";
        }

        synchronized (list) {

            if (list.isEmpty()) {
                return "(nil)";
            }

            return list.remove(0);
        }
    }

    // ---------------- RPOP ----------------

    public String rpop(String key) {

        java.util.List<String> list =
                lists.get(key);

        if (list == null) {
            return "(nil)";
        }

        synchronized (list) {

            if (list.isEmpty()) {
                return "(nil)";
            }

            return list.remove(list.size() - 1);
        }
    }

    // ---------------- LLEN ----------------

    public int llen(String key) {

        java.util.List<String> list =
                lists.get(key);

        if (list == null) {
            return 0;
        }

        synchronized (list) {

            return list.size();
        }
    }

    // ---------------- FLUSHALL ----------------

    public void flushAll() {

        store.clear();

        expiryTimes.clear();

        lists.clear();
    }

    // ---------------- EXPIRY CHECK ----------------

    private boolean isExpired(String key) {

        Long expiryTime =
                expiryTimes.get(key);

        if (expiryTime == null) {
            return false;
        }

        return System.currentTimeMillis()
                >= expiryTime;
    }

    // ---------------- REMOVE EXPIRED ----------------

    private void removeExpiredKeys() {

        for (
                String key :
                expiryTimes.keySet()
        ) {

            if (isExpired(key)) {

                store.remove(key);

                expiryTimes.remove(key);
            }
        }
    }
}