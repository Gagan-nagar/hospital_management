package project_Hospital_management;

import project_Hospital_management.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class DoctorManagementPanel extends JPanel {
    private JTable doctorTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, specializationField, contactField, fromField, toField;

    public DoctorManagementPanel() {
        setLayout(new BorderLayout());

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Doctor Information"));

        nameField = new JTextField();
        specializationField = new JTextField();
        contactField = new JTextField();
        fromField = new JTextField();
        toField = new JTextField();

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Specialization:"));
        formPanel.add(specializationField);
        formPanel.add(new JLabel("Contact:"));
        formPanel.add(contactField);
        formPanel.add(new JLabel("Available From (HH:MM):"));
        formPanel.add(fromField);
        formPanel.add(new JLabel("Available To (HH:MM):"));
        formPanel.add(toField);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Doctor");
        JButton updateButton = new JButton("Update Doctor");
        JButton deleteButton = new JButton("Delete Doctor");

        addButton.addActionListener(e -> addDoctor());
        updateButton.addActionListener(e -> updateDoctor());
        deleteButton.addActionListener(e -> deleteDoctor());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // Table
        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Name");
        tableModel.addColumn("Specialization");
        tableModel.addColumn("Contact");
        tableModel.addColumn("Available From");
        tableModel.addColumn("Available To");

        doctorTable = new JTable(tableModel);
        doctorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add components to main panel
        add(formPanel, BorderLayout.NORTH);
        add(new JScrollPane(doctorTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load doctor data
        loadDoctorData();

        // Add selection listener to populate form when a row is selected
        doctorTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = doctorTable.getSelectedRow();
                if (selectedRow >= 0) {
                    nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    specializationField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    contactField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                    fromField.setText(tableModel.getValueAt(selectedRow, 4).toString());
                    toField.setText(tableModel.getValueAt(selectedRow, 5).toString());
                }
            }
        });
    }

    private void loadDoctorData() {
        tableModel.setRowCount(0); // Clear existing data

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM doctors")) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("doctor_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("specialization"));
                row.add(rs.getString("contact"));
                row.add(rs.getTime("available_from").toString());
                row.add(rs.getTime("available_to").toString());
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading doctor data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addDoctor() {
        String name = nameField.getText().trim();
        String specialization = specializationField.getText().trim();
        String contact = contactField.getText().trim();
        String from = fromField.getText().trim();
        String to = toField.getText().trim();

        if (name.isEmpty() || specialization.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Specialization are required fields",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Time availableFrom = Time.valueOf(from + ":00");
            Time availableTo = Time.valueOf(to + ":00");

            String sql = "INSERT INTO doctors (name, specialization, contact, available_from, available_to) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, name);
                pstmt.setString(2, specialization);
                pstmt.setString(3, contact);
                pstmt.setTime(4, availableFrom);
                pstmt.setTime(5, availableTo);

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int id = generatedKeys.getInt(1);
                            JOptionPane.showMessageDialog(this, "Doctor added successfully with ID: " + id,
                                    "Success", JOptionPane.INFORMATION_MESSAGE);
                            loadDoctorData();
                            clearForm();
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Invalid time format. Please use HH:MM format",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding doctor: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDoctor() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to update",
                    "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int doctorId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = nameField.getText().trim();
        String specialization = specializationField.getText().trim();
        String contact = contactField.getText().trim();
        String from = fromField.getText().trim();
        String to = toField.getText().trim();

        if (name.isEmpty() || specialization.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Specialization are required fields",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Time availableFrom = Time.valueOf(from + ":00");
            Time availableTo = Time.valueOf(to + ":00");

            String sql = "UPDATE doctors SET name=?, specialization=?, contact=?, available_from=?, available_to=? " +
                    "WHERE doctor_id=?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, name);
                pstmt.setString(2, specialization);
                pstmt.setString(3, contact);
                pstmt.setTime(4, availableFrom);
                pstmt.setTime(5, availableTo);
                pstmt.setInt(6, doctorId);

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Doctor updated successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadDoctorData();
                }
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Invalid time format. Please use HH:MM format",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating doctor: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDoctor() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to delete",
                    "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int doctorId = (int) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this doctor?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String sql = "DELETE FROM doctors WHERE doctor_id=?";

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setInt(1, doctorId);

                    int affectedRows = pstmt.executeUpdate();

                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this, "Doctor deleted successfully",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadDoctorData();
                        clearForm();
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting doctor: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        nameField.setText("");
        specializationField.setText("");
        contactField.setText("");
        fromField.setText("");
        toField.setText("");
        doctorTable.clearSelection();
    }
}
