package chitFund.gui;

import chitFund.util.DatabaseConnector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Random;

public class ManageChitGroups extends JFrame {
    private JButton addButton;
    private JTable chitGroupTable;
    private DefaultTableModel tableModel;

    public ManageChitGroups() {
        initializeComponents();
        loadChitGroupsFromDatabase();
    }

    private void initializeComponents() {
        setTitle("Manage Chit Groups");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout());

        // Table to display chit groups
        tableModel = new DefaultTableModel();
        chitGroupTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make all cells non-editable
                return false;
            }
        };
        JScrollPane scrollPane = new JScrollPane(chitGroupTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button to add new chit group
        addButton = new JButton("Add Chit Group");
        addButton.setFont(new Font("Arial", Font.PLAIN, 14));
        addButton.setPreferredSize(new Dimension(150, 40));
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle adding new chit group
                addChitGroup();
            }
        });
        panel.add(addButton, BorderLayout.SOUTH);

        add(panel);

        // Add ListSelectionListener to the table
        chitGroupTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = chitGroupTable.getSelectedRow();
                if (selectedRow != -1) {
                    String groupId = (String) tableModel.getValueAt(selectedRow, 0);
                    // Call the displayCustomerIDs method to show customer IDs in the selected chit group
                    displayCustomerDetails(groupId);
                }
            }
        });
    }

    private void loadChitGroupsFromDatabase() {
        // Clear existing rows from the table
        tableModel.setRowCount(0);

        tableModel.setColumnIdentifiers(new String[]{"Group ID", "Name", "Scheme Amount", "Duration", "Monthly Due", "Available Slots"});

        try (Connection connection = DatabaseConnector.connect()) {
            if (connection != null) {
                String query = "SELECT * FROM chit_groups ORDER BY scheme_amount";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        String groupId = resultSet.getString("id"); // Change to String for Group ID
                        String name = resultSet.getString("name");
                        double schemeAmount = resultSet.getDouble("scheme_amount");
                        int duration = resultSet.getInt("duration");
                        double monthlyDue = resultSet.getDouble("monthly_due");
                        int totalMembers = resultSet.getInt("total_members");
                        int availableSlots = resultSet.getInt("available_slots"); // Retrieve available slots from the database

                        tableModel.addRow(new Object[]{groupId, name, schemeAmount, duration, monthlyDue, availableSlots});
                    }
                }
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void addChitGroup() {
        // Create a dialog for adding a new chit group
        JFrame addDialog = new JFrame("Add Chit Group");
        addDialog.setSize(400, 250);
        addDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addDialog.setLocationRelativeTo(null); // Center the dialog on the screen

        JPanel panel = new JPanel(new GridLayout(6, 2));

        // Components for input fields
        JTextField nameField = new JTextField();
        JTextField schemeAmountField = new JTextField();
        JTextField durationField = new JTextField();
        JTextField monthlyDueField = new JTextField();
        JTextField totalMembersField = new JTextField();

        // Labels for input fields
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Scheme Amount:"));
        panel.add(schemeAmountField);
        panel.add(new JLabel("Tenure (months):"));
        panel.add(durationField);
        panel.add(new JLabel("Monthly Due:"));
        panel.add(monthlyDueField);
        panel.add(new JLabel("Total Members:"));
        panel.add(totalMembersField);

        // Button to submit the form
        JButton submitButton = new JButton("Add");
        submitButton.addActionListener(e -> {
            // Get values from input fields
            String name = nameField.getText();
            String schemeAmountText = schemeAmountField.getText();
            String durationText = durationField.getText();
            String monthlyDueText = monthlyDueField.getText();
            String totalMembersText = totalMembersField.getText();

            // Check if any of the fields are empty
            if (name.isEmpty() || schemeAmountText.isEmpty() || durationText.isEmpty() || monthlyDueText.isEmpty() || totalMembersText.isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Parse numerical values
            double schemeAmount;
            int duration;
            double monthlyDue;
            int totalMembers;
            try {
                schemeAmount = Double.parseDouble(schemeAmountText);
                duration = Integer.parseInt(durationText);
                monthlyDue = Double.parseDouble(monthlyDueText);
                totalMembers = Integer.parseInt(totalMembersText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addDialog, "Invalid numerical format.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Call method to add the chit group to the database
            addChitGroupToDatabase(name, schemeAmount, duration, monthlyDue, totalMembers);

            // Close the dialog
            addDialog.dispose();

            // Refresh the table to show the newly added chit group
            loadChitGroupsFromDatabase();
        });

        panel.add(new JLabel());
        panel.add(submitButton);

        addDialog.add(panel);
        addDialog.setVisible(true);
    }

    private void addChitGroupToDatabase(String name, double schemeAmount, int duration, double monthlyDue, int totalMembers) {
        try (Connection connection = DatabaseConnector.connect()) {
            if (connection != null) {
                String groupId = generateChitGroupId(); // Generate Group ID
                String query = "INSERT INTO chit_groups (id, name, scheme_amount, duration, monthly_due, total_members, available_slots) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, groupId);
                    preparedStatement.setString(2, name);
                    preparedStatement.setDouble(3, schemeAmount);
                    preparedStatement.setInt(4, duration);
                    preparedStatement.setDouble(5, monthlyDue);
                    preparedStatement.setInt(6, totalMembers);
                    preparedStatement.setInt(7, totalMembers); // Set available slots initially equal to total members
                    preparedStatement.executeUpdate();
                }
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Method to generate a random alphanumeric ID of length 6 characters
    private String generateChitGroupId() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 6; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    private void displayCustomerDetails(String groupId) {
        JFrame customerDetailsDialog = new JFrame("Customer Details in Chit Group " + groupId);
        customerDetailsDialog.setSize(800, 400);
        customerDetailsDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        customerDetailsDialog.setLocationRelativeTo(null); // Center the dialog on the screen

        JPanel panel = new JPanel(new BorderLayout());

        // Table to display customer details
        DefaultTableModel customerTableModel = new DefaultTableModel();
        JTable customerTable = new JTable(customerTableModel);
        JScrollPane scrollPane = new JScrollPane(customerTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        customerTableModel.setColumnIdentifiers(new String[]{"Customer ID", "Name", "Address", "Phone No", "Email"});

        try (Connection connection = DatabaseConnector.connect()) {
            if (connection != null) {
                String query = "SELECT c.customer_id, c.customer_name, c.address, c.phone_num, c.email " +
                        "FROM customers c " +
                        "INNER JOIN customer_chit_groups ccg ON c.customer_id = ccg.customer_id " +
                        "WHERE ccg.chit_group_id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, groupId);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            String customerId = resultSet.getString("customer_id");
                            String customerName = resultSet.getString("customer_name");
                            String address = resultSet.getString("address");
                            String phoneNum = resultSet.getString("phone_num");
                            String email = resultSet.getString("email");
                            customerTableModel.addRow(new Object[]{customerId, customerName, address, phoneNum, email});
                        }
                    }
                }
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        customerDetailsDialog.add(panel);
        customerDetailsDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManageChitGroups().setVisible(true));
    }
}
