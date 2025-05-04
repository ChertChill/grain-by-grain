package database;

public class TransactionStatus {
    private int statusID;
    private String name;
    private Boolean isFinal;

    public TransactionStatus(int statusID, String name, Boolean isFinal) {
        this.statusID = statusID;
        this.name = name;
        this.isFinal = isFinal;
    }

    public int getStatusID() {
        return statusID;
    }

    public void setStatusID(int statusID) {
        this.statusID = statusID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getFinal() {
        return isFinal;
    }

    public void setFinal(Boolean aFinal) {
        isFinal = aFinal;
    }
}
