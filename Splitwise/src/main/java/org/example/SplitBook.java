package org.example;


import java.util.*;

class User {

    private String id;

    public String getDisplayName() {
        return displayName;
    }

    private String displayName;

    public User(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }
}

class UserService {

    HashMap<String, User> userDb = new HashMap<>();

    public void registerUser(String id, String name) {
        userDb.put(id, new User(id, name));
    }

    public boolean isUserRegistered(String id) {
        return userDb.containsKey(id);
    }

    public String getUserName(String id) {
        return userDb.get(id).getDisplayName();
    }
}


class Expense {

    private Integer id;
    private List<String> members;
    private List<Integer> paid ;

    public Expense(int id, List<String> members, List<Integer> paid) {
        this.id = id;
        this.members = members;
        this.paid = paid;
    }

    public List<String> getMembers() {
        return members;
    }

    public List<Integer> getPaid() {
        return paid;
    }
}

class ExpenseUser {
    String id;
    double amount;
    public ExpenseUser(String id, double amount) {
        this.id = id;
        this.amount = amount;
    }
    public double getAmount() {
        return amount;
    }
}

enum ExpenseType {
    EQUAL,
    EXACT,
    PERCENT
}

class ExpenseRecord {
    public ExpenseRecord(Map<String, Double> creditors, Map<String, Double> debitors) {
        this.creditors = creditors;
        this.debitors = debitors;
    }

    public Map<String, Double> getCreditors() {
        return creditors;
    }

    public void setCreditors(Map<String, Double> creditors) {
        this.creditors = creditors;
    }

    public Map<String, Double> getDebitors() {
        return debitors;
    }

    public void setDebitors(Map<String, Double> debitors) {
        this.debitors = debitors;
    }

    Map<String, Double> creditors = new HashMap<>();
    Map<String, Double> debitors = new HashMap<>();
}


interface SplitStrategy {
    public ExpenseRecord getSplit(List<String> user, List<Integer> amount, ExpenseRecord expenseRecord);
}

class EqualSplit implements SplitStrategy {
    public ExpenseRecord getSplit(List<String> user, List<Integer> amount, ExpenseRecord expenseRecord) {
        int totalAmount = amount.stream().mapToInt(Integer::intValue).sum();
        Map<String, Double> creditors = expenseRecord.getCreditors();
        Map<String, Double> debitors = expenseRecord.getDebitors();
        double preUserExpense = (double) totalAmount / user.size();
        for (int i =0; i<user.size(); i++) {
            if (amount.get(i) > preUserExpense) {
                double currentAmountInCredit = creditors.getOrDefault(user.get(i), (double) 0);
                double currentAmountInDebit = debitors.getOrDefault(user.get(i), (double) 0);
                double amountNeedToAdded = amount.get(i) - preUserExpense;
                if (currentAmountInCredit > 0) {
                    creditors.put(user.get(i), currentAmountInCredit + amountNeedToAdded);
                } else if (currentAmountInDebit > amountNeedToAdded) {
                    debitors.put(user.get(i), currentAmountInDebit - amountNeedToAdded);
                } else if (currentAmountInDebit < amountNeedToAdded) {
                    debitors.remove(user.get(i));
                    creditors.put(user.get(i), amountNeedToAdded - currentAmountInDebit);
                }
            } else if (amount.get(i) < preUserExpense) {
                double currentAmountInCredit = creditors.getOrDefault(user.get(i), (double) 0);
                double currentAmountInDebit = debitors.getOrDefault(user.get(i), (double) 0);
                double amountNeedToDebit = preUserExpense - amount.get(i);
                if (currentAmountInDebit > 0) {
                    debitors.put(user.get(i), currentAmountInDebit + amountNeedToDebit);
                } else if (currentAmountInCredit > amountNeedToDebit) {
                    creditors.put(user.get(i), currentAmountInCredit - amountNeedToDebit);
                } else if (currentAmountInCredit < amountNeedToDebit) {
                    creditors.remove(user.get(i));
                    debitors.put(user.get(i), amountNeedToDebit - currentAmountInCredit);
                }
            }
        }
        return new ExpenseRecord(creditors, debitors);
    }
}

class ExpenseService {

    HashMap<Integer, Expense> expenseDb = new HashMap<>();
//    Map<String, Double> creditors = new HashMap<>();
//    Map<String, Double> debitors = new HashMap<>();

