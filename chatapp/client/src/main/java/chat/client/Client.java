package chat.client;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.Scanner;

public class Client {
    private static final String SETTINGS_FILE = "settings.txt";
    private String logFilePath = "client-logs.log";
    private String serverAddress;
    private int port;
    private String name;

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        loadSettings();
        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your name: ");
            name = scanner.nextLine();
            out.println(name);

            Thread messageReader = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                        log(serverMessage);
                    }
                } catch (IOException e) {
                    if (!socket.isClosed()) {
                        log("Error reading from server: " + e.getMessage());
                    }
                }
            });
            messageReader.start();

            String userInput;
            while (true) {
                userInput = scanner.nextLine();
                if ("/exit".equalsIgnoreCase(userInput)) {
                    out.println(userInput);
                    System.out.println("You left chat");
                    log(name + " left chat");
                    break;
                }
                out.println(userInput);
                log(name + ": " + userInput);
            }

            Thread.sleep(500);
        } catch (IOException | InterruptedException e) {
            log("Connection error: " + e.getMessage());
        }
    }

    private void loadSettings() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SETTINGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("port=")) {
                    port = Integer.parseInt(line.split("=")[1].trim());
                } else if (line.startsWith("address=")) {
                    serverAddress = line.split("=")[1].trim();
                }
            }
        } catch (IOException e) {
            port = 8080;
            serverAddress = "localhost";
            log("Using default settings");
        }
    }

    public void log(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFilePath, true))) {
            writer.println(LocalDateTime.now() + " " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLogFilePath(String path) {
        this.logFilePath = path;
    }

}