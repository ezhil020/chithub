package chitFund.util;

import chitFund.domain.Bid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BidManager {
    public void placeBid(Bid bid) {
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "INSERT INTO bids (customer_id, auction_number, bidding_amount) VALUES (?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, bid.getCustomerId());
                preparedStatement.setInt(2, bid.getAuctionNumber());
                preparedStatement.setDouble(3, bid.getBiddingAmount());
                preparedStatement.executeUpdate();
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}