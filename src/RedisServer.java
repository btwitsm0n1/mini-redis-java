import java.io.*;
import java.net.*;

public class RedisServer {

    private static final int PORT = 6379;

    public static void main(String[] args) {

        DataStore store = new DataStore();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("MiniRedis server started.");
            System.out.println("Listening on port: " + PORT);

            while (true) {

                System.out.println("Waiting for client...");

                Socket clientSocket = serverSocket.accept();

                System.out.println(
                    "New client connected: "
                    + clientSocket.getInetAddress()
                );

                ClientHandler clientHandler =
                    new ClientHandler(clientSocket, store);

                Thread clientThread = new Thread(clientHandler);

                clientThread.start();
            }

        } catch (IOException e) {

            System.out.println("Server error: " + e.getMessage());
        }
    }
}