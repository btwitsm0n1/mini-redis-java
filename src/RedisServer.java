import java.io.*;
import java.net.*;

public class RedisServer {

    private static final int PORT = 6379;

    public static void main(String[] args) {

        DataStore store = new DataStore();
        CommandParser parser = new CommandParser();
        CommandHandler handler = new CommandHandler(store);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("MiniRedis server started.");
            System.out.println("Listening on port: " + PORT);

            while (true) {

                System.out.println("Waiting for client...");

                Socket clientSocket = serverSocket.accept();

                System.out.println("Client connected: "
                        + clientSocket.getInetAddress());

                BufferedReader input = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );

                PrintWriter output = new PrintWriter(
                        clientSocket.getOutputStream(),
                        true
                );

                String command;

                while ((command = input.readLine()) != null) {

                    if (command.equalsIgnoreCase("EXIT")) {
                        output.println("Goodbye!");
                        break;
                    }

                    String[] parts = parser.parse(command);

                    String response = handler.execute(parts);

                    output.println(response);
                }

                clientSocket.close();

                System.out.println("Client disconnected.");
            }

        } catch (IOException e) {

            System.out.println("Server error: " + e.getMessage());
        }
    }
}