    public boolean isExpenseExist(Integer id) {
        return expenseDb.containsKey(id);
    }

    public void createExpense(Integer id, List<String> user, List<Integer> amount) {
        expenseDb.put(id, new Expense(id, user, amount));
    }

    public List<String> settleBalanceOptimized(ExpenseRecord expenseRecord) {

        List<String> result = new ArrayList<>();

        TreeMap<Double, List<String>> creditors = new TreeMap<>();
        TreeMap<Double, List<String>> debitors = new TreeMap<>();

        // Build maps
        for (var e : expenseRecord.getCreditors().entrySet()) {
            creditors.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
        }

        for (var e : expenseRecord.getDebitors().entrySet()) {
            debitors.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
        }

        while (!creditors.isEmpty() && !debitors.isEmpty()) {

            var maxCreditorEntry = creditors.lastEntry();
            var maxDebitorEntry = debitors.lastEntry();

            String creditor = maxCreditorEntry.getValue().remove(0);
            String debtor = maxDebitorEntry.getValue().remove(0);

            double creditAmt = maxCreditorEntry.getKey();
            double debitAmt = maxDebitorEntry.getKey();

            double min = Math.min(creditAmt, debitAmt);

            result.add(debtor + " owes " + creditor + ": " + String.format("%.2f", min));

            // Clean up empty lists
            if (maxCreditorEntry.getValue().isEmpty()) creditors.remove(creditAmt);
            if (maxDebitorEntry.getValue().isEmpty()) debitors.remove(debitAmt);

            // Remaining
            if (creditAmt > min) {
                creditors.computeIfAbsent(creditAmt - min, k -> new ArrayList<>()).add(creditor);
            }
            if (debitAmt > min) {
                debitors.computeIfAbsent(debitAmt - min, k -> new ArrayList<>()).add(debtor);
            }
        }

        return result;
    }

    public List<String> settleBalance(ExpenseRecord expenseRecord) {
        List<String> result = new ArrayList<>();
        List<ExpenseUser> creditorsList = new ArrayList<>();
        List<ExpenseUser> debitorsList = new ArrayList<>();
        expenseRecord.getCreditors().forEach((k, v) -> creditorsList.add(new ExpenseUser(k, v)));
        expenseRecord.getDebitors().forEach((k, v) -> debitorsList.add(new ExpenseUser(k, v)));
        creditorsList.sort(Comparator.comparing(ExpenseUser::getAmount).reversed());
        debitorsList.sort(Comparator.comparing(ExpenseUser::getAmount).reversed());

        while (!creditorsList.isEmpty() && !debitorsList.isEmpty()) {
            ExpenseUser creditor = creditorsList.remove(0);
            ExpenseUser debtor = debitorsList.remove(0);
            double min = Math.min(creditor.amount, debtor.amount);
            result.add(debtor.id + " owes " + creditor.id + ": " + String.format("%.2f", min));
            double creditorRemaining = creditor.amount - min;
            double debtorRemaining = debtor.amount - min;
            if (creditorRemaining > 0) {
                creditorsList.add(new ExpenseUser(creditor.id, creditorRemaining));
                creditorsList.sort(Comparator.comparing(ExpenseUser::getAmount).reversed());
            }
            if (debtorRemaining > 0) {
                debitorsList.add(new ExpenseUser(debtor.id, debtorRemaining));
                debitorsList.sort(Comparator.comparing(ExpenseUser::getAmount).reversed());
            }
        }

        Collections.sort(result);
        return result;
    }
}

public class SplitBook {

    UserService userService = new UserService();
    ExpenseService expenseService = new ExpenseService();
    ExpenseRecord expenseRecord;
    public SplitBook() {
        expenseRecord = new ExpenseRecord(new HashMap<>(), new HashMap<>());
    }

    public void registerUser(String userId, String displayName) {
        userService.registerUser(userId, displayName);
    }

    public void recordExpense(int expenseId, List<String> members, List<Integer> paid) {
        if (expenseService.isExpenseExist(expenseId))
            return;
        // check each user.
        SplitStrategy splitStrategy = new EqualSplit();
        expenseRecord = splitStrategy.getSplit(members, paid, expenseRecord);
        for (String member: members) {
            if (!userService.isUserRegistered(member)) return;
        }
        expenseService.createExpense(expenseId, members, paid);
    }

    public List<String> listBalances() {
        return expenseService.settleBalance(expenseRecord);
    }
}
