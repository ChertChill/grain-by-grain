package database;

public class Bank {
    private int bankID;
    private String name;
    private String bic;
    private String address;

    public Bank(int bankID, String name, String bic, String address) {
        this.bankID = bankID;
        this.name = name;
        this.bic = bic;
        this.address = address;
    }

    // Getter and Setter for bankID
    public int getBankID() {
        return bankID;
    }

    public void setBankID(int bankID) {
        this.bankID = bankID;
    }

    // Getter and Setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and Setter for bic
    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    // Getter and Setter for address
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
