package database;

public class TransactionType {
    private int typeID;
    private String name;

    public TransactionType(int typeID, String name) {
        this.typeID = typeID;
        this.name = name;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
