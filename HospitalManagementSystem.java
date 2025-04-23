package project_Hospital_management;

import project_Hospital_management.DatabaseConnection;
import project_Hospital_management.NotificationServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class HospitalManagementSystem extends JFrame {
    private JTabbedPane tabbedPane;
    private JTextArea queryResultArea;
    private JTextField queryField;
    private JButton executeQueryButton;

    public HospitalManagementSystem() {
        setTitle("Hospital Management System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();

        // Start notification server in a separate thread
//        new Thread(() -> {
//            NotificationServer server = new NotificationServer(5555);
//            server.start();
//        }).start();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();

        // Add tabs for different functionalities
        tabbedPane.addTab("Patients", new PatientManagementPanel());
        tabbedPane.addTab("Doctors", new DoctorManagementPanel());
        tabbedPane.addTab("Appointments", new AppointmentManagementPanel());
        tabbedPane.addTab("Notifications", new NotificationPanel());

        // Add a new tab for executing queries
        JPanel queryPanel = new JPanel(new BorderLayout());
        queryField = new JTextField();
        executeQueryButton = new JButton("Execute Query");
        queryResultArea = new JTextArea();
        queryResultArea.setEditable(false);

        executeQueryButton.addActionListener(e -> executeQuery());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(queryField, BorderLayout.CENTER);
        inputPanel.add(executeQueryButton, BorderLayout.EAST);

        queryPanel.add(inputPanel, BorderLayout.NORTH);
        queryPanel.add(new JScrollPane(queryResultArea), BorderLayout.CENTER);

        tabbedPane.addTab("Execute Query", queryPanel);

        add(tabbedPane);
    }

    private void executeQuery() {
        String query = queryField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Query cannot be empty", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            StringBuilder result = new StringBuilder();
            int columnCount = rs.getMetaData().getColumnCount();

            // Append column names
            for (int i = 1; i <= columnCount; i++) {
                result.append(rs.getMetaData().getColumnName(i)).append("\t");
            }
            result.append("\n");

            // Append rows
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    result.append(rs.getString(i)).append("\t");
                }
                result.append("\n");
            }

            queryResultArea.setText(result.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error executing query: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Initialize database connection
        try {
            DatabaseConnection.getConnection();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Failed to connect to the database. Please check your database server and connection details.\nError: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            HospitalManagementSystem system = new HospitalManagementSystem();
            system.setVisible(true);
        });

        // Add shutdown hook to close database connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseConnection.closeConnection();
        }));
    }
}
