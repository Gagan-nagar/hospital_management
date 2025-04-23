package project_Hospital_management;
// ClientHandler.java
import project_Hospital_management.NotificationServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final NotificationServer server;
    private PrintWriter out;
    private boolean running;

    public ClientHandler(Socket socket, NotificationServer server) {
        this.clientSocket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        running = true;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while (running && (inputLine = in.readLine()) != null) {
                System.out.println("Received from client: " + inputLine);
                // Process client messages if needed
            }
        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void stop() {
        running = false;
        try {
            if (out != null) out.close();
            if (!clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
        server.removeClient(this);
        System.out.println("Client disconnected: " + clientSocket);
    }
}
