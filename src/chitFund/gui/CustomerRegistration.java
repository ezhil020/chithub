package chitFund.gui;

import chitFund.util.DatabaseConnector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Random;
import java.util.regex.Pattern;

public class CustomerRegistration extends JFrame {
    private JTextField customerNameField;
    private JTextField aadharNoField;
    private JTextField panNoField;
    private JTextField addressField;
    private JTextField phoneNumField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField bankAccountNoField;
    private JButton registerButton;
    private JLabel loginLink;
    private JPanel formPanel;

    public CustomerRegistration() {
        setTitle("Customer Registration");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeComponents();

        setVisible(true);
    }

    private void initializeComponents() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding

        formPanel = new JPanel(new GridLayout(0, 1, 10, 10)); // Adjusted grid layout with spacing
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        // Add form components with labels
        formPanel.add(createFormField("Customer Name:", customerNameField = new JTextField()));
        formPanel.add(createFormField("Aadhar Number:", aadharNoField = new JTextField()));
        formPanel.add(createFormField("PAN Number:", panNoField = new JTextField()));
        formPanel.add(createFormField("Address:", addressField = new JTextField()));
        formPanel.add(createFormField("Phone Number:", phoneNumField = new JTextField()));
        formPanel.add(createFormField("Email:", emailField = new JTextField()));
        formPanel.add(createFormField("Password:", passwordField = new JPasswordField()));
        formPanel.add(createFormField("Bank Account Number:", bankAccountNoField = new JTextField()));

        panel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new BorderLayout()); // Use border layout for button and login link
        registerButton = new JButton("Register");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (areAllTextFieldsFilled()) {
                    if (validateFields()) {
                        registerCustomer();
                    }
                } else {
                    JOptionPane.showMessageDialog(CustomerRegistration.this, "Please fill out all the text fields.", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        buttonPanel.add(registerButton, BorderLayout.NORTH); // Add register button to top of panel

        // Create a panel for the login link
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Centered panel for login link
        loginLink = new JLabel("Already registered? Login here");
        loginLink.setForeground(Color.BLUE); // Set color to blue to indicate it's a link
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Change cursor to hand when hovering
        loginLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dispose(); // Close the registration page
                new CustomerLogin().setVisible(true); // Open the login page
            }
        });
        loginPanel.add(loginLink); // Add login link to login panel
        buttonPanel.add(loginPanel, BorderLayout.CENTER); // Add login panel to center of button panel

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
    }

    // Method to create a labeled form field
    private JPanel createFormField(String label, JComponent field) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.add(new JLabel(label), BorderLayout.NORTH);
        fieldPanel.add(field, BorderLayout.CENTER);
        return fieldPanel;
    }

    // Method to check if all text fields are filled
    private boolean areAllTextFieldsFilled() {
        boolean allFilled = true;
        Component[] components = formPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                JPanel fieldPanel = (JPanel) component;
                Component[] subComponents = fieldPanel.getComponents();
                for (Component subComponent : subComponents) {
                    if (subComponent instanceof JTextField) {
                        JTextField textField = (JTextField) subComponent;
                        if (textField.getText().isEmpty()) {
                            allFilled = false;
                            break;
                        }
                    }
                }
            }
            if (!allFilled) {
                break;
            }
        }
        return allFilled;
    }

    // Method to validate all fields
    private boolean validateFields() {
        if (!validateAadharNumber(aadharNoField.getText())) {
            JOptionPane.showMessageDialog(this, "Invalid Aadhar number.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!validatePanNumber(panNoField.getText())) {
            JOptionPane.showMessageDialog(this, "Invalid PAN number.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!validatePhoneNumber(phoneNumField.getText())) {
            JOptionPane.showMessageDialog(this, "Invalid phone number.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!validateBankAccountNumber(bankAccountNoField.getText())) {
            JOptionPane.showMessageDialog(this, "Invalid bank account number.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!validateEmail(emailField.getText())) {
            JOptionPane.showMessageDialog(this, "Invalid email address.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    // Validation methods
    private boolean validateAadharNumber(String aadhar) {
        return aadhar.matches("\\d{12}");
    }

    private boolean validatePanNumber(String pan) {
        return pan.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}");
    }

    private boolean validatePhoneNumber(String phone) {
        return phone.matches("\\d{10}");
    }

    private boolean validateBankAccountNumber(String account) {
        return account.matches("\\d{11}");
    }

    private boolean validateEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private void registerCustomer() {
        // Retrieving values from input fields
        String customerName = customerNameField.getText();
        String aadharStr = aadharNoField.getText();
        String panNo = panNoField.getText();
        String address = addressField.getText();
        String phoneNumStr = phoneNumField.getText();
        String email = emailField.getText();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);
        String bankAccountNoStr = bankAccountNoField.getText();
        String customerId = generateCustomerId();

        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                // Insert data into customers table
                String query = "INSERT INTO customers (customer_id, customer_name, aadhar_no, pan_no, address, phone_num, email, password, bank_account_no, registration_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, customerId);
                preparedStatement.setString(2, customerName);
                preparedStatement.setLong(3, Long.parseLong(aadharStr));
                preparedStatement.setString(4, panNo);
                preparedStatement.setString(5, address);
                preparedStatement.setLong(6, Long.parseLong(phoneNumStr));
                preparedStatement.setString(7, email);
                preparedStatement.setString(8, password);
                preparedStatement.setLong(9, Long.parseLong(bankAccountNoStr));
                preparedStatement.setTimestamp(10, new Timestamp(System.currentTimeMillis())); // Set registration time
                preparedStatement.executeUpdate();

                DatabaseConnector.closeConnection(connection);

                JOptionPane.showMessageDialog(this, "Registration successful! Customer ID: " + customerId, "Success", JOptionPane.INFORMATION_MESSAGE);

                // Clear input fields after successful registration
                clearFields();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Registration failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String generateCustomerId() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 6; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    private void clearFields() {
        customerNameField.setText("");
        aadharNoField.setText("");
        panNoField.setText("");
        addressField.setText("");
        phoneNumField.setText("");
        emailField.setText("");
        passwordField.setText("");
        bankAccountNoField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CustomerRegistration();
            }
        });
    }
}