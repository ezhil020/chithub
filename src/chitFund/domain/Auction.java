package chitFund.domain;

public class Auction {
    private int auctionNumber;
    private double lowestBid;
    private int winningCustomerId;

    public Auction(int auctionNumber, double lowestBid, int winningCustomerId) {
        this.auctionNumber = auctionNumber;
        this.lowestBid = lowestBid;
        this.winningCustomerId = winningCustomerId;
    }

    public int getAuctionNumber() {
        return auctionNumber;
    }

    public double getLowestBid() {
        return lowestBid;
    }

    public int getWinningCustomerId() {
        return winningCustomerId;
    }
}
