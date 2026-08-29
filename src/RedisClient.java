import java.io.*;
import java.net.*;
import java.util.Scanner;

public class RedisClient {

    private static final String HOST = "localhost";
    private static final int PORT = 6379;

    public static void main(String[] args) {

        try (
            Socket socket = new Socket(HOST, PORT);

            BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );

            PrintWriter output = new PrintWriter(
                socket.getOutputStream(),
                true
            );

            Scanner scanner = new Scanner(System.in)
        ) {

            System.out.println("Connected to MiniRedis server.");
            System.out.println("Type EXIT to disconnect.");

            while (true) {

                System.out.print("MiniRedis> ");

                String command = scanner.nextLine();

                output.println(command);

                String response = input.readLine();

                System.out.println(response);

                if (command.equalsIgnoreCase("EXIT")) {
                    break;
                }
            }

        } catch (IOException e) {

            System.out.println("Could not connect to MiniRedis server.");
            System.out.println("Error: " + e.getMessage());
        }
    }
}