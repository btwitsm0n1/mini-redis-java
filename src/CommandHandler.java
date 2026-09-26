public class CommandHandler {

    private final DataStore store;

    public CommandHandler(DataStore store) {
        this.store = store;
    }

    public String execute(String[] parts) {

        if (parts == null || parts.length == 0) {
            return "ERR empty command";
        }

        String command =
                parts[0].toUpperCase();

        // ---------------- PING ----------------

        if (command.equals("PING")) {

            return "PONG";
        }

        // ---------------- SET ----------------

        if (command.equals("SET")) {

            if (parts.length != 3) {
                return "ERR wrong number of arguments for SET";
            }

            store.set(
                    parts[1],
                    parts[2]
            );

            return "OK";
        }

        // ---------------- GET ----------------

        if (command.equals("GET")) {

            if (parts.length != 2) {
                return "ERR wrong number of arguments for GET";
            }

            return store.get(parts[1]);
        }

        // ---------------- DELETE ----------------

        if (command.equals("DELETE")) {

            if (parts.length < 2) {
                return "ERR wrong number of arguments for DELETE";
            }

            String[] keys =
                    new String[parts.length - 1];

            System.arraycopy(
                    parts,
                    1,
                    keys,
                    0,
                    parts.length - 1
            );

            return String.valueOf(
                    store.delete(keys)
            );
        }

        // ---------------- EXISTS ----------------

        if (command.equals("EXISTS")) {

            if (parts.length != 2) {
                return "ERR wrong number of arguments for EXISTS";
            }

            return String.valueOf(
                    store.exists(parts[1])
            );
        }

        // ---------------- EXPIRE ----------------

        if (command.equals("EXPIRE")) {

            if (parts.length != 3) {
                return "ERR wrong number of arguments for EXPIRE";
            }

            long seconds;

            try {

                seconds =
                        Long.parseLong(parts[2]);

            } catch (NumberFormatException e) {

                return "ERR invalid number";
            }

            return String.valueOf(
                    store.expire(
                            parts[1],
                            seconds
                    )
            );
        }

        // ---------------- TTL ----------------

        if (command.equals("TTL")) {

            if (parts.length != 2) {
                return "ERR wrong number of arguments for TTL";
            }

            return String.valueOf(
                    store.ttl(parts[1])
            );
        }

        // ---------------- KEYS ----------------

        if (command.equals("KEYS")) {

            if (parts.length != 1) {
                return "ERR wrong number of arguments for KEYS";
            }

            return store.keys().toString();
        }

        // ---------------- INCR ----------------

        if (command.equals("INCR")) {

            if (parts.length != 2) {
                return "ERR wrong number of arguments for INCR";
            }

            try {

                return String.valueOf(
                        store.incr(parts[1])
                );

            } catch (IllegalArgumentException e) {

                return e.getMessage();
            }
        }

        // ---------------- DECR ----------------

        if (command.equals("DECR")) {

            if (parts.length != 2) {
                return "ERR wrong number of arguments for DECR";
            }

            try {

                return String.valueOf(
                        store.decr(parts[1])
                );

            } catch (IllegalArgumentException e) {

                return e.getMessage();
            }
        }

        // ---------------- INCRBY ----------------

        if (command.equals("INCRBY")) {

            if (parts.length != 3) {
                return "ERR wrong number of arguments for INCRBY";
            }

            long amount;

            try {

                amount =
                        Long.parseLong(parts[2]);

            } catch (NumberFormatException e) {

                return "ERR invalid number";
            }

            try {

                return String.valueOf(
                        store.incrBy(
                                parts[1],
                                amount
                        )
                );

            } catch (IllegalArgumentException e) {

                return e.getMessage();
            }
        }

        // ---------------- DECRBY ----------------

        if (command.equals("DECRBY")) {

            if (parts.length != 3) {
                return "ERR wrong number of arguments for DECRBY";
            }

            long amount;

            try {

                amount =
                        Long.parseLong(parts[2]);

            } catch (NumberFormatException e) {

                return "ERR invalid number";
            }

            try {

                return String.valueOf(
                        store.decrBy(
                                parts[1],
                                amount
                        )
                );

            } catch (IllegalArgumentException e) {

                return e.getMessage();
            }
        }

        // ---------------- APPEND ----------------

        if (command.equals("APPEND")) {

            if (parts.length != 3) {
                return "ERR wrong number of arguments for APPEND";
            }

            return String.valueOf(
                    store.append(
                            parts[1],
                            parts[2]
                    )
            );
        }

        // ---------------- MSET ----------------

        if (command.equals("MSET")) {

            if (parts.length < 3 ||
                    parts.length % 2 == 0) {

                return "ERR wrong number of arguments for MSET";
            }

            String[] pairs =
                    new String[parts.length - 1];

            System.arraycopy(
                    parts,
                    1,
                    pairs,
                    0,
                    parts.length - 1
            );

            store.mset(pairs);

            return "OK";
        }

        // ---------------- MGET ----------------

        if (command.equals("MGET")) {

            if (parts.length < 2) {
                return "ERR wrong number of arguments for MGET";
            }

            String[] keys =
                    new String[parts.length - 1];

            System.arraycopy(
                    parts,
                    1,
                    keys,
                    0,
                    parts.length - 1
            );

            return store.mget(keys);
        }

        // ---------------- SETNX ----------------

        if (command.equals("SETNX")) {

            if (parts.length != 3) {
                return "ERR wrong number of arguments for SETNX";
            }

            return String.valueOf(
                    store.setNx(
                            parts[1],
                            parts[2]
                    ) ? 1 : 0
            );
        }

        // ---------------- GETSET ----------------

        if (command.equals("GETSET")) {

            if (parts.length != 3) {
                return "ERR wrong number of arguments for GETSET";
            }

            return store.getSet(
                    parts[1],
                    parts[2]
            );
        }

        // ---------------- LPUSH ----------------

        if (command.equals("LPUSH")) {

            if (parts.length < 3) {
                return "ERR wrong number of arguments for LPUSH";
            }

            String key = parts[1];

            int added = 0;

            for (int i = 2; i < parts.length; i++) {

                added =
                        store.lpush(
                                key,
                                parts[i]
                        );
            }

            return String.valueOf(added);
        }

        // ---------------- FLUSHALL ----------------

        if (command.equals("FLUSHALL")) {

            if (parts.length != 1) {
                return "ERR wrong number of arguments for FLUSHALL";
            }

            store.flushAll();

            return "OK";
        }

        // ---------------- UNKNOWN ----------------

        return "ERR unknown command";
    }
}