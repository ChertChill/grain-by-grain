package transactions;

import database.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {
    private Long transactionId;
    private Long userId;
    private Long personTypeId;
    private LocalDateTime transactionDate;
    private Long typeId;
    private String comment;
    private Double amount;
    private Long statusId;
    private Long senderBankId;
    private String accountNumber;
    private Long RecieverBankId;
    private String RecieverInn;
    private String RecieverAccount;
    private Long categoryId;
    private String RecieverPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Полный конструктор
    public Transaction(Long transactionId, Long userId, Long personTypeId, LocalDateTime transactionDate,
                       Long typeId, String comment, Double amount, Long statusId, Long senderBankId,
                       String accountNumber, Long RecieverBankId, String RecieverInn,
                       String RecieverAccount, Long categoryId, String RecieverPhone,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.personTypeId = personTypeId;
        this.transactionDate = transactionDate;
        this.typeId = typeId;
        this.comment = comment;
        this.amount = amount;
        this.statusId = statusId;
        this.senderBankId = senderBankId;
        this.accountNumber = accountNumber;
        this.RecieverBankId = RecieverBankId;
        this.RecieverInn = RecieverInn;
        this.RecieverAccount = RecieverAccount;
        this.categoryId = categoryId;
        this.RecieverPhone = RecieverPhone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Transaction() {
    }

    // Геттеры и сеттеры
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPersonTypeId() {
        return personTypeId;
    }

    public void setPersonTypeId(Long personTypeId) {
        this.personTypeId = personTypeId;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public Long getSenderBankId() {
        return senderBankId;
    }

    public void setSenderBankId(Long senderBankId) {
        this.senderBankId = senderBankId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Long getRecieverBankId() {
        return RecieverBankId;
    }

    public void setRecieverBankId(Long RecieverBankId) {
        this.RecieverBankId = RecieverBankId;
    }

    public String getRecieverInn() {
        return RecieverInn;
    }

    public void setRecieverInn(String RecieverInn) {
        this.RecieverInn = RecieverInn;
    }

    public String getRecieverAccount() {
        return RecieverAccount;
    }

    public void setRecieverAccount(String RecieverAccount) {
        this.RecieverAccount = RecieverAccount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getRecieverPhone() {
        return RecieverPhone;
    }

    public void setRecieverPhone(String RecieverPhone) {
        this.RecieverPhone = RecieverPhone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // equals, hashCode и toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(personTypeId, that.personTypeId) &&
                Objects.equals(transactionDate, that.transactionDate) &&
                Objects.equals(typeId, that.typeId) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(statusId, that.statusId) &&
                Objects.equals(senderBankId, that.senderBankId) &&
                Objects.equals(accountNumber, that.accountNumber) &&
                Objects.equals(RecieverBankId, that.RecieverBankId) &&
                Objects.equals(RecieverInn, that.RecieverInn) &&
                Objects.equals(RecieverAccount, that.RecieverAccount) &&
                Objects.equals(categoryId, that.categoryId) &&
                Objects.equals(RecieverPhone, that.RecieverPhone) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, userId, personTypeId, transactionDate, typeId, comment,
                amount, statusId, senderBankId, accountNumber, RecieverBankId,
                RecieverInn, RecieverAccount, categoryId, RecieverPhone, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", userId=" + userId +
                ", personTypeId=" + personTypeId +
                ", transactionDate=" + transactionDate +
                ", typeId=" + typeId +
                ", comment='" + comment + '\'' +
                ", amount=" + amount +
                ", statusId=" + statusId +
                ", senderBankId=" + senderBankId +
                ", accountNumber='" + accountNumber + '\'' +
                ", RecieverBankId=" + RecieverBankId +
                ", RecieverInn='" + RecieverInn + '\'' +
                ", RecieverAccount='" + RecieverAccount + '\'' +
                ", categoryId=" + categoryId +
                ", RecieverPhone='" + RecieverPhone + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public void insertTransaction() throws SQLException {
        String sql = """
        INSERT INTO transactions (
            transaction_id,
            user_id,
            user_type_id,
            operation_date,
            transaction_type_id,
            amount,
            status_id,
            sender_bank_id,
            sender_account_id,
            receiver_bank_id,
            receiver_inn,
            receiver_account,
            receiver_phone,
            category_id,
            comment,
            created_at,
            updated_at
        ) VALUES (
            ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
        )
        """;

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            int idx = 1;
            ps.setLong      (idx++, this.getTransactionId());      // transaction_id
            ps.setLong      (idx++, this.getUserId());             // user_id
            ps.setLong      (idx++, this.getPersonTypeId());       // user_type_id
            ps.setObject    (idx++, this.getTransactionDate());    // operation_date
            ps.setLong      (idx++, this.getTypeId());             // transaction_type_id
            ps.setDouble    (idx++, this.getAmount());             // amount
            ps.setLong      (idx++, this.getStatusId());           // status_id
            ps.setLong      (idx++, this.getSenderBankId());       // sender_bank_id
            ps.setString    (idx++, this.getAccountNumber());      // sender_account_id
            ps.setLong      (idx++, this.getRecieverBankId());     // receiver_bank_id
            ps.setString    (idx++, this.getRecieverInn());        // receiver_inn
            ps.setString    (idx++, this.getRecieverAccount());    // receiver_account
            ps.setString    (idx++, this.getRecieverPhone());      // receiver_phone
            ps.setLong      (idx++, this.getCategoryId());         // category_id
            ps.setString    (idx++, this.getComment());            // comment
            ps.setObject    (idx++, this.getCreatedAt());          // created_at
            ps.setObject    (idx++, this.getUpdatedAt());          // updated_at


            ps.executeUpdate();
        }
    }
}
