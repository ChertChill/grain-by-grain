package transactions;

import database.Bank;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

public class DashboardGenerator {
    private Map<String, Integer> bankSenders;
    private Map<String, Integer> bankRecipients;

    private Map<String, Integer> incomeCategoriesAmount;
    private Map<String, Integer> expenseCategoriesAmount;

    private int completedTransactions;
    private int cancelledTransactions;

    private PeriodDataAggregator weeklyAggregator;
    private PeriodDataAggregator monthlyAggregator;
    private PeriodDataAggregator quarterlyAggregator;
    private PeriodDataAggregator yearlyAggregator;

    //ТРАНЗАКЦИИ ДОЛЖНЫ БЫТЬ ОТСОРТИРОВАНЫ ПО ДАТЕ!
    public LinkedHashMap<String, Object> generateDashboards(LocalDateTime startingTimestamp, List<Transaction> originalTransactions) {
        prepareDashboards(startingTimestamp, originalTransactions);
        return createDashboards();
    }

    private void prepareDashboards(LocalDateTime startingTimestamp, List<Transaction> transactions) {
        LocalDate firstDate = startingTimestamp.toLocalDate();

        bankSenders = new HashMap<>();
        bankRecipients = new HashMap<>();

        incomeCategoriesAmount = new HashMap<>();
        expenseCategoriesAmount = new HashMap<>();

        completedTransactions = 0;
        cancelledTransactions = 0;

        //создаем агрегаторы, которые сохраняют данные для разных периодов 
        weeklyAggregator = new PeriodDataAggregator("week",
                firstDate.with(WeekFields.ISO.dayOfWeek(), 1));
        monthlyAggregator = new PeriodDataAggregator("month",
                firstDate.with(firstDate.withDayOfMonth(1)));
        int quarterStartMonth = ((firstDate.getMonthValue() - 1) / 3) * 3 + 1;
        quarterlyAggregator = new PeriodDataAggregator("quarter",
                LocalDate.of(firstDate.getYear(), quarterStartMonth, 1));
        yearlyAggregator = new PeriodDataAggregator("year",
                LocalDate.of(firstDate.getYear(), 1, 1));

        for (Transaction tx : transactions) {
            LocalDate txDate = tx.getTransactionDate().toLocalDate();

            // вычисляем к какой неделе, месяцу, кварталу и году принадлежит транзакция 
            LocalDate txWeekStart = txDate.with(WeekFields.ISO.dayOfWeek(), 1);
            LocalDate txMonthStart = txDate.withDayOfMonth(1);
            int txQuarterStartMonth = ((txDate.getMonthValue() - 1) / 3) * 3 + 1;
            LocalDate txQuarterStart = LocalDate.of(txDate.getYear(), txQuarterStartMonth, 1);
            LocalDate txYearStart = LocalDate.of(txDate.getYear(), 1, 1);

            int type = tx.getType().getTypeID();

            //добавляем транзакцию в отмененную/завершенную
            if (tx.getStatus().getStatusID() == 0) cancelledTransactions++;
            else if (tx.getStatus().getFinal()) completedTransactions++;

            //добавляем транзакции для дэшборда банков
            Bank bankSender = tx.getSenderBank();
            if (!bankSenders.containsKey(bankSender.getName()))
                bankSenders.put(bankSender.getName(), 0);
            bankSenders.replace(bankSender.getName(), bankSenders.get(bankSender.getName()) + tx.getAmount());

            Bank bankRecipient = tx.getRecipientBank();
            if (!bankRecipients.containsKey(bankRecipient.getName()))
                bankRecipients.put(bankRecipient.getName(), 0);
            bankRecipients.replace(bankRecipient.getName(), bankRecipients.get(bankRecipient.getName()) + tx.getAmount());

            // заполняем агрегаторы данными, учитывая количество транзакций в период и добавляя '0' если их нет 
            while (txWeekStart.isAfter(weeklyAggregator.getPeriodStart())) {
                weeklyAggregator.flushData();
                weeklyAggregator.setPeriodStart(weeklyAggregator.getPeriodStart().plusWeeks(1));
            }

            while (txMonthStart.isAfter(monthlyAggregator.getPeriodStart())) {
                monthlyAggregator.flushData();
                monthlyAggregator.setPeriodStart(monthlyAggregator.getPeriodStart().plusMonths(1));
            }

            while (txQuarterStart.isAfter(quarterlyAggregator.getPeriodStart())) {
                quarterlyAggregator.flushData();
                quarterlyAggregator.setPeriodStart(quarterlyAggregator.getPeriodStart().plusMonths(3));
            }

            while (txYearStart.isAfter(yearlyAggregator.getPeriodStart())) {
                yearlyAggregator.flushData();
                yearlyAggregator.setPeriodStart(yearlyAggregator.getPeriodStart().plusYears(1));
            }

            // увеличиваем количество транзакций в текущем периоде
            weeklyAggregator.increaseTransactionCount();
            monthlyAggregator.increaseTransactionCount();
            quarterlyAggregator.increaseTransactionCount();
            yearlyAggregator.increaseTransactionCount();

            //увеличиваем текущую информацию по доходам/расходам
            if (type == 1) {
                weeklyAggregator.increaseIncomeCount();
                weeklyAggregator.increaseIncomeAmount(tx.getAmount());
                monthlyAggregator.increaseIncomeCount();
                monthlyAggregator.increaseIncomeAmount(tx.getAmount());
                quarterlyAggregator.increaseIncomeCount();
                quarterlyAggregator.increaseIncomeAmount(tx.getAmount());
                yearlyAggregator.increaseIncomeCount();
                yearlyAggregator.increaseIncomeAmount(tx.getAmount());

                String categoryName = tx.getCategory().getName();
                if (!incomeCategoriesAmount.containsKey(categoryName)) incomeCategoriesAmount.put(categoryName, 0);
                incomeCategoriesAmount.replace(categoryName, incomeCategoriesAmount.get(categoryName) + tx.getAmount());
            }
            else {
                weeklyAggregator.increaseExpenseCount();
                weeklyAggregator.increaseExpenseAmount(tx.getAmount());
                monthlyAggregator.increaseExpenseCount();
                monthlyAggregator.increaseExpenseAmount(tx.getAmount());
                quarterlyAggregator.increaseExpenseCount();
                quarterlyAggregator.increaseExpenseAmount(tx.getAmount());
                yearlyAggregator.increaseExpenseCount();
                yearlyAggregator.increaseExpenseAmount(tx.getAmount());

                String categoryName = tx.getCategory().getName();
                if (!expenseCategoriesAmount.containsKey(categoryName)) expenseCategoriesAmount.put(categoryName, 0);
                expenseCategoriesAmount.replace(categoryName, expenseCategoriesAmount.get(categoryName) + tx.getAmount());
            }
        }

        //обновляем еще раз агрегаторы на случай, если последняя транзакция не была концом периода
        weeklyAggregator.flushData();
        monthlyAggregator.flushData();
        quarterlyAggregator.flushData();
        yearlyAggregator.flushData();
    }

