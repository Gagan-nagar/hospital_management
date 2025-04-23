package project_Hospital_management;
// AppointmentManagementPanel.java

import project_Hospital_management.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class AppointmentManagementPanel extends JPanel {
    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> patientCombo, doctorCombo;
    private JTextField dateField, timeField;
    private JButton scheduleButton, cancelButton;

    public AppointmentManagementPanel() {
        setLayout(new BorderLayout());

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Appointment Information"));

        patientCombo = new JComboBox<>();
        doctorCombo = new JComboBox<>();
        dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        timeField = new JTextField(new SimpleDateFormat("HH:mm").format(new Date()));

        loadPatients();
        loadDoctors();

        formPanel.add(new JLabel("Patient:"));
        formPanel.add(patientCombo);
        formPanel.add(new JLabel("Doctor:"));
        formPanel.add(doctorCombo);
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        formPanel.add(dateField);
        formPanel.add(new JLabel("Time (HH:MM):"));
        formPanel.add(timeField);

        // Button panel
        JPanel buttonPanel = new JPanel();
        scheduleButton = new JButton("Schedule Appointment");
        cancelButton = new JButton("Cancel Appointment");

        scheduleButton.addActionListener(e -> scheduleAppointment());
        cancelButton.addActionListener(e -> cancelAppointment());

        buttonPanel.add(scheduleButton);
        buttonPanel.add(cancelButton);

        // Table
        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Patient");
        tableModel.addColumn("Doctor");
        tableModel.addColumn("Date");
        tableModel.addColumn("Time");
        tableModel.addColumn("Status");

        appointmentTable = new JTable(tableModel);
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add components to main panel
        add(formPanel, BorderLayout.NORTH);
        add(new JScrollPane(appointmentTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load appointment data
        loadAppointmentData();
    }

    private void loadPatients() {
        patientCombo.removeAllItems();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT patient_id, name FROM patients ORDER BY name")) {

            while (rs.next()) {
                patientCombo.addItem(rs.getInt("patient_id") + " - " + rs.getString("name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDoctors() {
        doctorCombo.removeAllItems();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT doctor_id, name FROM doctors ORDER BY name")) {

            while (rs.next()) {
                doctorCombo.addItem(rs.getInt("doctor_id") + " - " + rs.getString("name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading doctors: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAppointmentData() {
        tableModel.setRowCount(0); // Clear existing data

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT a.appointment_id, p.name AS patient_name, d.name AS doctor_name, " +
                             "a.appointment_date, a.appointment_time, a.status " +
                             "FROM appointments a " +
                             "JOIN patients p ON a.patient_id = p.patient_id " +
                             "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                             "ORDER BY a.appointment_date, a.appointment_time")) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("appointment_id"));
                row.add(rs.getString("patient_name"));
                row.add(rs.getString("doctor_name"));
                row.add(rs.getDate("appointment_date"));
                row.add(rs.getTime("appointment_time"));
                row.add(rs.getString("status"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading appointments: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void scheduleAppointment() {
        String patientSelection = (String) patientCombo.getSelectedItem();
        String doctorSelection = (String) doctorCombo.getSelectedItem();
        String date = dateField.getText().trim();
        String time = timeField.getText().trim();

        if (patientSelection == null || doctorSelection == null || date.isEmpty() || time.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int patientId = Integer.parseInt(patientSelection.split(" - ")[0]);
            int doctorId = Integer.parseInt(doctorSelection.split(" - ")[0]);
            Date appointmentDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            Time appointmentTime = Time.valueOf(time + ":00");

            // Check if doctor is available at that time
            if (!isDoctorAvailable(doctorId, appointmentDate, appointmentTime)) {
                JOptionPane.showMessageDialog(this, "Doctor is not available at that time",
                        "Scheduling Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time) " +
                    "VALUES (?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setInt(1, patientId);
                pstmt.setInt(2, doctorId);
                pstmt.setDate(3, new java.sql.Date(appointmentDate.getTime()));
                pstmt.setTime(4, appointmentTime);

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int id = generatedKeys.getInt(1);
                            JOptionPane.showMessageDialog(this, "Appointment scheduled successfully with ID: " + id,
                                    "Success", JOptionPane.INFORMATION_MESSAGE);
                            loadAppointmentData();
                        }
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error scheduling appointment: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isDoctorAvailable(int doctorId, Date date, Time time) throws SQLException {
        String sql = "SELECT available_from, available_to FROM doctors WHERE doctor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, doctorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Time availableFrom = rs.getTime("available_from");
                    Time availableTo = rs.getTime("available_to");

                    // Check if requested time is within doctor's available hours
                    return !time.before(availableFrom) && !time.after(availableTo);
                }
            }
        }
        return false;
    }

    private void cancelAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel",
                    "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int appointmentId = (int) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this appointment?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String sql = "UPDATE appointments SET status='Cancelled' WHERE appointment_id=?";

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setInt(1, appointmentId);

                    int affectedRows = pstmt.executeUpdate();

                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this, "Appointment cancelled successfully",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadAppointmentData();
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error cancelling appointment: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
