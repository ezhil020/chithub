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
import java.sql.Timestamp;

public class CustomerLogin extends JFrame {
    private JTextField customerIdField;
    private JPasswordField passwordField;

    public CustomerLogin() {
        initializeComponents();
    }

    private void initializeComponents() {
        setTitle("Login Form");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10)); // Rows, Columns, Horizontal Gap, Vertical Gap

        // Labels and fields for customer ID and password
        JLabel customerIdLabel = new JLabel("Customer ID:");
        formPanel.add(customerIdLabel);
        customerIdField = new JTextField(10); // Reduced size text field
        formPanel.add(customerIdField);

        JLabel passwordLabel = new JLabel("Password:");
        formPanel.add(passwordLabel);
        passwordField = new JPasswordField(10); // Reduced size text field
        formPanel.add(passwordField);

        panel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Center the button

        // Button to submit login credentials
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String customerId = customerIdField.getText();
                String password = new String(passwordField.getPassword());

                // Validate login credentials
                if (validateLogin(customerId, password)) {
                    JOptionPane.showMessageDialog(CustomerLogin.this, "Login Successful!");
                    // Log the login time
                    logLoginTime(customerId);
                    // Open the JoinChit window upon successful login
                    openJoinChitWindow(customerId);
                } else {
                    JOptionPane.showMessageDialog(CustomerLogin.this, "Invalid Customer ID or Password. Please try again.");
                    // Clear fields for the user to try again
                    customerIdField.setText("");
                    passwordField.setText("");
                }
            }
        });
        buttonPanel.add(loginButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private boolean validateLogin(String customerId, String password) {
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "SELECT * FROM customers WHERE customer_id = ? AND password = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, customerId);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    // Record with matching credentials found
                    DatabaseConnector.closeConnection(connection);
                    return true;
                }
                DatabaseConnector.closeConnection(connection);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // No record found with matching credentials
        return false;
    }

    private void logLoginTime(String customerId) {
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "INSERT INTO customer_logins (customer_id, login_time) VALUES (?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, customerId);
                preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                preparedStatement.executeUpdate();
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to log login time.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openJoinChitWindow(String customerId) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JoinChit(customerId).setVisible(true);
            }
        });
        dispose(); // Close the login window after opening JoinChit
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CustomerLogin().setVisible(true);
            }
        });
    }
}