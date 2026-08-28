import java.util.HashMap;
import java.util.Map;

public class DataStore {

    private final Map<String, String> data;

    public DataStore() {
        data = new HashMap<>();
    }

    public void set(String key, String value) {
        data.put(key, value);
    }

    public String get(String key) {
        return data.get(key);
    }

    public void delete(String key) {
        data.remove(key);
    }

    public boolean exists(String key) {
        return data.containsKey(key);
    }
}