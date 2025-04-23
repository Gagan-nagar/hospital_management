package project_Hospital_management;

import project_Hospital_management.ClientHandler;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationServer {
    private final int port;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>(); // Thread-safe list
    private boolean running;

    public NotificationServer(int port) {
        this.port = port;
    }

    public void start() {
        running = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Notification server started on port " + port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket);

                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            stop(); // Ensure server stops cleanly
        }
    }

    public void stop() {
        running = false;
        for (ClientHandler client : clients) {
            client.stop();
        }
        clients.clear(); // Clear the list after stopping all clients
        System.out.println("Notification server stopped.");
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static void main(String[] args) {
        try {
            DatabaseConnection.getConnection();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to connect to the database. Please check your database server and connection details.\nError: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        int port = 5555; // Default port
        NotificationServer server = new NotificationServer(port);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop)); // Ensure server stops on shutdown
        server.start();
    }
}
