public class Main {

    public static void main(String[] args) {

        DataStore store = new DataStore();

        CommandParser parser = new CommandParser();

        CommandHandler handler = new CommandHandler(store);


        // SET
        String input = "SET name Moni";

        String[] parts = parser.parse(input);

        String response = handler.execute(parts);

        System.out.println(response);


        // GET
        input = "GET name";

        parts = parser.parse(input);

        response = handler.execute(parts);

        System.out.println(response);


        // EXISTS
        input = "EXISTS name";

        parts = parser.parse(input);

        response = handler.execute(parts);

        System.out.println(response);


        // DELETE
        input = "DELETE name";

        parts = parser.parse(input);

        response = handler.execute(parts);

        System.out.println(response);


        // GET after DELETE
        input = "GET name";

        parts = parser.parse(input);

        response = handler.execute(parts);

        System.out.println(response);
    }
}