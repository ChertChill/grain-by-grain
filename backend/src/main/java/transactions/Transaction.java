package transactions;

import database.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class Transaction {
    private Long transactionID;
    private TransactionType type;
    private int amount;
    private String comment;
    private TransactionStatus status;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
    private Long userID;
    private String accountNumber;
    private String recipientNumber;
    private Bank bank;
    private Long recipientTIN;
    private String recipientPhone;
    private Category category;
    private LegalType legalType;


    // Полный конструктор
    public Transaction(Long transactionID, TransactionType type, int amount, String comment,
                       TransactionStatus status,
                       LocalDateTime transactionDate, LocalDateTime createdAt, Long userID,
                       String accountNumber, String recipientNumber, Bank bank, Long recipientTIN,
                       String recipientPhone, Category category, LegalType legalType) {
        this.transactionID = transactionID;
        this.type = type;
        this.amount = amount;
        this.comment = comment;
        this.status = status;
        this.transactionDate = transactionDate;
        this.createdAt = createdAt;
        this.userID = userID;
        this.accountNumber = accountNumber;
        this.recipientNumber = recipientNumber;
        this.bank = bank;
        this.recipientTIN = recipientTIN;
        this.recipientPhone = recipientPhone;
        this.category = category;
        this.legalType = legalType;
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

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType typeID) {
        this.type = typeID;
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

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
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

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bankID) {
        this.bank = bankID;
    }

    public Long getRecipientTIN() {
        return recipientTIN;
    }

    public void setRecipientTIN(Long recipientTIN) {
        this.recipientTIN = recipientTIN;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public LegalType getLegalType() {
        return legalType;
    }

    public void setLegalType(LegalType legalType) {
        this.legalType = legalType;
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
                bank_id,
                recipient_tin,
                recipient_phone,
                category_id,
                legal_type_id
        ) VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
        )
        """;

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            int idx = 1;
            ps.setInt    (idx++, this.getType().getTypeID());             // type_id
            ps.setInt    (idx++, this.getAmount());             // amount
            ps.setString (idx++, this.getComment());            // comment
            ps.setInt    (idx++, this.getStatus().getStatusID());           // status_id
            ps.setObject (idx++, this.getTransactionDate());    // transaction_date
            ps.setObject (idx++, this.getCreatedAt());          // created_at
            ps.setLong   (idx++, this.getUserID());             // user_id
            ps.setString (idx++, this.getAccountNumber());      // account_number
            ps.setString (idx++, this.getRecipientNumber());    // recipient_number
            ps.setInt (idx++, this.getBank().getBankID());               // bank
            ps.setLong    (idx++, this.getRecipientTIN());       // recipient_tin
            ps.setString (idx++, this.getRecipientPhone());              // phone
            ps.setInt    (idx++, this.getCategory().getCategoryID());         // category_id
            ps.setInt    (idx++, this.getLegalType().getLegalTypeID());        // legal_type_id

            ps.executeUpdate();
        }
    }
}
