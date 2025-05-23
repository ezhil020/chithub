package chitFund.gui;

import chitFund.gui.NewAuction;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminPanel extends JFrame {
    public AdminPanel() {
        setTitle("Admin Panel");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1, 10, 10)); // Add some spacing between buttons

        JButton manageChitGroupsButton = createButton("Manage Chit Groups");
        JButton manageCustomersButton = createButton("Manage Customers");
        JButton manageAuctionsButton = createButton("Manage Auctions");

        // Add buttons to the panel
        panel.add(manageChitGroupsButton);
        panel.add(manageCustomersButton);
        panel.add(manageAuctionsButton);

        manageChitGroupsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ManageChitGroups().setVisible(true); // Set the window visible after creation
            }
        });

        manageCustomersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ManageCustomers().setVisible(true); // Set the window visible after creation
            }
        });

        manageAuctionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewAuction newAuction = new NewAuction();
                JFrame auctionFrame = new JFrame("New Auction");
                auctionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                auctionFrame.add(newAuction);
                auctionFrame.pack();
                auctionFrame.setLocationRelativeTo(auctionFrame); // Set relative location to this frame
                auctionFrame.setVisible(true);
            }
        });

        add(panel);
        setVisible(true);
    }

    // Helper method to create buttons with consistent style
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(200, 40)); // Set preferred button size
        button.setFont(new Font("Arial", Font.PLAIN, 16)); // Set font
        button.setFocusPainted(false); // Remove focus border
        button.setBackground(new Color(50, 150, 250)); // Set background color
        button.setForeground(Color.WHITE); // Set text color

        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AdminPanel();
            }
        });
    }
}