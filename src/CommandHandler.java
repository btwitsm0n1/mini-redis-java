public class CommandHandler {

    private final DataStore store;

    public CommandHandler(DataStore store) {
        this.store = store;
    }

    public String execute(String[] parts) {

        if (parts.length == 0) {
            return "ERR empty command";
        }

        String command = parts[0].toUpperCase();

        try {

            // PING
            if (command.equals("PING")) {
                return "PONG";
            }

            // SET
            if (command.equals("SET")) {

                if (parts.length < 3) {
                    return "ERR wrong number of arguments for SET";
                }

                store.set(parts[1], parts[2]);
                return "OK";
            }

            // GET
            if (command.equals("GET")) {

                if (parts.length < 2) {
                    return "ERR wrong number of arguments for GET";
                }

                String value = store.get(parts[1]);

                if (value == null) {
                    return "(nil)";
                }

                return value;
            }

            // DELETE
            if (command.equals("DELETE")) {

                if (parts.length < 2) {
                    return "ERR wrong number of arguments for DELETE";
                }

                store.delete(parts[1]);
                return "OK";
            }

            // EXISTS
            if (command.equals("EXISTS")) {

                if (parts.length < 2) {
                    return "ERR wrong number of arguments for EXISTS";
                }

                return String.valueOf(
                        store.exists(parts[1])
                );
            }

            // EXPIRE
            if (command.equals("EXPIRE")) {

                if (parts.length < 3) {
                    return "ERR wrong number of arguments for EXPIRE";
                }

                long seconds =
                        Long.parseLong(parts[2]);

                boolean result =
                        store.expire(parts[1], seconds);

                return String.valueOf(result);
            }

            // TTL
            if (command.equals("TTL")) {

                if (parts.length < 2) {
                    return "ERR wrong number of arguments for TTL";
                }

                return String.valueOf(
                        store.ttl(parts[1])
                );
            }

            // KEYS
            if (command.equals("KEYS")) {

                if (parts.length > 1) {
                    return "ERR wrong number of arguments for KEYS";
                }

                return store.keys().toString();
            }

            // INCR
            if (command.equals("INCR")) {

                if (parts.length < 2) {
                    return "ERR wrong number of arguments for INCR";
                }

                return String.valueOf(
                        store.incr(parts[1])
                );
            }

            // DECR
            if (command.equals("DECR")) {

                if (parts.length < 2) {
                    return "ERR wrong number of arguments for DECR";
                }

                return String.valueOf(
                        store.decr(parts[1])
                );
            }

            // FLUSHALL
            if (command.equals("FLUSHALL")) {

                if (parts.length > 1) {
                    return "ERR wrong number of arguments for FLUSHALL";
                }

                store.flushAll();

                return "OK";
            }

            // Unknown command
            return "ERR unknown command";

        } catch (NumberFormatException e) {

            return "ERR invalid number";

        } catch (Exception e) {

            return "ERR " + e.getMessage();
        }
    }
}