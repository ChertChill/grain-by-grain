package database;

public class LegalType {
    private int legalTypeID;
    private String name;

    public LegalType(int legalTypeID, String name) {
        this.legalTypeID = legalTypeID;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLegalTypeID() {
        return legalTypeID;
    }

    public void setLegalTypeID(int legalTypeID) {
        this.legalTypeID = legalTypeID;
    }
}
