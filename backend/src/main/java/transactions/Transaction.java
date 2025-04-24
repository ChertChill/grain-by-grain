package transactions;

import database.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {
    private Long transactionID;
    private int typeID;
    private int amount;
    private String comment;
    private int statusID;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
    private Long userID;
    private String accountNumber;
    private String recipientNumber;
    private String bank;
    private Long recipientTIN;
    private String phone;
    private int categoryID;
    private int legalTypeID;


    // Полный конструктор
    public Transaction(Long transactionID, int typeID, int amount, String comment, int statusID,
                       LocalDateTime transactionDate, LocalDateTime createdAt, Long userID,
                       String accountNumber, String recipientNumber, String bank, Long recipientTIN,
                       String phone, int categoryID, int legalTypeID) {
        this.transactionID = transactionID;
        this.typeID = typeID;
        this.amount = amount;
        this.comment = comment;
        this.statusID = statusID;
        this.transactionDate = transactionDate;
        this.createdAt = createdAt;
        this.userID = userID;
        this.accountNumber = accountNumber;
        this.recipientNumber = recipientNumber;
        this.bank = bank;
        this.recipientTIN = recipientTIN;
        this.phone = phone;
        this.categoryID = categoryID;
        this.legalTypeID = legalTypeID;
    }

    public Transaction() {
    }

    // Getters and Setters (optional, but recommended)
    public Long getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(Long transactionID) {
        this.transactionID = transactionID;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getStatusID() {
        return statusID;
    }

    public void setStatusID(int statusID) {
        this.statusID = statusID;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getRecipientNumber() {
        return recipientNumber;
    }

    public void setRecipientNumber(String recipientNumber) {
        this.recipientNumber = recipientNumber;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public Long getRecipientTIN() {
        return recipientTIN;
    }

    public void setRecipientTIN(Long recipientTIN) {
        this.recipientTIN = recipientTIN;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public int getLegalTypeID() {
        return legalTypeID;
    }

    public void setLegalTypeID(int legalTypeID) {
        this.legalTypeID = legalTypeID;
    }


    public void saveToDatabase() throws SQLException {
        String sql = """
            INSERT INTO transactions (
                type_id,
                amount,
                comment,
                status_id,
                transaction_date,
                created_at,
                user_id,
                account_number,
                recipient_number,
                bank,
                recipient_tin,
                phone,
                category_id,
                legal_type_id
        ) VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
        )
        """;

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            int idx = 1;
            ps.setInt    (idx++, this.getTypeID());             // type_id
            ps.setInt    (idx++, this.getAmount());             // amount
            ps.setString (idx++, this.getComment());            // comment
            ps.setInt    (idx++, this.getStatusID());           // status_id
            ps.setObject (idx++, this.getTransactionDate());    // transaction_date
            ps.setObject (idx++, this.getCreatedAt());          // created_at
            ps.setLong   (idx++, this.getUserID());             // user_id
            ps.setString (idx++, this.getAccountNumber());      // account_number
            ps.setString (idx++, this.getRecipientNumber());    // recipient_number
            ps.setString (idx++, this.getBank());               // bank
            ps.setLong    (idx++, this.getRecipientTIN());       // recipient_tin
            ps.setString (idx++, this.getPhone());              // phone
            ps.setInt    (idx++, this.getCategoryID());         // category_id
            ps.setInt    (idx++, this.getLegalTypeID());        // legal_type_id

            ps.executeUpdate();
        }
    }
}
