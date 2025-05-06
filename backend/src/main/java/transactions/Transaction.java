package transactions;

import database.*;
import authorization.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

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
    private Bank senderBank;
    private Bank recipientBank;
    private Long recipientTIN;
    private String recipientPhone;
    private Category category;
    private LegalType legalType;


    // Полный конструктор
    public Transaction(Long transactionID, TransactionType type, int amount, String comment,
                       TransactionStatus status,
                       LocalDateTime transactionDate, LocalDateTime createdAt, Long userID,
                       String accountNumber, String recipientNumber, Bank senderBank, Bank recipientBank, Long recipientTIN,
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
        this.senderBank = senderBank;
        this.recipientBank = recipientBank;
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

    public Bank getSenderBank() {
        return senderBank;
    }

    public void setSenderBank(Bank bankID) {
        this.senderBank = bankID;
    }

    public Bank getRecipientBank() { return recipientBank; }

    public void setRecipientBank(Bank bankID) { this.recipientBank = bankID; }

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

    /**
     * Создает новую транзакцию из данных запроса
     * @param requestBody данные запроса
     * @param currentUser текущий пользователь
     * @return созданная транзакция
     * @throws Exception если возникла ошибка при создании транзакции
     */
    public static Transaction createFromRequest(Map<String, Object> requestBody, User currentUser) throws Exception {
        Transaction transaction = new Transaction();
        
        transaction.setType(DataLoader.getTransactionTypeByID((Integer) requestBody.get("type")));
        transaction.setAmount((Integer) requestBody.get("amount"));
        transaction.setComment((String) requestBody.get("comment"));
        transaction.setStatus(DataLoader.getTransactionStatusByID((Integer) requestBody.get("status")));
        transaction.setTransactionDate(LocalDateTime.parse((String) requestBody.get("transactionDate")));
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUserID(currentUser.getId());
        transaction.setAccountNumber((String) requestBody.get("accountNumber"));
        transaction.setRecipientNumber((String) requestBody.get("recipientNumber"));
        transaction.setSenderBank(DataLoader.getBankByID((Integer) requestBody.get("senderBank")));
        transaction.setRecipientBank(DataLoader.getBankByID((Integer) requestBody.get("recipientBank")));
        transaction.setRecipientTIN(Long.parseLong(requestBody.get("recipientTIN").toString()));
        transaction.setRecipientPhone((String) requestBody.get("recipientPhone"));
        transaction.setCategory(DataLoader.getCategoryByID((Integer) requestBody.get("category")));
        transaction.setLegalType(DataLoader.getLegalTypeByID((Integer) requestBody.get("legalType")));
        
        return transaction;
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
                sender_bank_id,
                recipient_bank_id,
                recipient_tin,
                recipient_phone,
                category_id,
                legal_type_id
        ) VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
        )
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
            ps.setInt    (idx++, this.getSenderBank().getBankID());               // senderBankID
            ps.setInt    (idx++, this.getRecipientBank().getBankID());
            ps.setLong   (idx++, this.getRecipientTIN());       // recipient_tin
            ps.setString (idx++, this.getRecipientPhone());              // phone
            ps.setInt    (idx++, this.getCategory().getCategoryID());         // category_id
            ps.setInt    (idx++, this.getLegalType().getLegalTypeID());

            ps.executeUpdate();
        }
    }

    /**
     * Обновляет существующую транзакцию из данных запроса
     * @param transactionId ID транзакции для обновления
     * @param requestBody данные запроса
     * @param currentUser текущий пользователь
     * @return обновленная транзакция
     * @throws Exception если возникла ошибка при обновлении транзакции
     */
    public static Transaction updateFromRequest(String transactionId, Map<String, Object> requestBody, User currentUser) throws Exception {
        Transaction transaction = new Transaction();
        transaction.setTransactionID(Long.parseLong(transactionId));
        
        transaction.setType(DataLoader.getTransactionTypeByID((Integer) requestBody.get("type")));
        transaction.setAmount((Integer) requestBody.get("amount"));
        transaction.setComment((String) requestBody.get("comment"));
        transaction.setStatus(DataLoader.getTransactionStatusByID((Integer) requestBody.get("status")));
        transaction.setTransactionDate(LocalDateTime.parse((String) requestBody.get("transactionDate")));
        transaction.setUserID(currentUser.getId());
        transaction.setAccountNumber((String) requestBody.get("accountNumber"));
        transaction.setRecipientNumber((String) requestBody.get("recipientNumber"));
        transaction.setSenderBank(DataLoader.getBankByID((Integer) requestBody.get("senderBank")));
        transaction.setRecipientBank(DataLoader.getBankByID((Integer) requestBody.get("recipientBank")));
        transaction.setRecipientTIN(Long.parseLong(requestBody.get("recipientTIN").toString()));
        transaction.setRecipientPhone((String) requestBody.get("recipientPhone"));
        transaction.setCategory(DataLoader.getCategoryByID((Integer) requestBody.get("category")));
        transaction.setLegalType(DataLoader.getLegalTypeByID((Integer) requestBody.get("legalType")));
        
        return transaction;
    }

    /**
     * Обновляет транзакцию в базе данных
     * @throws SQLException если возникла ошибка при обновлении в базе данных
     */
    public void updateInDatabase() throws SQLException {
        String sql = """
            UPDATE transactions SET
                type_id = ?,
                amount = ?,
                comment = ?,
                status_id = ?,
                transaction_date = ?,
                account_number = ?,
                recipient_number = ?,
                sender_bank_id = ?,
                recipient_bank_id = ?,
                recipient_tin = ?,
                recipient_phone = ?,
                category_id = ?,
                legal_type_id = ?
            WHERE transaction_id = ? AND user_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt    (idx++, this.getType().getTypeID());             // type_id
            ps.setInt    (idx++, this.getAmount());                       // amount
            ps.setString (idx++, this.getComment());                      // comment
            ps.setInt    (idx++, this.getStatus().getStatusID());         // status_id
            ps.setObject (idx++, this.getTransactionDate());              // transaction_date
            ps.setString (idx++, this.getAccountNumber());                // account_number
            ps.setString (idx++, this.getRecipientNumber());              // recipient_number
            ps.setInt    (idx++, this.getSenderBank().getBankID());       // sender_bank_id
            ps.setInt    (idx++, this.getRecipientBank().getBankID());    // recipient_bank_id
            ps.setLong   (idx++, this.getRecipientTIN());                 // recipient_tin
            ps.setString (idx++, this.getRecipientPhone());               // recipient_phone
            ps.setInt    (idx++, this.getCategory().getCategoryID());     // category_id
            ps.setInt    (idx++, this.getLegalType().getLegalTypeID());   // legal_type_id
            ps.setLong   (idx++, this.getTransactionID());                // transaction_id
            ps.setLong   (idx++, this.getUserID());                       // user_id

            int updatedRows = ps.executeUpdate();
            if (updatedRows == 0) {
                throw new SQLException("Transaction not found or user does not have permission to update it");
            }
        }
    }

    /**
     * Получает транзакцию по ID
     * @param transactionId ID транзакции
     * @return транзакция или null, если не найдена
     * @throws SQLException если возникла ошибка при получении из базы данных
     */
    public static Transaction getById(String transactionId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, Long.parseLong(transactionId));
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Transaction transaction = new Transaction();
                    transaction.setTransactionID(rs.getLong("transaction_id"));
                    transaction.setType(DataLoader.getTransactionTypeByID(rs.getInt("type_id")));
                    transaction.setAmount(rs.getInt("amount"));
                    transaction.setComment(rs.getString("comment"));
                    transaction.setStatus(DataLoader.getTransactionStatusByID(rs.getInt("status_id")));
                    transaction.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
                    transaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    transaction.setUserID(rs.getLong("user_id"));
                    transaction.setAccountNumber(rs.getString("account_number"));
                    transaction.setRecipientNumber(rs.getString("recipient_number"));
                    transaction.setSenderBank(DataLoader.getBankByID(rs.getInt("sender_bank_id")));
                    transaction.setRecipientBank(DataLoader.getBankByID(rs.getInt("recipient_bank_id")));
                    transaction.setRecipientTIN(rs.getLong("recipient_tin"));
                    transaction.setRecipientPhone(rs.getString("recipient_phone"));
                    transaction.setCategory(DataLoader.getCategoryByID(rs.getInt("category_id")));
                    transaction.setLegalType(DataLoader.getLegalTypeByID(rs.getInt("legal_type_id")));
                    return transaction;
                }
            }
        }
        return null;
    }
}
