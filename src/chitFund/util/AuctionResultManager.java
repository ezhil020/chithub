package chitFund.util;

import chitFund.domain.Auction;

public class AuctionResultManager {
    private AuctionManager auctionManager;

    public AuctionResultManager(AuctionManager auctionManager) {
        this.auctionManager = auctionManager;
    }

    public Auction getAuctionResult(int auctionNumber) {
        // Implement logic to retrieve auction result from the auction manager
        return auctionManager.getAuctionResult(auctionNumber);
    }
}