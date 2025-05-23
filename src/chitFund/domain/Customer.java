package chitFund.domain;

public class Customer {
    private int customerId;
    private String customerName;
    private String aadharNo;
    private String panNo;
    private String address;
    private String phoneNum;
    private String emailId;
    private boolean isWinner;

    public Customer(int customerId, String customerName, String aadharNo, String panNo, String address, String phoneNum, String emailId, boolean isWinner) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.aadharNo = aadharNo;
        this.panNo = panNo;
        this.address = address;
        this.phoneNum = phoneNum;
        this.emailId = emailId;
        this.isWinner = isWinner;
    }

    // Getters and setters
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAadharNo() {
        return aadharNo;
    }

    public void setAadharNo(String aadharNo) {
        this.aadharNo = aadharNo;
    }

    public String getPanNo() {
        return panNo;
    }

    public void setPanNo(String panNo) {
        this.panNo = panNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public void setWinner(boolean winner) {
        isWinner = winner;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", aadharNo='" + aadharNo + '\'' +
                ", panNo='" + panNo + '\'' +
                ", address='" + address + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", emailId='" + emailId + '\'' +
                ", isWinner=" + isWinner +
                '}';
    }
}