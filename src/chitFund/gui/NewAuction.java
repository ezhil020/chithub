package chitFund.gui;

import chitFund.util.DatabaseConnector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

public class NewAuction extends JPanel implements ActionListener {
    private JButton conductAuctionButton, viewAuctionButton, endAuctionButton;
    private JComboBox<String> chitGroupDropDown;
    private JTextArea displayArea;
    private ArrayList<String> chitGroups;

    public NewAuction() {
        conductAuctionButton = new JButton("Conduct Auction");
        viewAuctionButton = new JButton("View Auction");
        endAuctionButton = new JButton("End Auction");
        chitGroupDropDown = new JComboBox<>();
        displayArea = new JTextArea(20, 40);
        displayArea.setEditable(false);
        chitGroups = new ArrayList<>();

        fetchChitGroups(); // Fetch chit groups from the database

        for (String chitGroup : chitGroups) {
            chitGroupDropDown.addItem(chitGroup);
        }

        conductAuctionButton.addActionListener(this);
        viewAuctionButton.addActionListener(this);
        endAuctionButton.addActionListener(this);

        setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(1, 3));
        inputPanel.add(chitGroupDropDown);
        inputPanel.add(conductAuctionButton);
        inputPanel.add(endAuctionButton);

        add(inputPanel, BorderLayout.NORTH);
        add(viewAuctionButton, BorderLayout.SOUTH);
        add(new JScrollPane(displayArea), BorderLayout.CENTER);
    }

    private void fetchChitGroups() {
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "SELECT name FROM chit_groups";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    chitGroups.add(resultSet.getString("name"));
                }

                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == conductAuctionButton) {
            conductAuction();
        } else if (e.getSource() == viewAuctionButton) {
            viewAuction();
        } else if (e.getSource() == endAuctionButton) {
            endAuction();
        }
    }

    private void conductAuction() {
        String selectedChitGroup = (String) chitGroupDropDown.getSelectedItem();

        // Check if the auction is already live
        if (isAuctionLive(selectedChitGroup)) {
            JOptionPane.showMessageDialog(this, "Auction for Chit Group '" + selectedChitGroup + "' is already live.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            // Check if the selected chit group is fully filled
            if (isChitGroupFullyFilled(selectedChitGroup)) {
                // Check if the maximum number of auctions has been conducted for this chit group
                if (!isMaxAuctionsReached(selectedChitGroup)) {
                    // Start the auction
                    displayArea.append("Auction started for Chit Group: " + selectedChitGroup + "\n");
                    // Update the "is_auction_live" flag in the chit_groups table
                    updateIsAuctionLiveFlag(selectedChitGroup, true);
                    // Increment the auctions conducted count
                    updateAuctionsConducted(selectedChitGroup);
                    // Add the auction to the auction table
                    addAuction(selectedChitGroup);
                    // Join all customers in the chit group to the bid table with default bid amount 0
                    joinCustomersToBidTable(selectedChitGroup);
                    // Add customers to the payment table
                    addCustomersToPaymentTable(selectedChitGroup);
                } else {
                    JOptionPane.showMessageDialog(this, "Maximum auctions conducted for Chit Group '" + selectedChitGroup + "'. Auction cannot be conducted further.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Chit Group '" + selectedChitGroup + "' is not fully filled.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private boolean isChitGroupFullyFilled(String chitGroup) {
        boolean isFullyFilled = false;
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "SELECT available_slots FROM chit_groups WHERE name = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, chitGroup);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    int slotsAvailable = resultSet.getInt("available_slots");
                    isFullyFilled = slotsAvailable == 0;
                }

                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return isFullyFilled;
    }

    private boolean isMaxAuctionsReached(String chitGroup) {
        boolean maxAuctionsReached = false;
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "SELECT auctions_conducted, duration FROM chit_groups WHERE name = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, chitGroup);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    int auctionsConducted = resultSet.getInt("auctions_conducted");
                    int duration = resultSet.getInt("duration");
                    maxAuctionsReached = auctionsConducted >= duration;
                }

                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return maxAuctionsReached;
    }

    private void updateAuctionsConducted(String chitGroup) {
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "UPDATE chit_groups SET auctions_conducted = auctions_conducted + 1 WHERE name = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, chitGroup);
                preparedStatement.executeUpdate();
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void addAuction(String chitGroup) {
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                // Retrieve the auction number from the chit_groups table
                String query1 = "SELECT auctions_conducted FROM chit_groups WHERE name = ?";
                PreparedStatement preparedStatement1 = connection.prepareStatement(query1);
                preparedStatement1.setString(1, chitGroup);
                ResultSet resultSet = preparedStatement1.executeQuery();

                if (resultSet.next()) {
                    int auctionNumber = resultSet.getInt("auctions_conducted");

                    // Insert the new auction into the auction table
                    String query2 = "INSERT INTO auction (auction_no, chit_group_id) VALUES (?, (SELECT id FROM chit_groups WHERE name = ?))";
                    PreparedStatement preparedStatement2 = connection.prepareStatement(query2);
                    preparedStatement2.setInt(1, auctionNumber); // Use the auction number without incrementing
                    preparedStatement2.setString(2, chitGroup);
                    preparedStatement2.executeUpdate();
                }

                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void joinCustomersToBidTable(String chitGroup) {
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                System.out.println("Database connection established.");

                // Get the ID of the chit group
                String query2 = "SELECT id FROM chit_groups WHERE name = ?";
                PreparedStatement preparedStatement2 = connection.prepareStatement(query2);
                preparedStatement2.setString(1, chitGroup);
                ResultSet resultSet2 = preparedStatement2.executeQuery();
                String customerChitGroupId;
                if (resultSet2.next()) {
                    customerChitGroupId = resultSet2.getString("id");
                    System.out.println("Chit group ID: " + customerChitGroupId);
                } else {
                    System.out.println("Chit group not found: " + chitGroup);
                    return;
                }

                // Get the latest auction number for the specified chit group
                int auctionNumber = getLatestAuctionNumber(chitGroup);
                System.out.println("Latest auction number: " + auctionNumber);

                // Retrieve eligible customers to be added to the bid table
                ArrayList<String> eligibleCustomers = getEligibleCustomers(connection, customerChitGroupId, auctionNumber);

                // Debugging output
                System.out.println("Customers to be added to bid table:");
                int customerCount = 0;

                // Insert each eligible customer into the bid table with bid amount 0
                for (String customerId : eligibleCustomers) {
                    System.out.println("Adding customer " + customerId + " to bid table.");
                    String insertQuery = "INSERT INTO bid (auction_no, chit_group_id, customer_id, bid_amount) VALUES (?, ?, ?, 0)";
                    PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                    insertStatement.setInt(1, auctionNumber); // Use the same auction number for all customers
                    insertStatement.setString(2, customerChitGroupId);
                    insertStatement.setString(3, customerId);
                    int rowsAffected = insertStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Customer " + customerId + " added to bid table.");
                        customerCount++;
                    } else {
                        System.out.println("Failed to add customer " + customerId + " to bid table.");
                    }
                }

                System.out.println("Total customers added to bid table: " + customerCount);

                // Close resources
                preparedStatement2.close();

                System.out.println("Customers added to bid table successfully.");

                DatabaseConnector.closeConnection(connection);
                System.out.println("Database connection closed.");
            } else {
                System.out.println("Failed to establish database connection.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private ArrayList<String> getEligibleCustomers(Connection connection, String customerChitGroupId, int auctionNumber) throws SQLException {
        ArrayList<String> eligibleCustomers = new ArrayList<>();

        // Retrieve customers in the chit group who are not winners and have not already participated in the current auction
        String query = "SELECT c.customer_id FROM customer_chit_groups c " +
                "LEFT JOIN auction a ON c.chit_group_id = a.chit_group_id AND c.customer_id = a.winner_id AND a.auction_no = ? " +
                "WHERE c.chit_group_id = ? AND c.is_winner = FALSE AND (a.winner_id IS NULL OR a.auction_no != ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, auctionNumber);
        preparedStatement.setString(2, customerChitGroupId);
        preparedStatement.setInt(3, auctionNumber);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            String customerId = resultSet.getString("customer_id");
            eligibleCustomers.add(customerId);
        }

        // Close resources
        resultSet.close();
        preparedStatement.close();

        return eligibleCustomers;
    }

    private boolean isCustomerWinner(Connection connection, String customerChitGroupId, String customerId) throws SQLException {
        // Check if the customer is already a winner in any auction for the given chit group
        String winnerCheckQuery = "SELECT winner_id FROM auction WHERE chit_group_id = ? AND winner_id = ?";
        PreparedStatement winnerCheckStatement = connection.prepareStatement(winnerCheckQuery);
        winnerCheckStatement.setString(1, customerChitGroupId);
        winnerCheckStatement.setString(2, customerId);
        ResultSet winnerCheckResultSet = winnerCheckStatement.executeQuery();
        return winnerCheckResultSet.next();
    }

    private int getLatestAuctionNumber(String chitGroup) {
        int latestAuctionNumber = 0;
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "SELECT auctions_conducted FROM chit_groups WHERE name = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, chitGroup);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    latestAuctionNumber = resultSet.getInt("auctions_conducted");
                    System.out.println("Latest auction number for chit group " + chitGroup + ": " + latestAuctionNumber);
                }
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return latestAuctionNumber;
    }

    private void viewAuction() {
        String selectedChitGroup = (String) chitGroupDropDown.getSelectedItem();
        //displayArea.append("Viewing auctions for Chit Group: " + selectedChitGroup + "\n");

        displayArea.setText(""); // To clear the screen

        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {

                String query2 = "SELECT id from chit_groups where name = ?";
                PreparedStatement preparedStatement2 = connection.prepareStatement(query2);
                preparedStatement2.setString(1, selectedChitGroup);
                ResultSet resultSet2 = preparedStatement2.executeQuery();
                String customer_chit_group;
                resultSet2.next();
                customer_chit_group = resultSet2.getString("id");

                String query = "SELECT a.auction_no, a.winner_id, c.customer_name as winner_name, a.discount_amount " +
                        "FROM auction a " +
                        "INNER JOIN customers c ON a.winner_id = c.customer_id " +
                        "WHERE a.chit_group_id = ?";

                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, customer_chit_group);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (!resultSet.isBeforeFirst()) {
                    // No auctions conducted for this chit group
                    JOptionPane.showMessageDialog(this, "No auctions conducted for Chit Group '" + selectedChitGroup + "'.", "Information", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Display auction details
                    displayArea.append("Auction No\tWinner ID\tWinner Name\t  Discount Amount\n");
                    while (resultSet.next()) {
                        int auctionNo = resultSet.getInt("auction_no");
                        String winnerId = resultSet.getString("winner_id");
                        String winnerName = resultSet.getString("winner_name");
                        double discountAmount = resultSet.getDouble("discount_amount");
                        displayArea.append(auctionNo + "\t" + winnerId + "\t" + winnerName + "\t  " + discountAmount + "\n");
                    }
                }

                // Close resources
                resultSet.close();
                preparedStatement.close();
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while viewing auctions: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void endAuction() {
        String selectedChitGroup = (String) chitGroupDropDown.getSelectedItem();

        // Check if the auction is live
        if (isAuctionLive(selectedChitGroup)) {
            // Update the is_auction_live column in the chit_groups table to false
            updateIsAuctionLiveFlag(selectedChitGroup, false);
            displayArea.append("Auction ended for Chit Group: " + selectedChitGroup + "\n");

            // Display bid table details for the current auction number and chit group
            displayBidTableDetails(selectedChitGroup);

            // Select winner and update the winner_id in the auction table
            String winnerAccNo = selectWinner(selectedChitGroup);

            // Credit the winner with the winning amount
            if (winnerAccNo != null) {
                creditWinner(selectedChitGroup, winnerAccNo);
            } else {
                JOptionPane.showMessageDialog(this, "No winner selected for Chit Group '" + selectedChitGroup + "'.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            // Update due amount in the payment table
            updateDueAmountInPaymentTable(selectedChitGroup);

            // Reduce monthly due from customer balance
            boolean monthlyDueReduced = reduceMonthlyDueFromCustomerBalance(selectedChitGroup);

            // If monthly due was successfully reduced, update is_due_paid flag to true
            if (monthlyDueReduced) {
                // Update is_due_paid flag to true in the payment table
                updateIsDuePaid(selectedChitGroup, getLatestAuctionNumber(selectedChitGroup));
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reduce monthly due from customer accounts for Chit Group '" + selectedChitGroup + "'.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } else {
            JOptionPane.showMessageDialog(this, "Auction for Chit Group '" + selectedChitGroup + "' is not live.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void creditWinner(String chitGroup, String winnerAccountNo) {
        try (Connection connection = DatabaseConnector.connect()) {
            if (connection != null) {
                int auctionNo = getLatestAuctionNumber(chitGroup);
                // Retrieve the winner's customer ID
                String winnerIdQuery = "SELECT customer_id FROM customers WHERE bank_account_no = ?";
                PreparedStatement winnerIdStatement = connection.prepareStatement(winnerIdQuery);
                winnerIdStatement.setString(1, winnerAccountNo);
                ResultSet winnerIdResultSet = winnerIdStatement.executeQuery();

                if (winnerIdResultSet.next()) {
                    String winnerId = winnerIdResultSet.getString("customer_id");

                    // Retrieve the scheme amount for the selected chit group
                    String schemeAmountQuery = "SELECT scheme_amount FROM chit_groups WHERE id = ?";
                    PreparedStatement schemeAmountStatement = connection.prepareStatement(schemeAmountQuery);
                    schemeAmountStatement.setString(1, getChitGroupId(chitGroup));
                    ResultSet schemeAmountResultSet = schemeAmountStatement.executeQuery();
                    if (schemeAmountResultSet.next()) {
                        double schemeAmount = schemeAmountResultSet.getDouble("scheme_amount");

                        // Retrieve the bid amount for the winner from the bid table
                        String bidAmountQuery = "SELECT bid_amount FROM bid WHERE customer_id = ? AND chit_group_id = ? AND auction_no = ?";
                        PreparedStatement bidAmountStatement = connection.prepareStatement(bidAmountQuery);
                        bidAmountStatement.setString(1, winnerId);
                        bidAmountStatement.setString(2, getChitGroupId(chitGroup));
                        bidAmountStatement.setInt(3, auctionNo);
                        ResultSet bidAmountResultSet = bidAmountStatement.executeQuery();
                        if (bidAmountResultSet.next()) {
                            double bidAmount = bidAmountResultSet.getDouble("bid_amount");

                            // Calculate the winning amount
                            double winningAmount = (bidAmount == 0) ? schemeAmount : schemeAmount - bidAmount;

                            // Update the winner's account balance
                            String updateWinnerBalanceQuery = "UPDATE customer_account SET balance = balance + ? WHERE account_no = ?";
                            PreparedStatement updateWinnerBalanceStatement = connection.prepareStatement(updateWinnerBalanceQuery);
                            updateWinnerBalanceStatement.setDouble(1, winningAmount);
                            updateWinnerBalanceStatement.setString(2, winnerAccountNo);
                            int rowsUpdated = updateWinnerBalanceStatement.executeUpdate();

                            if (rowsUpdated > 0) {
                                System.out.println("Winner '" + winnerAccountNo + "' credited with winning amount: " + winningAmount);
                            } else {
                                System.out.println("Failed to credit winner '" + winnerAccountNo + "'");
                            }

                            // Deduct the winning amount from the admin's account
                            String adminAccountNo = getAdminAccountNumber(auctionNo);
                            if (adminAccountNo != null) {
                                String deductAdminBalanceQuery = "UPDATE admin_account SET balance = balance - ? WHERE account_no = ?";
                                PreparedStatement deductAdminBalanceStatement = connection.prepareStatement(deductAdminBalanceQuery);
                                deductAdminBalanceStatement.setDouble(1, winningAmount);
                                deductAdminBalanceStatement.setString(2, adminAccountNo);
                                int adminRowsUpdated = deductAdminBalanceStatement.executeUpdate();

                                if (adminRowsUpdated > 0) {
                                    System.out.println("Admin account '" + adminAccountNo + "' deducted with winning amount: " + winningAmount);
                                } else {
                                    System.out.println("Failed to deduct winning amount from admin account '" + adminAccountNo + "'");
                                }
                            } else {
                                System.out.println("Admin account not found for auction number: " + auctionNo);
                            }
                        } else {
                            System.out.println("Bid amount not found for winner '" + winnerAccountNo + "'");
                        }
                    } else {
                        System.out.println("Scheme amount not found for chit group '" + chitGroup + "'");
                    }
                } else {
                    System.out.println("Winner ID not found for account number '" + winnerAccountNo + "'");
                }

                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String getAdminAccountNumber(int auctionNumber) {
        String adminAccountNo = null;
        if (auctionNumber >= 1 && auctionNumber <= 5) {
            adminAccountNo = "43431212717"; // Jeevan's account number
        } else if (auctionNumber >= 6 && auctionNumber <= 10) {
            adminAccountNo = "11788201021"; // Ezhil's account number
        } else if (auctionNumber >= 11 && auctionNumber <= 15) {
            adminAccountNo = "31982291129"; // Keertika's account number
        } else if (auctionNumber >= 16 && auctionNumber <= 20) {
            adminAccountNo = "12092121987"; // Guru's account number
        }
        return adminAccountNo;
    }

    private void addCustomersToPaymentTable(String chitGroup) {
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                // Retrieve all customers in the chit group along with their unique identifiers
                String customerQuery = "SELECT customer_id FROM customer_chit_groups WHERE chit_group_id = ?";
                PreparedStatement customerStatement = connection.prepareStatement(customerQuery);
                customerStatement.setString(1, getChitGroupId(chitGroup));
                ResultSet customerResultSet = customerStatement.executeQuery();

                // Add customers to the payment table
                while (customerResultSet.next()) {
                    String customerId = customerResultSet.getString("customer_id");

                    // Insert a new row into the payment table
                    String insertQuery = "INSERT INTO payment (customer_id, chit_group_id, auction_no, due_amount, is_due_paid) VALUES (?, ?, ?, 0, FALSE)";
                    PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                    insertStatement.setString(1, customerId);
                    insertStatement.setString(2, getChitGroupId(chitGroup));
                    insertStatement.setInt(3, getLatestAuctionNumber(chitGroup)); // Assuming you have a method to get the latest auction number
                    insertStatement.executeUpdate();
                }

                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateDueAmountInPaymentTable(String chitGroup) {
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                int auctionNumber = getLatestAuctionNumber(chitGroup);
                String query = "SELECT monthly_due FROM auction WHERE auction_no = ? AND chit_group_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, auctionNumber);
                preparedStatement.setString(2, getChitGroupId(chitGroup));
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    double monthlyDue = resultSet.getDouble("monthly_due");
                    String updateQuery = "UPDATE payment SET due_amount = ? WHERE chit_group_id = ? AND auction_no = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setDouble(1, monthlyDue);
                    updateStatement.setString(2, getChitGroupId(chitGroup));
                    updateStatement.setInt(3, auctionNumber);
                    updateStatement.executeUpdate();
                }
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateIsDuePaid(String chitGroup, int auctionNumber) {
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                // Update is_due_paid flag to true in the payment table
                String updateFlagQuery = "UPDATE payment SET is_due_paid = TRUE WHERE chit_group_id = ? AND auction_no = ?";
                PreparedStatement updateFlagStatement = connection.prepareStatement(updateFlagQuery);
                updateFlagStatement.setString(1, getChitGroupId(chitGroup));
                updateFlagStatement.setInt(2, auctionNumber);
                int rowsUpdated = updateFlagStatement.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("is_due_paid flag updated successfully for Chit Group: " + chitGroup + " and Auction Number: " + auctionNumber);
                } else {
                    System.out.println("Failed to update is_due_paid flag for Chit Group: " + chitGroup + " and Auction Number: " + auctionNumber);
                }

                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private boolean reduceMonthlyDueFromCustomerBalance(String chitGroup) {
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                try {
                    // Get the latest auction number for the specified chit group
                    int auctionNumber = getLatestAuctionNumber(chitGroup);

                    // Retrieve monthly due for the current auction number and chit group
                    String query = "SELECT monthly_due FROM auction WHERE auction_no = ? AND chit_group_id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setInt(1, auctionNumber);
                    preparedStatement.setString(2, getChitGroupId(chitGroup));
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        double monthlyDue = resultSet.getDouble("monthly_due");

                        // Retrieve all customers in the chit group along with their account numbers and balances
                        String customerQuery = "SELECT customers.bank_account_no, customer_account.balance " +
                                "FROM customers " +
                                "INNER JOIN customer_chit_groups ON customers.customer_id = customer_chit_groups.customer_id " +
                                "INNER JOIN customer_account ON customers.bank_account_no = customer_account.account_no " +
                                "WHERE customer_chit_groups.chit_group_id = ?";
                        PreparedStatement customerStatement = connection.prepareStatement(customerQuery);
                        customerStatement.setString(1, getChitGroupId(chitGroup));
                        ResultSet customerResultSet = customerStatement.executeQuery();

                        // Reduce monthly due amount from customer account balance
                        while (customerResultSet.next()) {
                            String accountNo = customerResultSet.getString("bank_account_no");
                            double balance = customerResultSet.getDouble("balance");

                            // Check if the balance is sufficient to deduct the monthly due
                            if (balance >= monthlyDue) {
                                // Update customer account balance
                                String updateQuery = "UPDATE customer_account SET balance = balance - ? WHERE account_no = ?";
                                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                                updateStatement.setDouble(1, monthlyDue);
                                updateStatement.setString(2, accountNo);
                                updateStatement.executeUpdate();

                                // Determine which admin account to credit the deducted amount
                                String adminAccountNo;
                                if (auctionNumber <= 5)
                                    adminAccountNo = "43431212717"; // Jeevan's account number
                                else if (auctionNumber <= 10)
                                    adminAccountNo = "11788201021"; // Ezhil's account number
                                else if (auctionNumber <= 15)
                                    adminAccountNo = "31982291129"; // Keertika's account number
                                else
                                    adminAccountNo = "12092121987"; // Guru's account number

                                // Update admin account balance
                                String adminUpdateQuery = "UPDATE admin_account SET balance = balance + ? WHERE account_no = ?";
                                PreparedStatement adminUpdateStatement = connection.prepareStatement(adminUpdateQuery);
                                adminUpdateStatement.setDouble(1, monthlyDue);
                                adminUpdateStatement.setString(2, adminAccountNo);
                                adminUpdateStatement.executeUpdate();
                            } else {
                                System.out.println("Insufficient balance for account: " + accountNo + " in Chit Group: " + chitGroup);
                                return false; // Monthly due not reduced successfully due to insufficient balance
                            }
                        }

                        // Monthly due reduced successfully for all customers
                        return true;
                    } else {
                        System.out.println("Monthly due not found for Chit Group: " + chitGroup);
                        return false; // Monthly due not found
                    }
                } finally {
                    DatabaseConnector.closeConnection(connection);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false; // Error occurred during database operation
    }

    private String getChitGroupId(String chitGroup) {
        String chitGroupId = null;
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "SELECT id FROM chit_groups WHERE name = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, chitGroup);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    chitGroupId = resultSet.getString("id");
                }
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return chitGroupId;
    }


    private void displayBidTableDetails(String chitGroup) {
        displayArea.setText("");
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                // Get the latest auction number for the specified chit group
                int auctionNumber = getLatestAuctionNumber(chitGroup);

                // Retrieve bid table details for the current auction number and chit group
                String query2 = "SELECT id from chit_groups where name = ?";
                PreparedStatement preparedStatement2 = connection.prepareStatement(query2);
                preparedStatement2.setString(1, chitGroup);
                ResultSet resultSet2 = preparedStatement2.executeQuery();

                String customer_chit_group;
                resultSet2.next();
                customer_chit_group = resultSet2.getString("id");

                String query = "SELECT * FROM bid WHERE auction_no = ? AND chit_group_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, auctionNumber);
                preparedStatement.setString(2, customer_chit_group);
                ResultSet resultSet = preparedStatement.executeQuery();

                displayArea.append("\nBid Table Details for Current Auction:\n");
                displayArea.append("Customer ID\t\tBid Amount\n");

                while (resultSet.next()) {
                    String customerId = resultSet.getString("customer_id");
                    double bidAmount = resultSet.getDouble("bid_amount");
                    displayArea.append(customerId + "\t\t" + bidAmount + "\n");
                }

                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String selectWinner(String chitGroup) {
        String winnerAccountNo = null;
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                // Get the latest auction number for the specified chit group
                int auctionNumber = getLatestAuctionNumber(chitGroup);

                // Retrieve all bids for the current auction and chit group
                String query2 = "SELECT id from chit_groups where name = ?";
                PreparedStatement preparedStatement2 = connection.prepareStatement(query2);
                preparedStatement2.setString(1, chitGroup);
                ResultSet resultSet2 = preparedStatement2.executeQuery();

                String customerChitGroupId;
                resultSet2.next();
                customerChitGroupId = resultSet2.getString("id");

                String query = "SELECT customer_id, bid_amount FROM bid WHERE auction_no = ? AND chit_group_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, auctionNumber);
                preparedStatement.setString(2, customerChitGroupId);
                ResultSet resultSet = preparedStatement.executeQuery();

                String query3 = "SELECT monthly_due, total_members FROM chit_groups WHERE name = ?";
                PreparedStatement preparedStatement3 = connection.prepareStatement(query3);
                preparedStatement3.setString(1, chitGroup);
                ResultSet resultSet3 = preparedStatement3.executeQuery();

                resultSet3.next();

                double monthlyDue=resultSet3.getDouble("monthly_due");
                int totalCustomers=resultSet3.getInt("total_members");

                ArrayList<String> potentialWinners = new ArrayList<>();
                double highestBid = 0;

                // Find customers who participated in the auction and their highest bid
                while (resultSet.next()) {
                    String customerId = resultSet.getString("customer_id");
                    double bidAmount = resultSet.getDouble("bid_amount");

                    if (bidAmount > highestBid) {
                        highestBid = bidAmount;
                        potentialWinners.clear(); // Clear potential winners if higher bid found
                        potentialWinners.add(customerId);
                    } else if (bidAmount == highestBid) {
                        // Add customer to potential winners if bid amount matches the highest bid
                        potentialWinners.add(customerId);
                    }
                }

                // If there are potential winners, choose one randomly
                if (!potentialWinners.isEmpty()) {
                    Collections.shuffle(potentialWinners);
                    String winnerId = potentialWinners.get(0);

                    double updatedMonthlyDue =0;

                    if(highestBid==0){
                        updatedMonthlyDue =monthlyDue;
                    }
                    else{
                        updatedMonthlyDue =monthlyDue-(highestBid/totalCustomers);
                    }

                    // Update the winner_id and discount amount in the auction table
                    String updateQuery = "UPDATE auction SET winner_id = ?, discount_amount = ?, monthly_due = ? WHERE auction_no = ? AND chit_group_id = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setString(1, winnerId);
                    updateStatement.setDouble(2, highestBid);
                    updateStatement.setDouble(3, updatedMonthlyDue);
                    updateStatement.setInt(4, auctionNumber);
                    updateStatement.setString(5, customerChitGroupId);
                    updateStatement.executeUpdate();

                    // Update the is_winner flag in the customer_chit_groups table
                    String updateWinnerQuery = "UPDATE customer_chit_groups SET is_winner = TRUE WHERE customer_id = ? AND chit_group_id = ?";
                    PreparedStatement updateWinnerStatement = connection.prepareStatement(updateWinnerQuery);
                    updateWinnerStatement.setString(1, winnerId);
                    updateWinnerStatement.setString(2, customerChitGroupId);
                    updateWinnerStatement.executeUpdate();

                    // Retrieve the account number of the winner
                    String winnerAccountQuery = "SELECT bank_account_no FROM customers WHERE customer_id = ?";
                    PreparedStatement winnerAccountStatement = connection.prepareStatement(winnerAccountQuery);
                    winnerAccountStatement.setString(1, winnerId);
                    ResultSet winnerAccountResultSet = winnerAccountStatement.executeQuery();
                    if (winnerAccountResultSet.next()) {
                        winnerAccountNo = winnerAccountResultSet.getString("bank_account_no");
                    }

                    displayArea.append("Winner selected for Chit Group: " + chitGroup + "\n");
                    displayArea.append("Winner ID: " + winnerId + "\n");
                    displayArea.append("Discount Amount: " + highestBid + "\n");
                } else {
                    displayArea.append("No winners selected for Chit Group: " + chitGroup + "\n");
                }

                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return winnerAccountNo;
    }

    private boolean isAuctionLive(String chitGroup) {
        boolean isLive = false;
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "SELECT is_auction_live FROM chit_groups WHERE name = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, chitGroup);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    isLive = resultSet.getBoolean("is_auction_live");
                }

                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return isLive;
    }

    private void updateIsAuctionLiveFlag(String chitGroup, boolean isLive) {
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "UPDATE chit_groups SET is_auction_live = ? WHERE name = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setBoolean(1, isLive);
                preparedStatement.setString(2, chitGroup);
                preparedStatement.executeUpdate();
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("New Auction");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new NewAuction());
        frame.pack();
        frame.setVisible(true);
    }
}