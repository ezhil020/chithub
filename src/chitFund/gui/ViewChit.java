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

public class ViewChit extends JFrame {
    private String customerId;
    private JTable chitGroupTable;
    private DefaultTableModel tableModel;
    private JButton joinAuctionButton;

    public ViewChit(String customerId) {
        this.customerId = customerId;
        initializeComponents();
        loadChitGroupsFromDatabase();
    }

    private void initializeComponents() {
        setTitle("My Chit Groups");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        // Table to display chit groups
        tableModel = new DefaultTableModel();
        chitGroupTable = new JTable(tableModel);
        chitGroupTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = chitGroupTable.getSelectedRow();
                    if (selectedRow != -1) {
                        joinAuctionButton.setEnabled(true);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(chitGroupTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button to join auction
        joinAuctionButton = new JButton("Join Auction");
        joinAuctionButton.setEnabled(false);
        joinAuctionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                joinAuction();
            }
        });
        panel.add(joinAuctionButton, BorderLayout.SOUTH);

        add(panel);
    }

    private void loadChitGroupsFromDatabase() {
        // Clear existing rows from the table
        tableModel.setRowCount(0);

        // Add new column identifiers including the auctions conducted column
        tableModel.setColumnIdentifiers(new String[]{"Group ID", "Name", "Scheme Amount", "Tenure", "Monthly Due", "Total Members", "Available Slots", "Auctions Conducted"});

        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "SELECT chit_groups.id, chit_groups.name, chit_groups.scheme_amount, chit_groups.duration, chit_groups.monthly_due, chit_groups.total_members, chit_groups.available_slots, chit_groups.auctions_conducted " +
                        "FROM chit_groups " +
                        "INNER JOIN customer_chit_groups ON chit_groups.id = customer_chit_groups.chit_group_id " +
                        "WHERE customer_chit_groups.customer_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, customerId);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String groupId = resultSet.getString("id");
                    String name = resultSet.getString("name");
                    double schemeAmount = resultSet.getDouble("scheme_amount");
                    int duration = resultSet.getInt("duration");
                    double monthlyDue = resultSet.getDouble("monthly_due");
                    int totalMembers = resultSet.getInt("total_members");
                    int availableSlots = resultSet.getInt("available_slots");
                    int auctionsConducted = resultSet.getInt("auctions_conducted");

                    Object[] rowData = {groupId, name, schemeAmount, duration, monthlyDue, totalMembers, availableSlots, auctionsConducted};
                    tableModel.addRow(rowData);
                }

                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void joinAuction() {
        int selectedRow = chitGroupTable.getSelectedRow();
        if (selectedRow != -1) {
            String chitGroupId = (String) chitGroupTable.getValueAt(selectedRow, 0);
            String chitGroupName = (String) chitGroupTable.getValueAt(selectedRow, 1);

            // Check if auction is live in the database
            boolean isAuctionLive = checkAuctionLive(chitGroupId);
            if (isAuctionLive) {
                openBidEntryPage(chitGroupId, chitGroupName);
            } else {
                JOptionPane.showMessageDialog(this, "Auction is not live for this chit group", "Auction Not Live", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a chit group to join auction", "No Chit Group Selected", JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean checkAuctionLive(String chitGroupId) {
        boolean isAuctionLive = false;
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "SELECT is_auction_live FROM chit_groups WHERE id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, chitGroupId);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    isAuctionLive = resultSet.getBoolean("is_auction_live");
                }
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return isAuctionLive;
    }

    private void openBidEntryPage(String chitGroupId, String chitGroupName) {
        JFrame joinAuctionFrame = new JFrame("Join Auction - " + chitGroupName);
        joinAuctionFrame.setSize(400, 200);
        joinAuctionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        joinAuctionFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        JTextField bidAmountField = new JTextField(10);
        JButton submitBidButton = new JButton("Submit Bid");
        submitBidButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle submitting bid
                String bidAmountText = bidAmountField.getText();
                double bidAmount = Double.parseDouble(bidAmountText);

                // Retrieve the scheme amount for the selected chit group
                double schemeAmount = getSchemeAmount(chitGroupId);

                // Check if the bid amount is less than 40% of the scheme amount
                if (bidAmount <= (0.4 * schemeAmount)) {
                    // Update the bid amount in the bid table
                    updateBidAmount(chitGroupId, customerId, bidAmount);
                    joinAuctionFrame.dispose();
                } else {
                    JOptionPane.showMessageDialog(joinAuctionFrame, "Bid amount must be less than 40% of the scheme amount", "Invalid Bid Amount", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        JPanel inputPanel = new JPanel(); // Adjust layout to accommodate three rows
        inputPanel.add(new JLabel("Enter Bid Amount: "));
        inputPanel.add(bidAmountField);
        inputPanel.add(submitBidButton);
        inputPanel.add(new JLabel("Enter the discount amount"));
        inputPanel.add(new JLabel("That will be deducted from the scheme amount"));

        panel.add(inputPanel, BorderLayout.CENTER);
        joinAuctionFrame.add(panel);
        joinAuctionFrame.setVisible(true);
    }

    private double getSchemeAmount(String chitGroupId) {
        double schemeAmount = 0.0;
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "SELECT scheme_amount FROM chit_groups WHERE id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, chitGroupId);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    schemeAmount = resultSet.getDouble("scheme_amount");
                }
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return schemeAmount;
    }

    private void updateBidAmount(String chitGroupId, String customerId, double bidAmount) {
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "UPDATE bid SET bid_amount = ? WHERE chit_group_id = ? AND customer_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setDouble(1, bidAmount);
                preparedStatement.setString(2, chitGroupId);
                preparedStatement.setString(3, customerId);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Bid amount updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update bid amount", "Error", JOptionPane.ERROR_MESSAGE);
                }
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}