public class CommandHandler {

    private final DataStore store;

    public CommandHandler(DataStore store) {
        this.store = store;
    }

    public String execute(String[] parts) {

        String command = parts[0].toUpperCase();

        if (command.equals("SET")) {
            store.set(parts[1], parts[2]);
            return "OK";
        }

        if (command.equals("GET")) {
            String value = store.get(parts[1]);

            if (value == null) {
                return "(nil)";
            }

            return value;
        }

        if (command.equals("DELETE")) {
            store.delete(parts[1]);
            return "OK";
        }

        if (command.equals("EXISTS")) {
            return String.valueOf(store.exists(parts[1]));
        }

        if (command.equals("EXPIRE")) {

            boolean result = store.expire(
                    parts[1],
                    Long.parseLong(parts[2])
            );

            return String.valueOf(result);
        }

        if (command.equals("TTL")) {

            return String.valueOf(
                    store.ttl(parts[1])
            );
        }

        return "Unknown command";
    }
}