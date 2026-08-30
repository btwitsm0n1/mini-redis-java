import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private DataStore store;

    public ClientHandler(Socket clientSocket, DataStore store) {
        this.clientSocket = clientSocket;
        this.store = store;
    }

    @Override
    public void run() {

        CommandParser parser = new CommandParser();
        CommandHandler handler = new CommandHandler(store);

        try (
            BufferedReader input = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())
            );

            PrintWriter output = new PrintWriter(
                clientSocket.getOutputStream(),
                true
            )
        ) {

            System.out.println(
                "Handling client: " + clientSocket.getInetAddress()
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

        } catch (IOException e) {

            System.out.println(
                "Client connection error: " + e.getMessage()
            );

        } finally {

            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket.");
            }

            System.out.println("Client disconnected.");
        }
    }
}