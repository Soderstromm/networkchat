package chat.server;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final String SETTINGS_FILE = "settings.txt";
    private static final String LOG_FILE = "server-logs.log";
    private int port;
    private List<ClientHandler> clients = new ArrayList<>();
    private ConcurrentHashMap<String, PrintWriter> writers = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        loadSettings();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            log("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            log("Server error: " + e.getMessage());
        }
    }

    private void loadSettings() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SETTINGS_FILE))) {
            port = Integer.parseInt(reader.readLine().split("=")[1].trim());
        } catch (IOException e) {
            port = 8080;
            log("Using default port 8080");
        }
    }

    public synchronized void log(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String logMessage = LocalDateTime.now() + " [SERVER] " + message;
            writer.println(logMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private String name;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                name = in.readLine();
                writers.put(name, out);
                clients.add(this);
                broadcast(name + " joined the chat");

                String message;
                while ((message = in.readLine()) != null) {
                    if ("/exit".equalsIgnoreCase(message)) break;
                    broadcast(name + ": " + message);
                }
            } catch (IOException e) {
                log("Client error: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Error closing socket: " + e.getMessage());
                }
                clients.remove(this);
                writers.remove(name);
                broadcast(name + " left the chat");
            }
        }

        private void broadcast(String message) {
            log(message);
            for (PrintWriter writer : writers.values()) {
                writer.println(message);
            }
        }
    }
}
