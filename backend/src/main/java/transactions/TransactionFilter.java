package transactions;

import authorization.User;
import database.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.*;

public class TransactionFilter {
    public List<Transaction> getUserTransactions(User user, Map<String, List<String>> filters) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT * FROM transactions WHERE user_id = ?");
        List<Object> parameters = new ArrayList<>();
        List<Transaction> transactions = new ArrayList<>();
        parameters.add(user.getId());

        if (filters.containsKey("amount")) {
            query.append(" AND amount = ?");
            parameters.add(Integer.valueOf(filters.get("amount").getFirst()));
        }

        if (filters.containsKey("status_id")) {
            query.append(" AND status_id = ?");
            parameters.add(Integer.valueOf(filters.get("status_id").getFirst()));
        }

        if (filters.containsKey("bank")) {
            query.append(" AND bank = ?");
            parameters.add(filters.get("bank").getFirst());
        }

        if (filters.containsKey("amount_gt")) {
            query.append(" AND amount >= ?");
            parameters.add(Integer.valueOf(filters.get("amount_gt").getFirst()));
        }

        if (filters.containsKey("amount_lw")) {
            query.append(" AND amount <= ?");
            parameters.add(Integer.valueOf(filters.get("amount_lw").getFirst()));
        }

        try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(query.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i)); // Bind values safely
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
            return transactions;
        }
    }

//    public void getTransactionsByTime(List<Transaction> transactions) {
//        Map<String, Long> debitTransactions = new LinkedHashMap<>();
//        Map<String, Long> creditTransactions = new LinkedHashMap<>();
//        List<Integer> periodTransactions = new ArrayList<>();
////        List<Integer> weeklyTransactions = new ArrayList<>();
////        List<Integer> monthlyTransactions = new ArrayList<>();
////        List<Integer> quartleryTransactions = new ArrayList<>();
////        List<Integer> yearlyTransactions = new ArrayList<>();
//        HashMap<LocalDateTime, Integer> transactionDates = new HashMap<>();
//        LocalDateTime first = null;
//        LocalDateTime last = null;
//        LocalDateTime current = null;
////        int currentWeekTransactions = 0;
////        int currentMonthTransactions = 0;
////        int currentQuarterTransactions = 0;
////        int currentYearTransactions = 0;
////        int currentDebitTransactions = 0;
////        int currentCreditTransactions = 0;
//        boolean isMonday = false;
//        Month lastMonth = null;
//        int lastYear = 0;
//        for (Transaction transaction : transactions) {
//            transactionDates.put(transaction.getTransactionDate(), transaction.getTypeID());
//        }
//
//        Collections.sort(transactionDates);
//        first = transactionDates.getFirst();
//        current = first;
//        last = transactionDates.getLast();
//
//        //запускаем луп до последнего дня+1 (чтобы последняя дата записалась в массив)
//        while (current.isBefore(last.plusDays(1))) {
//            if (transactionDates.containsKey(current)) {
//
//            }
//        }
//            /*
//        while (current.isBefore(last.plusDays(1))) {
//            if (lastYear == 0L) lastYear = current.getYear();
//            else if (current.getYear() != lastYear) {
//                lastYear = current.getYear();
//                yearlyTransactions.add(currentYearTransactions);
//                currentYearTransactions = 0;
//            }
//
//            if ((current.getMonth() == Month.APRIL
//                    || current.getMonth() == Month.JULY
//                    || current.getMonth() == Month.OCTOBER) && current.getDayOfMonth() == 1) {
//                if (current.getMonth() != lastMonth) {
//                    quartleryTransactions.add(currentQuarterTransactions);
//                    currentQuarterTransactions = 0;
//                }
//            }
//
//            if (lastMonth == null) lastMonth = current.getMonth();
//            else if (current.getMonth() != lastMonth) {
//                lastMonth = current.getMonth();
//                monthlyTransactions.add(currentMonthTransactions);
//                debitTransactions.put(lastMonth.getDisplayName(3, Locale.ENGLISH));
//                creditTransactions.put(lastMonth.getDisplayName(3, Locale.ENGLISH));
//                currentMonthTransactions = 0;
//                currentDebitTransactions = 0;
//                currentCreditTransactions = 0;
//            }
//            if (current.getDayOfWeek() == DayOfWeek.MONDAY && !current.isEqual(first)) {
//                if (!isMonday) {
//                    isMonday = true;
//                    weeklyTransactions.add(currentWeekTransactions);
//                    currentWeekTransactions = 0;
//                }
//            } else if (isMonday) {
//                isMonday = false;
//            }
//
//            if (transactionDates.contains(current)) {
//                currentWeekTransactions++;
//                currentMonthTransactions++;
//                currentQuarterTransactions++;
//                currentYearTransactions++;
//                if
//            }
//            current = current.plusDays(1);
//            if (current.isAfter(last)) {
//                weeklyTransactions.add(currentWeekTransactions);
//                monthlyTransactions.add(currentMonthTransactions);
//                quartleryTransactions.add(currentQuarterTransactions);
//                yearlyTransactions.add(currentYearTransactions);
//            }
//        }
//        */
//    }

    private Transaction mapResultSetToTransaction(ResultSet resultSet) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setTransactionID(resultSet.getLong("transaction_id"));  // transaction_id

        transaction.setUserID(resultSet.getLong("user_id"));  // user_id

        transaction.setLegalTypeID(resultSet.getInt("legal_type_id"));  // person_type_id (assuming this is legalTypeID)

        transaction.setTransactionDate(resultSet.getTimestamp("transaction_date").toLocalDateTime());  // transaction_date

        transaction.setTypeID(resultSet.getInt("type_id"));  // type_id

        transaction.setAmount(resultSet.getInt("amount"));  // amount (assuming it's stored as an integer)

        transaction.setStatusID(resultSet.getInt("status_id"));  // status_id

        transaction.setBank(resultSet.getString("bank"));  // bank (assuming sender_bank_id is the bank column)

        transaction.setAccountNumber(resultSet.getString("account_number"));  // account_number

        transaction.setRecipientNumber(resultSet.getString("recipient_number"));  // Recipient_number (assuming Reciiver_bank_id maps to recipient number)

        transaction.setRecipientTIN(resultSet.getLong("recipient_tin"));  // Recipient_tin (Reciiver_inn)

        transaction.setRecipientPhone(resultSet.getString("recipient_phone"));  // Reciiver_phone (phone)

        transaction.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());  // created_at

        transaction.setCategoryID(resultSet.getInt("category_id"));  // category_id

        transaction.setComment(resultSet.getString("comment"));  // comment


        return transaction;
    }
}
