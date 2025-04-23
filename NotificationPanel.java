package project_Hospital_management;

import project_Hospital_management.NotificationClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NotificationPanel extends JPanel {
    private JTextArea notificationArea;
    private JTextField messageField;
    private NotificationClient notificationClient;

    public NotificationPanel() {
        setLayout(new BorderLayout());

        notificationArea = new JTextArea(10, 40);
        notificationArea.setEditable(false);

        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        JButton sendButton = new JButton("Send Notification");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText().trim();
                if (!message.isEmpty()) {
                    // In a real application, this would send to the server
                    notificationArea.append("Admin: " + message + "\n");
                    messageField.setText("");
                }
            }
        });

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(new JScrollPane(notificationArea), BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        // Connect to notification server
        notificationClient = new NotificationClient();
        notificationClient.connect("localhost", 5555, notificationArea);

        // Add shutdown hook to disconnect client
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            notificationClient.disconnect();
        }));
    }
}