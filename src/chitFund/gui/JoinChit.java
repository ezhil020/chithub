package chitFund.gui;

import chitFund.util.DatabaseConnector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JoinChit extends JFrame {
    private String customerId;
    private JTable chitGroupTable;
    private DefaultTableModel tableModel;

    public JoinChit(String customerId) {
        this.customerId = customerId;
        initializeComponents();
        loadChitGroupsFromDatabase();
    }

    private void initializeComponents() {
        setTitle("Join Chit Group");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        // Table to display chit groups
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make all cells non-editable
                return false;
            }
        };
        chitGroupTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(chitGroupTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton viewChitButton = new JButton("View My Chit Groups");
        viewChitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ViewChit viewChit = new ViewChit(customerId);
                viewChit.setVisible(true);
            }
        });
        panel.add(viewChitButton, BorderLayout.SOUTH);

        // Add ListSelectionListener to the table
        chitGroupTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = chitGroupTable.getSelectedRow();
                    if (selectedRow != -1) {
                        String groupId = (String) tableModel.getValueAt(selectedRow, 0);
                        String groupName = (String) tableModel.getValueAt(selectedRow, 1);
                        showConfirmationDialog(groupId, groupName);
                    }
                }
            }
        });

        add(panel);
    }

    private void loadChitGroupsFromDatabase() {
        // Clear existing rows from the table
        tableModel.setRowCount(0);

        tableModel.setColumnIdentifiers(new String[]{"Group ID", "Name", "Scheme Amount", "Duration", "Monthly Due", "Total Members", "Available Slots"});

        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "SELECT id, name, scheme_amount, duration, monthly_due, total_members, available_slots FROM chit_groups";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String groupId = resultSet.getString("id");
                    String name = resultSet.getString("name");
                    double schemeAmount = resultSet.getDouble("scheme_amount");
                    int duration = resultSet.getInt("duration");
                    double monthlyDue = resultSet.getDouble("monthly_due");
                    int totalMembers = resultSet.getInt("total_members");
                    int availableSlots = resultSet.getInt("available_slots");

                    // Only add chit groups with available slots
                    if (availableSlots > 0) {
                        Object[] rowData = {groupId, name, schemeAmount, duration, monthlyDue, totalMembers, availableSlots};
                        tableModel.addRow(rowData);
                    }
                }

                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showConfirmationDialog(String groupId, String groupName) {
        int option = JOptionPane.showConfirmDialog(this, "Join this chit group \"" + groupName + "\"?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            // Perform the action to join the chit group
            joinChitGroup(groupId);
        }
    }

    private void joinChitGroup(String groupId) {
        // Perform the action to join the chit group
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                // Check if the user is already a member of the chit group
                if (isMemberOfChitGroup(connection, customerId, groupId)) {
                    JOptionPane.showMessageDialog(this, "You are already a member of this chit group.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Add the user to the chit group
                String query = "INSERT INTO customer_chit_groups (customer_id, chit_group_id) VALUES (?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, customerId);
                preparedStatement.setString(2, groupId);
                preparedStatement.executeUpdate();

                // Update available slots in chit_groups table
                String updateQuery = "UPDATE chit_groups SET available_slots = available_slots - 1 WHERE id = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setString(1, groupId);
                updateStatement.executeUpdate();

                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Provide feedback to the user
        JOptionPane.showMessageDialog(this, "You have successfully joined the chit group.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean isMemberOfChitGroup(Connection connection, String customerId, String groupId) throws SQLException {
        String query = "SELECT * FROM customer_chit_groups WHERE customer_id = ? AND chit_group_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, customerId);
        preparedStatement.setString(2, groupId);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next();
    }

}