    private LinkedHashMap<String, Object> createDashboards() {
        LinkedHashMap<String, List<List<String>>> firstDashboard = new LinkedHashMap<>();
        LinkedHashMap<String, List<List<String>>> secondDashboard = new LinkedHashMap<>();
        LinkedHashMap<String, List<List<String>>> thirdDashboard = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> fourthDashboard = new LinkedHashMap<>();
        LinkedHashMap<String, Map<String, Integer>> fifthDashboard = new LinkedHashMap<>();
        LinkedHashMap<String, Map<String, Integer>> sixthDashboard = new LinkedHashMap<>();
        firstDashboard.put("weekly", Arrays.asList(weeklyAggregator.getLabels(), weeklyAggregator.getTransactionCounts()));
        firstDashboard.put("monthly", Arrays.asList(monthlyAggregator.getLabels(), monthlyAggregator.getTransactionCounts()));
        firstDashboard.put("quarterly", Arrays.asList(quarterlyAggregator.getLabels(), quarterlyAggregator.getTransactionCounts()));
        firstDashboard.put("yearly", Arrays.asList(yearlyAggregator.getLabels(), yearlyAggregator.getTransactionCounts()));
        secondDashboard.put("weekly", Arrays.asList(weeklyAggregator.getLabels(), weeklyAggregator.getIncomeCounts(), weeklyAggregator.getExpensesCounts()));
        secondDashboard.put("monthly", Arrays.asList(monthlyAggregator.getLabels(), monthlyAggregator.getIncomeCounts(), monthlyAggregator.getExpensesCounts()));
        secondDashboard.put("quarterly", Arrays.asList(quarterlyAggregator.getLabels(), quarterlyAggregator.getIncomeCounts(), quarterlyAggregator.getExpensesCounts()));
        secondDashboard.put("yearly", Arrays.asList(yearlyAggregator.getLabels(), yearlyAggregator.getIncomeCounts(), yearlyAggregator.getExpensesCounts()));
        thirdDashboard.put("weekly", Arrays.asList(weeklyAggregator.getLabels(), weeklyAggregator.getIncomeAmounts(), weeklyAggregator.getExpenseAmounts()));
        thirdDashboard.put("monthly", Arrays.asList(monthlyAggregator.getLabels(), monthlyAggregator.getIncomeAmounts(), monthlyAggregator.getExpenseAmounts()));
        thirdDashboard.put("quarterly", Arrays.asList(quarterlyAggregator.getLabels(), quarterlyAggregator.getIncomeAmounts(), quarterlyAggregator.getExpenseAmounts()));
        thirdDashboard.put("yearly", Arrays.asList(yearlyAggregator.getLabels(), yearlyAggregator.getIncomeAmounts(), yearlyAggregator.getExpenseAmounts()));
        fourthDashboard.put("Completed Transactions:", completedTransactions);
        fourthDashboard.put("Cancelled Transactions:", cancelledTransactions);
        fifthDashboard.put("BankSenders:", bankSenders);
        fifthDashboard.put("BankReceivers:", bankRecipients);
        sixthDashboard.put("Income Categories:", incomeCategoriesAmount);
        sixthDashboard.put("Expenses Categories:", expenseCategoriesAmount);
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("Dashboard 1:", firstDashboard);
        result.put("Dashboard 2:", secondDashboard);
        result.put("Dashboard 3:", thirdDashboard);
        result.put("Dashboard 4", fourthDashboard);
        result.put("Dashboard 5:", fifthDashboard);
        result.put("Dashboard 6:", sixthDashboard);
        return result;
    }

