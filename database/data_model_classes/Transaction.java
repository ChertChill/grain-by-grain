
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
    private Long recipientBankId;
    private String recipientInn;
    private String recipientAccount;
    private Long categoryId;
    private String recipientPhone;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;

    // Полный конструктор
    public Transaction(Long transactionId, Long userId, Long personTypeId, LocalDateTime transactionDate, 
                      Long typeId, String comment, Double amount, Long statusId, Long senderBankId, 
                      String accountNumber, Long recipientBankId, String recipientInn, 
                      String recipientAccount, Long categoryId, String recipientPhone, 
                      LocalDateTime createdAt, Long createdBy, LocalDateTime updatedAt, Long updatedBy) {
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
        this.recipientBankId = recipientBankId;
        this.recipientInn = recipientInn;
        this.recipientAccount = recipientAccount;
        this.categoryId = categoryId;
        this.recipientPhone = recipientPhone;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
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

    public Long getRecipientBankId() {
        return recipientBankId;
    }

    public void setRecipientBankId(Long recipientBankId) {
        this.recipientBankId = recipientBankId;
    }

    public String getRecipientInn() {
        return recipientInn;
    }

    public void setRecipientInn(String recipientInn) {
        this.recipientInn = recipientInn;
    }

    public String getRecipientAccount() {
        return recipientAccount;
    }

    public void setRecipientAccount(String recipientAccount) {
        this.recipientAccount = recipientAccount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
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
                Objects.equals(recipientBankId, that.recipientBankId) &&
                Objects.equals(recipientInn, that.recipientInn) &&
                Objects.equals(recipientAccount, that.recipientAccount) &&
                Objects.equals(categoryId, that.categoryId) &&
                Objects.equals(recipientPhone, that.recipientPhone) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(updatedBy, that.updatedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, userId, personTypeId, transactionDate, typeId, comment, 
                          amount, statusId, senderBankId, accountNumber, recipientBankId, 
                          recipientInn, recipientAccount, categoryId, recipientPhone, createdAt, 
                          createdBy, updatedAt, updatedBy);
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
                ", recipientBankId=" + recipientBankId +
                ", recipientInn='" + recipientInn + '\'' +
                ", recipientAccount='" + recipientAccount + '\'' +
                ", categoryId=" + categoryId +
                ", recipientPhone='" + recipientPhone + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy=" + createdBy +
                ", updatedAt=" + updatedAt +
                ", updatedBy=" + updatedBy +
                '}';
    }
}