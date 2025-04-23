 package project_Hospital_management;

import project_Hospital_management.NotificationServer;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class NotificationClient {
    private Socket socket;
    private BufferedReader in;
    private boolean running;

    public void connect(String host, int port, JTextArea notificationArea) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            running = true;

            new Thread(() -> {
                try {
                    String message;
                    while (running && (message = in.readLine()) != null) {
                        String finalMessage = message;
                        SwingUtilities.invokeLater(() -> {
                            notificationArea.append(finalMessage + "\n");
                        });
                    }
                } catch (IOException e) {
                    if (running) {
                        SwingUtilities.invokeLater(() -> {
                            notificationArea.append("Connection error: " + e.getMessage() + "\n");
                        });
                    }
                } finally {
                    disconnect();
                }
            }).start();

        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                notificationArea.append("Failed to connect to server: " + e.getMessage() + "\n");
            });
        }
    }

    public void disconnect() {
        running = false;
        try {
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }
}