public class CommandHandler {

    private final DataStore store;

    public CommandHandler(DataStore store) {
        this.store = store;
    }

    public String execute(String[] parts) {

        if (parts == null || parts.length == 0) {
            return "ERR empty command";
        }

        String command = parts[0].toUpperCase();

        try {

            if (command.equals("SET")) {
                if (parts.length != 3) {
                    return "ERR usage: SET key value";
                }

                store.set(parts[1], parts[2]);
                return "OK";
            }

            if (command.equals("GET")) {
                if (parts.length != 2) {
                    return "ERR usage: GET key";
                }

                String value = store.get(parts[1]);

                if (value == null) {
                    return "(nil)";
                }

                return value;
            }

            if (command.equals("DELETE")) {
                if (parts.length != 2) {
                    return "ERR usage: DELETE key";
                }

                store.delete(parts[1]);
                return "OK";
            }

            if (command.equals("EXISTS")) {
                if (parts.length != 2) {
                    return "ERR usage: EXISTS key";
                }

                return String.valueOf(store.exists(parts[1]));
            }

            if (command.equals("EXPIRE")) {
                if (parts.length != 3) {
                    return "ERR usage: EXPIRE key seconds";
                }

                long seconds = Long.parseLong(parts[2]);

                if (seconds <= 0) {
                    return "ERR seconds must be greater than zero";
                }

                return String.valueOf(store.expire(parts[1], seconds));
            }

            if (command.equals("PERSIST")) {
                if (parts.length != 2) {
                    return "ERR usage: PERSIST key";
                }

                return String.valueOf(store.persist(parts[1]));
            }

            if (command.equals("TTL")) {
                if (parts.length != 2) {
                    return "ERR usage: TTL key";
                }

                return String.valueOf(store.ttl(parts[1]));
            }

            return "Unknown command";

        } catch (NumberFormatException e) {
            return "ERR seconds must be a valid number";
        }
    }
}