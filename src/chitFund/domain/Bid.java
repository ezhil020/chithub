package chitFund.domain;

public class Bid {
    private int customerId;
    private int auctionNumber;
    private double biddingAmount;

    public Bid(int customerId, int auctionNumber, double biddingAmount) {
        this.customerId = customerId;
        this.auctionNumber = auctionNumber;
        this.biddingAmount = biddingAmount;
    }

    // Getters and setters
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getAuctionNumber() {
        return auctionNumber;
    }

    public void setAuctionNumber(int auctionNumber) {
        this.auctionNumber = auctionNumber;
    }

    public double getBiddingAmount() {
        return biddingAmount;
    }

    public void setBiddingAmount(double biddingAmount) {
        this.biddingAmount = biddingAmount;
    }
}