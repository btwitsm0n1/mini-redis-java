import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        DataStore store = new DataStore();

        CommandParser parser = new CommandParser();

        CommandHandler handler = new CommandHandler(store);

        Scanner scanner = new Scanner(System.in);

        System.out.println("MiniRedis started!");
        System.out.println("Type 'EXIT' to stop the server.");

        while (true) {

            System.out.print("MiniRedis> ");

            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("EXIT")) {
                break;
            }

            String[] parts = parser.parse(input);

            String response = handler.execute(parts);

            System.out.println(response);
        }

        scanner.close();

        System.out.println("MiniRedis stopped.");
    }
}