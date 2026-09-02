import java.util.Map;
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

    public void set(String key, String value) {

        store.put(key, value);

        // New SET removes any previous expiry
        expiryTimes.remove(key);
    }

    public String get(String key) {

        if (isExpired(key)) {
            delete(key);
            return null;
        }

        return store.get(key);
    }

    public void delete(String key) {

        store.remove(key);
        expiryTimes.remove(key);
    }

    public boolean exists(String key) {

        if (isExpired(key)) {
            delete(key);
            return false;
        }

        return store.containsKey(key);
    }

    public boolean expire(String key, long seconds) {

        if (!store.containsKey(key)) {
            return false;
        }

        long expiryTime =
                System.currentTimeMillis() + (seconds * 1000);

        expiryTimes.put(key, expiryTime);

        return true;
    }

    private boolean isExpired(String key) {

        Long expiryTime = expiryTimes.get(key);

        if (expiryTime == null) {
            return false;
        }

        return System.currentTimeMillis() >= expiryTime;
    }

    private void removeExpiredKeys() {

        long currentTime = System.currentTimeMillis();

        for (Map.Entry<String, Long> entry : expiryTimes.entrySet()) {

            String key = entry.getKey();
            long expiryTime = entry.getValue();

            if (currentTime >= expiryTime) {
                store.remove(key);
                expiryTimes.remove(key);
            }
        }
    }
}