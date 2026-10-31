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

            // PING
            if (command.equals("PING")) {
                if (parts.length != 1) {
                    return "ERR wrong number of arguments for PING";
                }
                return "PONG";
            }

            // INFO
            if (command.equals("INFO")) {
                if (parts.length != 1) {
                    return "ERR wrong number of arguments for INFO";
                }

                return "MiniRedis Server\n"
                        + "Version: 1.0\n"
                        + "Total keys: " + store.size() + "\n"
                        + "Total lists: " + store.listSize();
            }

            // SET
            if (command.equals("SET")) {
                if (parts.length != 3) {
                    return "ERR wrong number of arguments for SET";
                }

                store.set(parts[1], parts[2]);
                return "OK";
            }

            // GET
            if (command.equals("GET")) {
                if (parts.length != 2) {
                    return "ERR wrong number of arguments for GET";
                }

                return store.get(parts[1]);
            }

            // DELETE
            if (command.equals("DELETE")) {
                if (parts.length < 2) {
                    return "ERR wrong number of arguments for DELETE";
                }

                return String.valueOf(store.delete(
                        java.util.Arrays.copyOfRange(parts, 1, parts.length)
                ));
            }

            // EXISTS
            if (command.equals("EXISTS")) {
                if (parts.length != 2) {
                    return "ERR wrong number of arguments for EXISTS";
                }

                return String.valueOf(store.exists(parts[1]));
            }

            // EXPIRE
            if (command.equals("EXPIRE")) {
                if (parts.length != 3) {
                    return "ERR wrong number of arguments for EXPIRE";
                }

                long seconds = Long.parseLong(parts[2]);

                if (seconds < 0) {
                    return "ERR expiry must not be negative";
                }

                return String.valueOf(store.expire(parts[1], seconds));
            }

            // TTL
            if (command.equals("TTL")) {
                if (parts.length != 2) {
                    return "ERR wrong number of arguments for TTL";
                }

                return String.valueOf(store.ttl(parts[1]));
            }

            // KEYS
            if (command.equals("KEYS")) {
                if (parts.length != 1) {
                    return "ERR wrong number of arguments for KEYS";
                }

                return store.keys().toString();
            }

            // FLUSHALL
            if (command.equals("FLUSHALL")) {
                if (parts.length != 1) {
                    return "ERR wrong number of arguments for FLUSHALL";
                }

                store.flushAll();
                return "OK";
            }

            return "ERR unknown command";

        } catch (NumberFormatException e) {
            return "ERR invalid number";

        } catch (Exception e) {
            return "ERR " + e.getMessage();
        }
    }
}
