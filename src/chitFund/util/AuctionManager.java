package chitFund.util;

import chitFund.domain.Auction;
import chitFund.domain.Bid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuctionManager {

    public void conductAuction(int auctionNumber) {
        List<Bid> bids = getAllBidsForAuction(auctionNumber);
        if (!bids.isEmpty()) {
            double lowestBid = Double.MAX_VALUE;
            int winningCustomerId = -1;

            for (Bid bid : bids) {
                if (bid.getBiddingAmount() < lowestBid) {
                    lowestBid = bid.getBiddingAmount();
                    winningCustomerId = bid.getCustomerId();
                }
            }

            markCustomerAsWinner(winningCustomerId);
            storeAuctionResult(new Auction(auctionNumber, lowestBid, winningCustomerId)); // Updated constructor call
        } else {
            System.out.println("No bids found for auction " + auctionNumber);
        }
    }

    private void markCustomerAsWinner(int customerId) {
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "UPDATE customers SET is_winner = 1 WHERE customer_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, customerId);
                preparedStatement.executeUpdate();
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void storeAuctionResult(Auction auction) {
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "INSERT INTO auctions (auction_number, lowest_bid, winning_customer_id) VALUES (?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, auction.getAuctionNumber());
                preparedStatement.setDouble(2, auction.getLowestBid());
                preparedStatement.setInt(3, auction.getWinningCustomerId());
                preparedStatement.executeUpdate();
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private List<Bid> getAllBidsForAuction(int auctionNumber) {
        List<Bid> bids = new ArrayList<>();
        try {
            Connection connection = DatabaseConnector.connect();
            if (connection != null) {
                String query = "SELECT * FROM bids WHERE auction_number = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, auctionNumber);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    int customerId = resultSet.getInt("customer_id");
                    double biddingAmount = resultSet.getDouble("bidding_amount");
                    bids.add(new Bid(customerId, auctionNumber, biddingAmount));
                }
                DatabaseConnector.closeConnection(connection);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return bids;
    }

    public Auction getAuctionResult(int auctionNumber) {
        // Implement logic to retrieve auction result from the database
        // Example: SELECT * FROM auctions WHERE auction_number = auctionNumber

        // For demonstration purposes, creating a dummy Auction object
        Auction auction = new Auction(auctionNumber, 100.0, 123); // Example: Auction(auctionNumber, lowestBid, winningCustomerId)
        return auction;
    }
}
