package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {
    private static Connection connection;

    private static List<Bank> banks;
    private static List<Category> categories;
    private static List<LegalType> legalTypes;
    private static List<TransactionStatus> transactionStatuses;
    private static List<TransactionType> transactionTypes;

    public static void loadEverything() throws SQLException {
        connection = DatabaseConnection.getConnection();
        banks = loadBanks();
        categories = loadCategories();
        legalTypes = loadLegalTypes();
        transactionStatuses = loadTransactionStatuses();
        transactionTypes = loadTransactionTypes();

    }

    public static List<TransactionType> loadTransactionTypes() throws SQLException {
        String query = "select * from transaction_types";
        List<TransactionType> types = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                types.add(new TransactionType(rs.getInt("type_id"),
                        rs.getString("name")));
            }
            return types;
        }
    }

    private static List<Category> loadCategories() throws SQLException {
        String query = "SELECT * FROM categories";
        List<Category> categories = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("category_id"),
                        rs.getString("name")
                ));
            }
        }

        return categories;
    }

    private static List<LegalType> loadLegalTypes() throws SQLException {
        String query = "select * from legal_types";
        List<LegalType> legalTypes = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                legalTypes.add(new LegalType(rs.getInt("legal_type_id"), rs.getString("name")));
            }
            return legalTypes;
        }
    }

    private static List<TransactionStatus> loadTransactionStatuses() throws SQLException {
        String query = "SELECT * FROM transaction_status";
        List<TransactionStatus> transactionStatuses = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                transactionStatuses.add(new TransactionStatus(
                        rs.getInt("status_id"),
                        rs.getString("name"),
                        rs.getBoolean("is_final")
                ));
            }
        }

        return transactionStatuses;
    }

    private static List<Bank> loadBanks() throws SQLException {
        String query = "SELECT * FROM banks";
        List<Bank> banks = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                banks.add(new Bank(
                        rs.getInt("bank_id"),
                        rs.getString("name"),
                        rs.getString("bic"),
                        rs.getString("address")
                ));
            }
        }

        return banks;
    }

    public static List<Bank> getBanks() {
        return banks;
    }

    public static List<Category> getCategories() {
        return categories;
    }

    public static List<TransactionStatus> getTransactionStatuses() {
        return transactionStatuses;
    }

    public static void setBanks(List<Bank> banks) {
        DataLoader.banks = banks;
    }

    public static Bank getBankByID(int bankID) throws DataLoadingException {
        for (Bank bank : banks) {
            if (bank.getBankID() == bankID) return bank;
        }
        throw new DataLoadingException("Bank not found");
    }

    public static Category getCategoryByID(int categoryID) throws DataLoadingException {
        for (Category category : categories) {
            if (category.getCategoryID() == categoryID) return category;
        }
        throw new DataLoadingException("Category not found");
    }

    public static LegalType getLegalTypeByID(int legalTypeID) throws DataLoadingException {
        for (LegalType legalType : legalTypes) {
            if (legalType.getLegalTypeID() == legalTypeID) return legalType;
        }
        throw new DataLoadingException("Legal type not found");
    }
    public static TransactionStatus getTransactionStatusByID(int transactionStatusID) throws DataLoadingException {
        for (TransactionStatus transactionStatus : transactionStatuses) {
            if (transactionStatus.getStatusID() == transactionStatusID) return transactionStatus;
        }
        throw new DataLoadingException("TransactionStatus not found");
    }
    public static TransactionType getTransactionTypeByID(int transactionTypeID) throws DataLoadingException {
        for (TransactionType transactionType : transactionTypes) {
            if (transactionType.getTypeID() == transactionTypeID) return transactionType;
        }
        throw new DataLoadingException("TransactionStatus not found");
    }
}