    private static class PeriodDataAggregator {
        private final List<String> labels = new ArrayList<>();
        private final List<String> transactionCounts = new ArrayList<>();
        private final List<String> incomeCounts = new ArrayList<>();
        private final List<String> expensesCounts = new ArrayList<>();
        private final List<String> incomeAmounts = new ArrayList<>();
        private final List<String> expenseAmounts = new ArrayList<>();

        private LocalDate periodStart;

        private int currentTransactionCount = 0;
        private int currentIncomeCount = 0;
        private int currentExpensesCount = 0;
        private int currentIncomeAmount = 0;
        private int currentExpenseAmount = 0;

        String period;

        private PeriodDataAggregator(String period, LocalDate periodStart) {
            this.period = period;
            this.periodStart = periodStart;
        }

        public void flushData() {
            addLabel();
            transactionCounts.add(String.valueOf(currentTransactionCount));
            incomeCounts.add(String.valueOf(currentIncomeCount));
            expensesCounts.add(String.valueOf(currentExpensesCount));
            incomeAmounts.add(String.valueOf(currentIncomeAmount));
            expenseAmounts.add(String.valueOf(currentExpenseAmount));

            currentTransactionCount = 0;
            currentIncomeCount = 0;
            currentExpensesCount = 0;
            currentIncomeAmount = 0;
            currentExpenseAmount = 0;
        }

        public void increaseTransactionCount() { currentTransactionCount++; }
        public void increaseIncomeCount() { currentIncomeCount++; }
        public void increaseExpenseCount() { currentExpensesCount++; }
        public void increaseIncomeAmount(int amount) { currentIncomeAmount += amount; }
        public void increaseExpenseAmount(int amount) { currentExpenseAmount += amount; }

        public List<String> getLabels() {
            return labels;
        }

        public List<String> getTransactionCounts() {
            return transactionCounts;
        }

        public List<String> getIncomeCounts() {
            return incomeCounts;
        }

        public List<String> getExpensesCounts() {
            return expensesCounts;
        }

        public List<String> getIncomeAmounts() {
            return incomeAmounts;
        }

        public List<String> getExpenseAmounts() {
            return expenseAmounts;
        }

        private LocalDate getPeriodStart() {
            return periodStart;
        }

        private void setPeriodStart(LocalDate periodStart) {
            this.periodStart = periodStart;
        }

        private void addLabel() {
            DateTimeFormatter formatter;
            switch (period) {
                case "week": {
                    formatter = DateTimeFormatter.ofPattern("MM-dd", new Locale("ru"));
                    labels.add("неделя " + periodStart.format(formatter));
                    break;
                }
                case "month": {
                    formatter = DateTimeFormatter.ofPattern("MMM", new Locale("ru"));
                    labels.add(periodStart.format(formatter));
                    break;
                }
                case "quarter": {
                    int quarter = (periodStart.getMonthValue() - 1) / 3 + 1;
                    labels.add("Q" + quarter + " " + periodStart.getYear());
                    break;
                }
                case "year": {
                    formatter = DateTimeFormatter.ofPattern("yyyy", new Locale("ru"));
                    labels.add(periodStart.format(formatter));
                    break;
                }
            }
        }
    }
}
